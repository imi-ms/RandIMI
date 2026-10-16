package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomService;
import de.unimuenster.imi.randimi.service.StratumCodeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.unimuenster.imi.randimi.service.StudyUtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/**
 * Implementation of a blocked randomization algorithm.
 * <p>
 * Version 2: The study arm is drawn weighted by the remaining assignments in the current block.
 * See {@link BlockedRandomizationV1} for the previous version.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Service
public class BlockedRandomization extends AbstractRandomization {

	private final StratumCodeService stratumCodeService;

	private final SubjectRepository subjectRepository;

	protected final RandomService randomService;
	private final StudyUtilityService studyUtilityService;

	@Autowired
	public BlockedRandomization(final MessageService messageService, final StratumCodeService stratumCodeService,
	                            final SubjectRepository subjectRepository, final RandomService randomService,
	                            final StudyUtilityService studyUtilityService) {
		super(messageService);
		this.stratumCodeService = stratumCodeService;
		this.subjectRepository = subjectRepository;
		this.randomService = randomService;
		this.studyUtilityService = studyUtilityService;
	}

	@Override
	public RandomizationAlgorithm getAlgorithm() {
		return RandomizationAlgorithm.BLOCKED;
	}

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	public void onSubjectListCreation(final SubjectList subjectList) {
		final int numberStudyArms = subjectList.getStudy().getStudyArms().size();
		final Integer[] remainingAssignments = new Integer[numberStudyArms];
		Arrays.fill(remainingAssignments, 0);
		subjectList.setRemainingAssignments(remainingAssignments);
	}

	@Override
	public void onStudyDTOValidation(Errors errors, StudyDTO studyDTO) {
		// Min blocksize
		if (studyDTO.getMinBlocksize() == null) {
			errors.rejectValue("minBlocksize", "errormessage", getMsg("validator.general.mustNotBeNull"));
			return;
		} else if (studyDTO.getMinBlocksize() < 2) {
			errors.rejectValue("minBlocksize", "errormessage",
					getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
			return;
		}

		// Max blocksize
		if (studyDTO.getMaxBlocksize() == null) {
			errors.rejectValue("maxBlocksize", "errormessage", getMsg("validator.general.mustNotBeNull"));
			return;
		} else if (studyDTO.getMaxBlocksize() < 2) {
			errors.rejectValue("maxBlocksize", "errormessage",
					getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
			return;
		}

		if (studyDTO.getMinBlocksize() > studyDTO.getMaxBlocksize()) {
			errors.rejectValue("minBlocksize", "errormessage",
					getMsg("validator.study.minBlocksizeLargerThanMaxBlocksize"));
			return;
		}

		int numberStratumCombinations = stratumCodeService.getNumberOfStratumCombinationsPerSite(studyDTO);
		if (studyDTO.isStratifyBySite()) {
			for (SiteDTO siteDTO : studyDTO.getSites()) {
				// Check minimal and maximal blocksizes with regard to the stratum size
				int stratumSize = siteDTO.getCapacity() / numberStratumCombinations;
				if (studyDTO.getMinBlocksize() > stratumSize) {
					errors.rejectValue("minBlocksize", "errormessage",
							getMsg("validator.study.minBlocksizeLargerThanStratumSize", stratumSize));
					return;
				}
				if (studyDTO.getMaxBlocksize() > stratumSize) {
					errors.rejectValue("maxBlocksize", "errormessage",
							getMsg("validator.study.maxBlocksizeLargerThanStratumSize", stratumSize));
					return;
				}
			}
		} else if (studyDTO.getCapacity() != null) {
			int stratumSize = studyDTO.getCapacity() / numberStratumCombinations;
			if (studyDTO.getMinBlocksize() > stratumSize) {
				errors.rejectValue("minBlocksize", "errormessage",
						getMsg("validator.study.minBlocksizeLargerThanStratumSize", stratumSize));
				return;
			}
			if (studyDTO.getMaxBlocksize() > stratumSize) {
				errors.rejectValue("maxBlocksize", "errormessage",
						getMsg("validator.study.maxBlocksizeLargerThanStratumSize", stratumSize));
				return;
			}
		}

		// Check if there are possible blocksizes in range
		int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(studyDTO);
		if (numberStudyArmParts > 0) {
			boolean foundPossibleBlockSize = false;
			for (int blockSize = studyDTO.getMinBlocksize(); blockSize <= studyDTO.getMaxBlocksize(); ++blockSize) {
				if (blockSize % numberStudyArmParts == 0) {
					foundPossibleBlockSize = true;
					break;
				}
			}
			if (!foundPossibleBlockSize) {
				errors.rejectValue("maxBlocksize", "errormessage",
				                   getMsg("validator.study.blocksizeNotDivisibleByStudyArms", numberStudyArmParts));
			}
		}
	}

	@Override
	public void onSubjectRelease(Subject subject) {
		SubjectList subjectList = subject.getSubjectList();

		if (isCurrentBlockFull(subjectList))
			createNewBlock(subject.getSite(), subjectList);

		int studyArmIndex = 0;
		List<StudyArm> studyArms = subjectList.getStudy().getStudyArms();

		for (int i = 0; i < studyArms.size(); i++) {
			if (studyArms.get(i).getId() == subject.getStudyArm().getId()) {
				studyArmIndex = i;
				break;
			}
		}

		subjectList.getRemainingAssignments()[studyArmIndex] += 1;
	}

	@Override
	public StudyArm getRandomStudyArm(Site site, SubjectList subjectList) {
		if (isCurrentBlockFull(subjectList))
			createNewBlock(site, subjectList);

		Integer[] remainingAssignments = subjectList.getRemainingAssignments();
		int studyArmIndex = drawStudyArmIndex(site, subjectList, remainingAssignments);

		remainingAssignments[studyArmIndex] -= 1;

		return site.getStudy().getStudyArms().get(studyArmIndex);
	}

	/**
	 * Draws the index of the study arm for the next subject of the current block.
	 * Draws one of the remaining assignments of the block, so each study arm is weighted by its remaining
	 * assignments and every permutation of the block is equally likely.
	 *
	 * @param site                 Site containing the seed to use.
	 * @param subjectList          The list the subject will be assigned to.
	 * @param remainingAssignments Remaining assignments of each study arm in the current block.
	 * @return Index of the drawn study arm.
	 */
	protected int drawStudyArmIndex(Site site, SubjectList subjectList, Integer[] remainingAssignments) {
		int numberRemainingAssignments = 0;
		for (final Integer remaining : remainingAssignments)
			numberRemainingAssignments += remaining;

		int remainingAssignmentIndex = randomService.nextRandomInt(site, subjectList, numberRemainingAssignments);
		int studyArmIndex = 0;
		while (remainingAssignmentIndex >= remainingAssignments[studyArmIndex]) {
			remainingAssignmentIndex -= remainingAssignments[studyArmIndex];
			studyArmIndex++;
		}

		return studyArmIndex;
	}

	private boolean isCurrentBlockFull(SubjectList subjectList) {
		for (Integer integer : subjectList.getRemainingAssignments())
			if (integer != 0)
				return false;
		return true;
	}

	private void createNewBlock(Site site, SubjectList subjectList) {
		Study study = subjectList.getStudy();
		final int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		int blockSize = drawBlockSize(site, subjectList);
		int assignmentsPerPart = blockSize / numberStudyArmParts;

		Integer[] remainingAssignments = subjectList.getRemainingAssignments();
		for (int studyArmIndex = 0; studyArmIndex < study.getStudyArms().size(); ++studyArmIndex) {
			StudyArm studyArm = study.getStudyArms().get(studyArmIndex);
			remainingAssignments[studyArmIndex] = assignmentsPerPart * studyArm.getRatio();
		}
	}

	private Integer drawBlockSize(final Site site, final SubjectList subjectList) {
		List<Integer> possibleBlockSizes = getPossibleBlockSizes(site, subjectList);
		int index = randomService.nextRandomInt(site, subjectList, possibleBlockSizes.size());
		return possibleBlockSizes.get(index);
	}

	protected List<Integer> getPossibleBlockSizes(final Site site, final SubjectList subjectList) {
		final List<Integer> possibleBlockSizes = new ArrayList<>();
		final Study study = subjectList.getStudy();
		final long subjectListSize = subjectRepository.countBlockingSubjectInSubjectList(subjectList.getId());

		final int limit;
		if (study.getStratums().isEmpty())
			limit = study.getCapacity();
		else if (!study.isStratifiedBySite())
			limit = study.getCapacity() / study.getSubjectLists().size();
		else
			limit = site.getCapacity() / stratumCodeService.getNumberOfStratumCombinationsPerSite(study);

		final int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		int blockSize = study.getMinBlocksize();

		// Find first block size
		boolean foundBlockSize = false;
		while (blockSize <= study.getMaxBlocksize() && !foundBlockSize) {
			if (blockSize % numberStudyArmParts == 0) {
				possibleBlockSizes.add(blockSize);
				foundBlockSize = true;
			} else {
				blockSize += 1;
			}
		}

		// Add possible block sizes until max block size or the capacity is reached
		boolean exceeded = false;
		while (!exceeded) {
			blockSize += numberStudyArmParts;
			if (blockSize > study.getMaxBlocksize() || subjectListSize + blockSize > limit)
				exceeded = true;
			else
				possibleBlockSizes.add(blockSize);
		}

		return possibleBlockSizes;
	}

}
