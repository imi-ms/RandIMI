package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.study.MinimizationParameter;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.service.StudyUtilityService;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a minimization algorithm.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class MinimizationRandomization extends AbstractRandomization {

	private final StratumCodeService stratumCodeService;
	private final StudyUtilityService studyUtilityService;
	private final SubjectRepository subjectRepository;

	public MinimizationRandomization(final MessageService messageService, final StratumCodeService stratumCodeService,
	                                 final StudyUtilityService studyUtilityService,
	                                 final SubjectRepository subjectRepository) {
		super(messageService);
		this.stratumCodeService = stratumCodeService;
		this.studyUtilityService = studyUtilityService;
		this.subjectRepository = subjectRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RandomizationAlgorithm getAlgorithm() {
		return RandomizationAlgorithm.MINIMIZATION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSubjectListCreation(final SubjectList subjectList) {
		// Nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStudyDTOValidation(final Errors errors, final StudyDTO studyDTO) {
		if (studyDTO.getPreGenerateSubjectList()) {
			errors.rejectValue("preGenerateSubjectList", "validator.study.preGenerateSubjectListMinimization");
		}

		if (studyDTO.getMinimizationParameter() == null) {
			errors.rejectValue("minimizationParameter", "validator.general.mustNotBeNull");
			return;
		}

		if (studyDTO.getMinimizationParameter().getForceRatio() == null) {
			errors.rejectValue("minimizationParameter.forceRatio", "validator.general.mustNotBeNull");
		}

		if (studyDTO.getMinimizationParameter().getImbalanceBias() == null) {
			errors.rejectValue("minimizationParameter.imbalanceBias", "validator.general.mustNotBeNull");
		} else if (studyDTO.getMinimizationParameter().getImbalanceBias() < 0 ||
		    studyDTO.getMinimizationParameter().getImbalanceBias() > 1) {
			errors.rejectValue("minimizationParameter.imbalanceBias", "validator.general.mustBeBetween",
			                   new Object[]{0, 1}, null);
		}

		if (studyDTO.getMinimizationParameter().getImbalanceFunction() == null) {
			errors.rejectValue("minimizationParameter.imbalanceFunction", "validator.general.mustNotBeNull");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSubjectRelease(final Subject subject) {
		// Nothing to do
	}

	/**
	 * Implementation of minimization.
	 * See <a href="https://www.jstor.org/stable/2529712?seq=4">this paper</a>.
	 *
	 * @param site        Site containing the seed to use.
	 * @param subjectList The list the subject will be assigned to.
	 * @return A random study arm.
	 */
	@Override
	public StudyArm getRandomStudyArm(final Site site, final SubjectList subjectList) {
		final Study study = site.getStudy();


		// 1. Get the number of patients for each study arm and stratum value of the next patient
		final List<List<Float>> numberSubjects = getNumberSubjectsStudyArmAndStratumPart(study, subjectList);

		// 2. Calculate the treatment number for each value and study arm
		final List<List<Float>> t = calculateImbalancePerStratum(study.getMinimizationParameter(), numberSubjects);

		// 3. Calculate the imbalance for each study arm if the next patient would be allocated to that study arm
		final List<Float> imbalances = calculateImbalance(t);

		// 4. Get the probabilities for each study arm
		List<Float> probabilities = calculateProbabilities(study, imbalances);

		// 5. Adapt probabilities to comply with the capacities
		probabilities = forceCapacities(site, subjectList, probabilities);

		// 6. Draw study arm
		return drawStudyArm(site, probabilities);
	}

	/**
	 * Returns for each study arm and stratum part of the new subject
	 * the number of subjects in the entire study that match the study arm and stratum value.
	 *
	 * @param study       The study.
	 * @param subjectList The subject list the new subject will be added to.
	 * @return The number of subjects.
	 */
	private List<List<Float>> getNumberSubjectsStudyArmAndStratumPart(final Study study, final SubjectList subjectList) {
		final List<StratumPartBase> parts = subjectList.getStratumParts();
		final List<List<Float>> numberSubjects = new ArrayList<>(study.getStudyArms().size());

		for (final StudyArm studyArm : study.getStudyArms()) {
			final List<Float> numberSubjectsStudyArm = new ArrayList<>(parts.size());

			if (parts.isEmpty()) {
				final long n = subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(subjectList.getId(),
				                                                                              studyArm.getId());
				numberSubjectsStudyArm.add((float) n / studyArm.getRatio());
			} else {
				for (final StratumPartBase part : parts) {
					final long n = subjectRepository.countBlockingSubjectForStudyArmAndStratumPart(studyArm.getId(),
					                                                                               part.getId());
					numberSubjectsStudyArm.add((float) n / studyArm.getRatio());
				}
			}

			numberSubjects.add(numberSubjectsStudyArm);
		}

		return numberSubjects;
	}

	/**
	 * Calculates the imbalance for each study arm and stratum value under the assumption the new subject would be assigned to the study arm respectively.
	 *
	 * @param parameter      Parameter for the minimization algorithm.
	 * @param numberSubjects The current number of subjects for each study arm and stratum value.
	 * @return The imbalance.
	 */
	private List<List<Float>> calculateImbalancePerStratum(final MinimizationParameter parameter,
	                                                       final List<List<Float>> numberSubjects) {
		final int numberStudyArms = numberSubjects.size();
		final int numberFactors = numberSubjects.get(0).size();
		final List<List<Float>> t = new ArrayList<>(numberStudyArms);

		final List<Float> values = new ArrayList<>(numberStudyArms);
		for (int i = 0; i < numberStudyArms; i++) {
			values.add(0.0f);
		}

		for (int studyArmIndex = 0; studyArmIndex < numberStudyArms; studyArmIndex++) {
			final List<Float> ts = new ArrayList<>(numberFactors);

			for (int factorIndex = 0; factorIndex < numberFactors; ++factorIndex) {
				// Copy values
				for (int j = 0; j < numberStudyArms; j++) {
					values.set(j, numberSubjects.get(j).get(factorIndex));
				}
				values.set(studyArmIndex, values.get(studyArmIndex) + 1);

				final float imbalance = switch (parameter.getImbalanceFunction()) {
					case RANGE -> range(values);
					case VARIANCE -> variance(values);
				};

				ts.add(imbalance);
			}

			t.add(ts);
		}

		return t;
	}

	/**
	 * Calculates the imbalance of each study arm.
	 *
	 * @param t The imbalance for each study arm and stratum value.
	 * @return The imbalance.
	 */
	private List<Float> calculateImbalance(final List<List<Float>> t) {
		final List<Float> imbalances = new ArrayList<>(t.size());

		for (final List<Float> floats : t) {
			float imbalance = 0.0f;
			for (int i = 0; i < t.get(0).size(); i++) {
				imbalance += floats.get(i);
			}

			imbalances.add(imbalance);
		}

		return imbalances;
	}

	/**
	 * Calculatest the probabilities for each study arm to be drawn.
	 *
	 * @param study      The study.
	 * @param imbalances The imbalances for each study arm.
	 * @return The probabilities.
	 */
	private List<Float> calculateProbabilities(final Study study, final List<Float> imbalances) {
		final List<Float> probabilities = new ArrayList<>(imbalances.size());
		final float minImbalance = Collections.min(imbalances);
		int numberWithMinImbalance = 0;
		int studyArmRatioPreferred = 0;

		for (int studyArmIndex = 0; studyArmIndex < study.getStudyArms().size(); studyArmIndex++) {
			if (imbalances.get(studyArmIndex) == minImbalance) {
				numberWithMinImbalance++;
				studyArmRatioPreferred += study.getStudyArms().get(studyArmIndex).getRatio();
			}
		}

		final float n = imbalances.size();
		final float p = numberWithMinImbalance == study.getStudyArms().size()
		                ? 1.0f
		                : study.getMinimizationParameter().getImbalanceBias();
		for (int studyArmIndex = 0; studyArmIndex < study.getStudyArms().size(); studyArmIndex++) {
			if (imbalances.get(studyArmIndex) == minImbalance) {
				probabilities.add(p * (study.getStudyArms().get(studyArmIndex).getRatio()) / studyArmRatioPreferred);
			} else {
				probabilities.add(1.0f - p / (n - numberWithMinImbalance));
			}
		}

		return probabilities;
	}

	/**
	 * Sets the probabilities to draw study arms that have reached the capacity to 0.
	 *
	 * @param site          The site of the subject
	 * @param subjectList   The subject list the new subject will be added to.
	 * @param probabilities The probabilities.
	 * @return The modified probabilities
	 */
	private List<Float> forceCapacities(final Site site, final SubjectList subjectList,
	                                    final List<Float> probabilities) {
		if (!site.getStudy().getMinimizationParameter().isForceRatio()) {
			return probabilities;
		}

		final int listCapacity = stratumCodeService.getCapacity(subjectList);
		for (int studyArmIndex = 0; studyArmIndex < probabilities.size(); studyArmIndex++) {
			final StudyArm studyArm = site.getStudy().getStudyArms().get(studyArmIndex);
			final int sum = studyUtilityService.calculateNumberOfStudyArmParts(site.getStudy());
			final int studyArmCapacity = (listCapacity / sum) * studyArm.getRatio();
			final long current = subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(subjectList.getId(),
			                                                                                    studyArm.getId());

			if (current == studyArmCapacity) {
				probabilities.set(studyArmIndex, 0.0f);
			}
		}

		return normalize(probabilities);
	}

	/**
	 * Draws a random study arm with the given probabilities.
	 *
	 * @param site          Site containing the random state.
	 * @param probabilities The probabilities.
	 * @return The study arm.
	 */
	private StudyArm drawStudyArm(final Site site, final List<Float> probabilities) {
		final Study study = site.getStudy();

		final float draw = site.nextRandomFloat();
		float sum = 0;
		for (int studyArmIndex = 0; studyArmIndex < probabilities.size(); studyArmIndex++) {
			sum += probabilities.get(studyArmIndex);
			if (draw <= sum) {
				return study.getStudyArms().get(studyArmIndex);
			}
		}

		return study.getStudyArms().get(study.getStudyArms().size() - 1);
	}


	/**
	 * Calculates the range, i.e., the largest minus the smallest value.
	 *
	 * @param values The values.
	 * @return The range.
	 */
	private float range(final List<Float> values) {
		final float min = Collections.min(values);
		final float max = Collections.max(values);
		return max - min;
	}

	private float variance(final List<Float> values) {
		final float m = mean(values);
		float sum = 0;
		for (final Float value : values) {
			sum += (float) Math.pow(value - m, 2);
		}
		return sum / values.size();
	}

	private float mean(final List<Float> values) {
		float sum = 0.0f;
		for (final Float value : values) {
			sum += value;
		}
		return sum / values.size();
	}

	private List<Float> normalize(final List<Float> values) {
		float sum = 0.0f;
		for (final Float value : values) {
			sum += value;
		}
		if (sum == 0.0f) {
			return values;
		}

		final List<Float> normalized = new ArrayList<>(values.size());
		for (final Float value : values) {
			normalized.add(value / sum);
		}
		return normalized;
	}
}
