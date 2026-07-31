package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyStatisticsDTO;
import de.unimuenster.imi.randimi.dto.study.SubjectListStatisticsDTO;
import de.unimuenster.imi.randimi.dto.study.statistics.SiteStatisticsDTO;
import de.unimuenster.imi.randimi.dto.study.statistics.SubjectSeriesDTO;
import de.unimuenster.imi.randimi.dto.study.statistics.SubjectSeriesParameterDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.mapping.study.SiteMapper;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumPartMapper;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for calculating statistics.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class StatisticsService {

	private final SubjectRepository subjectRepository;

	private final SiteMapper siteMapper;
	private final StratumPartMapper stratumPartMapper;

	private final RandimiExceptionFactoryService exceptionFactoryService;
	private final StratumCodeService stratumCodeService;

	public StatisticsService(final SubjectRepository subjectRepository, final SiteMapper siteMapper,
	                         final StratumPartMapper stratumPartMapper,
	                         final RandimiExceptionFactoryService exceptionFactoryService,
	                         final StratumCodeService stratumCodeService) {
		this.subjectRepository = subjectRepository;
		this.siteMapper = siteMapper;
		this.stratumPartMapper = stratumPartMapper;
		this.exceptionFactoryService = exceptionFactoryService;
		this.stratumCodeService = stratumCodeService;
	}

	/**
	 * Creates the study statistics for a given study.
	 *
	 * @param study The study for which the statistics should be created.
	 * @return The study statistics.
	 */
	public StudyStatisticsDTO createStudyStatistics(final Study study) {
		final var studyStatistics = new StudyStatisticsDTO();

		final var siteStatistics = createSiteStatistics(study);
		studyStatistics.setSiteStatistics(siteStatistics);

		final var subjectListStatistics = createSubjectListStatistics(study);
		studyStatistics.setSubjectListStatistics(subjectListStatistics);

		return studyStatistics;
	}

	/**
	 * Creates a series about the number of subjects over the course of the study.
	 * Subjects included are filtered by the given parameters.
	 * The series starts with the first randomization and ends with the last randomization or deallocation.
	 *
	 * @param study      The study for which the statistics should be created.
	 * @param parameters The parameters for filtering the subjects.
	 * @return The series.
	 * @throws RandimiException If the study is not active or deleted.
	 */
	public SubjectSeriesDTO createSubjectSeriesStatistics(
			final Study study,
			final SubjectSeriesParameterDTO parameters
	) throws RandimiException {
		// Validate state
		if (study.getStatus() == StudyStatus.INEXISTENT || study.getStatus() == StudyStatus.CREATED) {
			throw exceptionFactoryService.notAcceptableStudyNotActive(study);
		}
		if (study.getStatus() == StudyStatus.DELETED) {
			throw exceptionFactoryService.notAcceptableStudyDeleted(study);
		}

		// Find the start and end of the series
		final var firstSubject = subjectRepository.findFirstBySubjectListStudyIdAndStatusNotOrderByRandomizationTimestampAsc(study.getId(), SubjectStatus.PRE_GENERATED);
		final var lastSubject = subjectRepository.findFirstBySubjectListStudyIdAndStatusNotOrderByGreatestDesc(study.getId(), SubjectStatus.PRE_GENERATED);

		LocalDate startDate;
		LocalDate endDate;
		int[] series;

		// Edgecase if the study does not contain subjects
		if (firstSubject.isEmpty() || lastSubject.isEmpty()) {
			// Create empty series
			startDate = study.getActivationDate().toLocalDateTime().toLocalDate();
			endDate = LocalDate.now();
			long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
			series = new int[Math.toIntExact(numberOfDays)];
		} else {

			// Initialize the series
			startDate = firstSubject.get().getRandomizationTimestamp().toLocalDateTime().toLocalDate();
			endDate = lastSubject.get().getReleaseTimestamp() != null
			          ? lastSubject.get().getReleaseTimestamp().toLocalDateTime().toLocalDate()
			          : lastSubject.get().getRandomizationTimestamp().toLocalDateTime().toLocalDate();
			long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
			series = new int[Math.toIntExact(numberOfDays)];

			final List<Subject> subjects = getSubjects(study, parameters);
			for (final Subject subject : subjects) {
				final var randomizationDate = subject.getRandomizationTimestamp().toLocalDateTime().toLocalDate();
				final var day = Long.valueOf(ChronoUnit.DAYS.between(startDate, randomizationDate)).intValue();
				series[day] = series[day] + 1;

				if (subject.getReleaseTimestamp() != null) {
					final var releaseDate = subject.getReleaseTimestamp().toLocalDateTime().toLocalDate();
					final var releaseDay = Long.valueOf(ChronoUnit.DAYS.between(startDate, releaseDate)).intValue();
					series[releaseDay] = series[releaseDay] - 1;
				}
			}

			// Accumulate the series
			for (int i = 1; i < numberOfDays; i++) {
				series[i] = series[i] + series[i - 1];
			}
		}


		// Create the series
		final var subjectSeries = new SubjectSeriesDTO();
		subjectSeries.setStart(startDate);
		subjectSeries.setEnd(endDate);
		subjectSeries.setSeries(series);
		setSite(subjectSeries, study, parameters);
		setStratumParts(subjectSeries, study, parameters);
		setTarget(subjectSeries, study, parameters);

		return subjectSeries;
	}

	/**
	 * Creates the site statistics for a given study.
	 *
	 * @param study The study.
	 * @return The statistics.
	 */
	private List<SiteStatisticsDTO> createSiteStatistics(final Study study) {
		final List<SiteStatisticsDTO> result = new ArrayList<>();

		for (final Site site : study.getSites()) {
			var siteStatistics = new SiteStatisticsDTO();
			result.add(siteStatistics);

			siteStatistics.setSite(siteMapper.toSiteDto(site));
			siteStatistics.setNumberSubjects(
					subjectRepository.countBlockingSubjectInStudyAndSite(study.getId(), site.getId()));

			final Map<String, Long> numberSubjectsPerStudyArm = new HashMap<>();
			for (final var studyArm : study.getStudyArms()) {
				long number = subjectRepository.countBlockingSubjectInStudyAndSiteAndStudyArm(study.getId(),
				                                                                              site.getId(),
				                                                                              studyArm.getId());
				numberSubjectsPerStudyArm.put(studyArm.getApiId(), number);
			}

			siteStatistics.setNumberSubjectsPerStudyArm(numberSubjectsPerStudyArm);
		}

		return result;
	}

	/**
	 * Creates the subject list statistics for a given study.
	 * For each subject list in the study, one SubjectListStatisticsDTO is created.
	 *
	 * @param study The study.
	 * @return The statistics.
	 */
	private List<SubjectListStatisticsDTO> createSubjectListStatistics(final Study study) {
		final List<SubjectListStatisticsDTO> result = new ArrayList<>();

		for (final var subjectList : study.getSubjectLists()) {
			var subjectListStatistics = new SubjectListStatisticsDTO();
			result.add(subjectListStatistics);

			var stratumParts = subjectList.getStratumParts()
			                              .stream()
			                              .map(stratumPartMapper::toStratumPartBaseDTO)
			                              .toList();
			subjectListStatistics.setStratumParts(stratumParts);

			subjectListStatistics.setCapacity(stratumCodeService.getCapacity(subjectList));
			subjectListStatistics.setNumberSubjects(
					subjectRepository.countBlockingSubjectInSubjectList(subjectList.getId()));


			Map<String, Long> numberSubjectsPerStudyArm = new HashMap<>();
			for (final var studyArm : subjectList.getStudy().getStudyArms()) {
				long number = subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(subjectList.getId(), studyArm.getId());
				numberSubjectsPerStudyArm.put(studyArm.getApiId(), number);
			}

			subjectListStatistics.setNumberSubjectsPerStudyArm(numberSubjectsPerStudyArm);
		}

		return result;
	}

	/**
	 * Get the subjects for the given study that matches the given parameters.
	 *
	 * @param study      The study for which the subjects should be retrieved.
	 * @param parameters The parameters for filtering the subjects.
	 * @return The list of subjects that match the given parameters.
	 */
	private List<Subject> getSubjects(final Study study, final SubjectSeriesParameterDTO parameters) throws RandimiException {
		if (parameters.getStrataParameters() != null) {
			final List<StratumPartBase> stratumParts = stratumCodeService.calculateStratumParts(study,
			                                                                                    parameters.getStrataParameters(),
			                                                                                    parameters.getSiteApiId());
			final Optional<SubjectList> subjectListOptional = stratumCodeService.getSubjectListForParts(stratumParts, study);
			if (subjectListOptional.isEmpty()) {
				throw exceptionFactoryService.notAcceptableMissingRandomizationList(study);
			}

			return subjectRepository.findBySubjectListIdAndStatusNot(subjectListOptional.get().getId(),
			                                                         SubjectStatus.PRE_GENERATED);
		} else if (parameters.getSiteApiId() != null) {
			return subjectRepository.findBySubjectListStudyIdAndSiteApiIdAndStatusNot(study.getId(),
			                                                                          parameters.getSiteApiId(),
			                                                                          SubjectStatus.PRE_GENERATED);
		} else {
			return subjectRepository.findBySubjectListStudyIdAndStatusNot(study.getId(), SubjectStatus.PRE_GENERATED);
		}
	}

	/**
	 * Set the site for the given series.
	 *
	 * @param series     The series to set the site for.
	 * @param study      The study containing the site.
	 * @param parameters The parameters containing the site API ID.
	 */
	private void setSite(final SubjectSeriesDTO series, final Study study, final SubjectSeriesParameterDTO parameters) throws RandimiException {
		if (parameters.getSiteApiId() != null) {
			final Site site = study.getSiteByApiId(parameters.getSiteApiId());

			if (site == null) {
				throw exceptionFactoryService.notAcceptableMissingSite(parameters.getSiteApiId(), study);
			}

			final SiteDTO siteDto = this.siteMapper.toSiteDto(site);
			series.setSite(siteDto);
		} else {
			series.setSite(null);
		}
	}

	/**
	 * Sets the stratum parts in the given series.
	 *
	 * @param series     The series.
	 * @param study      The study containing the strata.
	 * @param parameters The parameters containing the stratum parameters.
	 */
	private void setStratumParts(final SubjectSeriesDTO series, final Study study, final SubjectSeriesParameterDTO parameters) throws RandimiException {
		if (parameters.getStrataParameters() != null) {
			final Map<String, String> stratumParts = new HashMap<>();

			for (final var entry : parameters.getStrataParameters().entrySet()) {
				final Stratum stratum = study.getStratumByApiId(entry.getKey()).orElse(null);
				if (stratum == null) {
					throw exceptionFactoryService.missingParameterStratum(entry.getKey());
				}

				final StratumPartBase part = stratum.getStratumPartByValue(entry.getValue());

				if (part == null) {
					throw exceptionFactoryService.notAcceptableMissingMatchingStratumPart(entry.getValue(), stratum);
				}

				stratumParts.put(stratum.getName(), part.getName());
			}

			series.setStrataParts(stratumParts);
		}
	}

	/**
	 * Sets the target value of the series based on the request parameters.
	 *
	 * @param series     The series.
	 * @param study      The study.
	 * @param parameters The request parameters.
	 */
	private void setTarget(final SubjectSeriesDTO series, final Study study, final SubjectSeriesParameterDTO parameters) throws RandimiException {
		if (parameters.getStrataParameters() != null) {
			final List<StratumPartBase> stratumParts = stratumCodeService.calculateStratumParts(study,
			                                                                                    parameters.getStrataParameters(),
			                                                                                    parameters.getSiteApiId());
			final Optional<SubjectList> subjectListOptional = stratumCodeService.getSubjectListForParts(stratumParts, study);
			if (subjectListOptional.isEmpty()) {
				throw exceptionFactoryService.notAcceptableMissingRandomizationList(study);
			}

			series.setTarget(stratumCodeService.getCapacity(subjectListOptional.get()));
		} else if (parameters.getSiteApiId() != null) {
			final Site site = study.getSiteByApiId(parameters.getSiteApiId());
			if (site == null) {
				throw exceptionFactoryService.notAcceptableMissingSite(parameters.getSiteApiId(), study);
			}
			series.setTarget(site.getCapacity());
		} else {
			series.setTarget(study.getCapacity());
		}
	}

}
