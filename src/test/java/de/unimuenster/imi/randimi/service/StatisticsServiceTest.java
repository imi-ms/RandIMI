package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.statistics.SubjectSeriesParameterDTO;
import de.unimuenster.imi.randimi.mapping.study.SiteMapper;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumPartMapper;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class StatisticsServiceTest extends RandimiIntegrationTest {

	private static final long INTERVENTION_SUBJECTS = 5;
	private static final long CONTROL_SUBJECTS = 2;

	@Autowired private SiteMapper siteMapper;
	@Autowired private StratumPartMapper stratumPartMapper;

	@Autowired private RandimiExceptionFactoryService exceptionFactoryService;
	@Autowired private StratumCodeService stratumCodeService;

	private StatisticsService statisticsService;

	private Study study;

	@BeforeEach
	void setUp() {
		study = createStudy();
		Subject firstSubject = new Subject();
		firstSubject.setRandomizationTimestamp(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault()).minusDays(3)));
		Subject secondSubject = new Subject();
		secondSubject.setRandomizationTimestamp(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault()).minusDays(1)));
		Subject thirdSubject = new Subject();
		thirdSubject.setRandomizationTimestamp(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault()).minusDays(1)));
		List<Subject> subjectList = List.of(firstSubject, secondSubject, thirdSubject);
		List<Subject> subjectListForSite = List.of(firstSubject, thirdSubject);
		SubjectRepository subjectRepository = mock(SubjectRepository.class);
		when(subjectRepository.findBySubjectListStudyIdAndStatusNot(any(), any())).thenReturn(subjectList);
		when(subjectRepository.findBySubjectListStudyIdAndSiteApiIdAndStatusNot(any(), any(), any())).thenReturn(subjectListForSite);
		when(subjectRepository.findFirstBySubjectListStudyIdAndStatusNotOrderByRandomizationTimestampAsc(any(), any()))
				.thenReturn(Optional.of(firstSubject));
		when(subjectRepository.findFirstBySubjectListStudyIdAndStatusNotOrderByGreatestDesc(any(), any()))
				.thenReturn(Optional.of(thirdSubject));
		when(subjectRepository.countBlockingSubjectInSubjectList(anyLong())).thenReturn(3L);
		when(subjectRepository.countBlockingSubjectInStudyAndSite(anyLong(), anyLong())).thenReturn(12L);
		when(subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(anyLong(),
		                                                                    eq(study.getStudyArms().get(0).getId())))
				.thenReturn(INTERVENTION_SUBJECTS);
		when(subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(anyLong(),
		                                                                    eq(study.getStudyArms().get(1).getId())))
				.thenReturn(CONTROL_SUBJECTS);
		when(subjectRepository.countBlockingSubjectInStudyAndSiteAndStudyArm(anyLong(), anyLong(),
		                                                                     eq(study.getStudyArms().get(0).getId())))
				.thenReturn(INTERVENTION_SUBJECTS);
		when(subjectRepository.countBlockingSubjectInStudyAndSiteAndStudyArm(anyLong(), anyLong(),
		                                                                     eq(study.getStudyArms().get(1).getId())))
				.thenReturn(CONTROL_SUBJECTS);

		statisticsService = new StatisticsService(subjectRepository, siteMapper, stratumPartMapper,
		                                          exceptionFactoryService, stratumCodeService);
	}

	@Test
	void createStudyStatistics() {
		var result = statisticsService.createStudyStatistics(study);

		// Test site statistics
		var siteStatistics = result.getSiteStatistics();
		assertEquals(1, siteStatistics.size(), "Unexpected number of sites");

		var siteStatistic = siteStatistics.get(0);
		assertEquals("muenster", siteStatistic.getSite().getApiId(), "Unexpected site");
		assertEquals(12, siteStatistic.getNumberSubjects());

		var firstArm = study.getStudyArms().get(0);
		assertTrue(siteStatistic.getNumberSubjectsPerStudyArm().containsKey(firstArm.getApiId()));
		assertEquals(INTERVENTION_SUBJECTS, siteStatistic.getNumberSubjectsPerStudyArm().get(firstArm.getApiId()));

		var secondArm = study.getStudyArms().get(1);
		assertTrue(siteStatistic.getNumberSubjectsPerStudyArm().containsKey(secondArm.getApiId()));
		assertEquals(CONTROL_SUBJECTS, siteStatistic.getNumberSubjectsPerStudyArm().get(secondArm.getApiId()));

		assertEquals(5, siteStatistic.getCapacityPerStudyArm());

		// Test subject list statistics
		var subjectListStatistics = result.getSubjectListStatistics();
		assertEquals(1, subjectListStatistics.size());
		var subjectListStatistic = subjectListStatistics.get(0);
		assertEquals(0, subjectListStatistic.getStratumParts().size());
		assertEquals(3, subjectListStatistic.getNumberSubjects());
		assertEquals(10, subjectListStatistic.getCapacity());
		assertEquals(5, subjectListStatistic.getCapacityPerStudyArm());

		assertEquals(2, subjectListStatistic.getNumberSubjectsPerStudyArm().size());
		assertEquals(INTERVENTION_SUBJECTS, subjectListStatistic.getNumberSubjectsPerStudyArm().get(firstArm.getApiId()));
		assertEquals(CONTROL_SUBJECTS, subjectListStatistic.getNumberSubjectsPerStudyArm().get(secondArm.getApiId()));
	}

	@Test
	void createSubjectSeriesStatistics() {
		Study study = new Study();
		study.setStatus(StudyStatus.ACTIVE);
		study.setCapacity(10);

		var oneDayAgo = LocalDate.now(ZoneId.systemDefault()).minusDays(1);
		var threeDaysAgo = LocalDate.now(ZoneId.systemDefault()).minusDays(3);
		var fiveDaysAgo = LocalDateTime.now(ZoneId.systemDefault()).minusDays(5);
		study.setActivationDate(Timestamp.valueOf(fiveDaysAgo));

		var result = assertDoesNotThrow(
				() -> statisticsService.createSubjectSeriesStatistics(study, new SubjectSeriesParameterDTO()));

		assertEquals(threeDaysAgo, result.getStart(), "Unexpected start date");
		assertEquals(oneDayAgo, result.getEnd(), "Unexpected end date");
		assertArrayEquals(new int[]{1, 1, 3}, result.getSeries(), "Unexpected series");
		assertEquals(10, result.getTarget(), "Unexpected target");
	}

	@Test
	void createSubjectSeriesStatisticsForSite() {
		var oneDayAgo = LocalDate.now(ZoneId.systemDefault()).minusDays(1);
		var threeDaysAgo = LocalDate.now(ZoneId.systemDefault()).minusDays(3);
		var fiveDaysAgo = LocalDateTime.now(ZoneId.systemDefault()).minusDays(5);
		study.setActivationDate(Timestamp.valueOf(fiveDaysAgo));

		var parameter = new SubjectSeriesParameterDTO();
		parameter.setSiteApiId(study.getSites().get(0).getApiId());
		var result = assertDoesNotThrow(() -> statisticsService.createSubjectSeriesStatistics(study, parameter));

		assertEquals(threeDaysAgo, result.getStart(), "Unexpected start date");
		assertEquals(oneDayAgo, result.getEnd(), "Unexpected end date");
		assertArrayEquals(new int[]{1, 1, 2}, result.getSeries(), "Unexpected series");
		assertEquals(10, result.getTarget(), "Unexpected target");
		assertNotNull(result.getSite(), "Site not set");
		assertEquals("muenster", result.getSite().getApiId(), "Unexpected site");
	}

	private Study createStudy() {
		var study = new Study();
		study.setCapacity(10);
		study.setStatus(StudyStatus.ACTIVE);

		var muenster = new Site();
		muenster.setApiId("muenster");
		muenster.setCapacity(10);
		study.addSite(muenster);

		var intervention = new StudyArm();
		intervention.setId(0);
		intervention.setGuiName("Intervention");
		intervention.setApiId("Intervention");
		study.addStudyArm(intervention);

		var control = new StudyArm();
		control.setId(1);
		control.setGuiName("Control");
		control.setApiId("Control");
		study.addStudyArm(control);

		Subject firstSubject = new Subject();
		Subject secondSubject = new Subject();
		Subject thirdSubject = new Subject();
		List<Subject> subjectList = List.of(firstSubject, secondSubject, thirdSubject);
		var list = new SubjectList();
		list.addAllRandomizationEntries(subjectList);
		study.addSubjectList(list);

		return study;
	}

}
