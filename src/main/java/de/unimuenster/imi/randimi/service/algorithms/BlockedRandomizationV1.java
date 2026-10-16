package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.service.StudyUtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Version 1 of the blocked randomization algorithm. Only kept for studies created with this version.
 * <p>
 * The study arm is drawn uniformly among the study arms with remaining assignments in the current block. For unequal
 * ratios this does not preserve the allocation ratio at each position of a block, and even for equal ratios the
 * permutations of a block are not equally likely.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class BlockedRandomizationV1 extends BlockedRandomization {

	@Autowired
	public BlockedRandomizationV1(final MessageService messageService, final StratumCodeService stratumCodeService,
	                              final SubjectRepository subjectRepository, final RandomService randomService,
	                              final StudyUtilityService studyUtilityService) {
		super(messageService, stratumCodeService, subjectRepository, randomService, studyUtilityService);
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	protected int drawStudyArmIndex(Site site, SubjectList subjectList, Integer[] remainingAssignments) {
		List<Integer> remainingAssignmentIndices = new ArrayList<Integer>();
		for (int i = 0; i < remainingAssignments.length; i++)
			if (remainingAssignments[i] != 0)
				remainingAssignmentIndices.add(i);

		int remainingAssignmentIndex = randomService.nextRandomInt(site, subjectList,
		                                                           remainingAssignmentIndices.size());
		return remainingAssignmentIndices.get(remainingAssignmentIndex);
	}
}
