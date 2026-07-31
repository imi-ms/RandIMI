package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Preciado-Marquez
 */
public class SubjectRepositoryTest extends RepositoryTestBase {

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	StratumCodeService stratumCodeService;

	@Test
	public void countBySubjectListIdAndLocationAndStatusInTest() {
		long count = subjectRepository.countBySubjectListIdAndSiteIdAndStatusIn(
				getSubjectList().getId(), activeStudy.getSites().get(0).getId(),
				List.of(SubjectStatus.ACTIVE, SubjectStatus.DELETED));
		assertEquals(1, count, "Number of subjects in the list with location is wrong!");

		count = subjectRepository.countBySubjectListIdAndSiteIdAndStatusIn(
				getPreGeneratedSubjectList().getId(), preGenereatedStudy.getSites().get(0).getId(),
				List.of(SubjectStatus.ACTIVE, SubjectStatus.DELETED));
		assertEquals(1, count, "Number of subjects in the list with location is wrong!");
	}

	@Test
	public void countBlockingSubjectsInSubjectListAndSiteTest() {
		long count = subjectRepository.countBlockingSubjectInSubjectListAndSite(
				getSubjectList().getId(), activeStudy.getSites().get(0).getId());
		assertEquals(1, count, "Number of subjects in the list with location is wrong!");

		count = subjectRepository.countBlockingSubjectInSubjectListAndSite(
				getPreGeneratedSubjectList().getId(), preGenereatedStudy.getSites().get(0).getId());
		assertEquals(1, count, "Number of subjects in the list with location is wrong!");
	}

	@Test
	public void countBlockingSubjectInSubjectListTets() {
		long count = subjectRepository.countBlockingSubjectInSubjectList(getSubjectList().getId());
		assertEquals(2, count, "Number of subjects in the list with location is wrong!");

		count = subjectRepository.countBlockingSubjectInSubjectList(getPreGeneratedSubjectList().getId());
		assertEquals(1, count);
	}

	@Test
	public void countBySubjectListStudyIdAndLocationAndStatusTest() {
		long count = subjectRepository.countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(
				activeStudy.getId(), activeStudy.getSites().get(0).getId(), SubjectStatus.ACTIVE);
		assertEquals(1, count, "Number of subjects in study and site is wrong");

		count = subjectRepository.countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(
				preGenereatedStudy.getId(), preGenereatedStudy.getSites().get(0).getId(), SubjectStatus.ACTIVE);
		assertEquals(1, count, "Number of subjects in study and site is wrong");
	}

	@Test
	public void countBlockingSubjectInStudyAndSiteTest() {
		long count = subjectRepository.countBlockingSubjectInStudyAndSite(activeStudy.getId(),
		                                                                  activeStudy.getSites().get(0).getId());
		assertEquals(1, count, "Number of subjects in study and site is wrong");

		count = subjectRepository.countBlockingSubjectInStudyAndSite(preGenereatedStudy.getId(),
		                                                             preGenereatedStudy.getSites().get(0).getId());
		assertEquals(1, count, "Number of subjects in study and site is wrong");
	}

	@Test
	public void countBySubjectListStudyIdAndPseudonymTest() {
		long count = subjectRepository.countBySubjectListStudyIdAndPseudonym(activeStudy.getId(), "pseudonym1");
		assertEquals(1, count, "Number of subjects in study wrong!");

		count = subjectRepository.countBySubjectListStudyIdAndPseudonym(preGenereatedStudy.getId(), "pseudonym1");
		assertEquals(1, count, "Number of subjects in study wrong!");
	}

	@Test
	public void countBySubjectListStudyIdAndPseudonymAndSiteTest() {
		long count = subjectRepository.countBySubjectListStudyIdAndPseudonymAndSiteId(
				activeStudy.getId(), "pseudonym1", activeStudy.getSites().get(0).getId());
		assertEquals(1, count, "Number of subjects in site wrong!");

		count = subjectRepository.countBySubjectListStudyIdAndPseudonymAndSiteId(
				activeStudy.getId(), "pseudonym1", activeStudy.getSites().get(1).getId());
		assertEquals(0, count, "Number of subjects in site wrong!");


		count = subjectRepository.countBySubjectListStudyIdAndPseudonymAndSiteId(
				preGenereatedStudy.getId(), "pseudonym1", preGenereatedStudy.getSites().get(0).getId());
		assertEquals(1, count, "Number of subjects in site wrong!");
	}

	@Test
	public void countBlockingSubjectInStudy() {
		long count = subjectRepository.countBlockingSubjectInStudy(activeStudy.getId());
		assertEquals(2, count, "Number of subjects in study wrong!");

		count = subjectRepository.countBlockingSubjectInStudy(preGenereatedStudy.getId());
		assertEquals(1, count, "Number of subjects in study wrong!");
	}

	@Test
	public void isEntryInStudyTest() {
		Optional<Subject> entryO = subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(
				"pseudonym1", activeStudy.getSites().get(0).getId(), activeStudy.getId());

		assertTrue(entryO.isPresent(), "Randomization entry not found!");
		Subject entry = entryO.get();

		boolean contains = subjectRepository.isEntryInStudy(entry.getId(), activeStudy.getId());
		assertTrue(contains, "Entry not found in study!");

		// Id is because of the hibernate sequence definitely wrong
		contains = subjectRepository.isEntryInStudy(0L, activeStudy.getId());
		assertFalse(contains, "Entry found in not existing study!");

		// Id is because of the hibernate sequence definitely wrong
		contains = subjectRepository.isEntryInStudy(entry.getId(), 0L);
		assertFalse(contains, "Not existing entry found in study!");
	}

	@Test
	public void findFirstByPseudonymAndSiteIdAndSubjectListStudyIdTest() {
		Optional<Subject> entry = subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(
				"pseudonym1", activeStudy.getSites().get(0).getId(), activeStudy.getId());

		assertTrue(entry.isPresent(), "Randomization entry not found!");
		assertEquals("pseudonym1", entry.get().getPseudonym(), "Found wrong pseudonymization entry!");

		entry = subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(
				"", activeStudy.getSites().get(0).getId(), activeStudy.getId());
		assertTrue(entry.isEmpty(), "Found not existing randomization entry!");

		entry = subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(
				"pseudonym1", 0, activeStudy.getId());
		assertTrue(entry.isEmpty(), "Found not existing randomization entry!");

		// ID is because of the hibernate sequence definitely wrong
		entry = subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(
				"pseudonym1", activeStudy.getSites().get(0).getId(), 0L);
		assertTrue(entry.isEmpty(), "Found not existing randomization entry!");
	}

	@Test
	public void findFirstByPseudonymAndSiteApiIdAndSubjectListStudyIdTest() {
		Subject entry = subjectRepository.findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId("pseudonym1",
		                                                                                        activeStudy.getSites()
		                                                                                                   .get(0)
		                                                                                                   .getApiId(),
		                                                                                        activeStudy.getId())
		                                 .orElse(null);
		assertNotNull(entry, "Randomization entry not found!");
		assertEquals("pseudonym1", entry.getPseudonym(), "Found wrong pseudonymization entry!");

		entry = subjectRepository.findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId("",
		                                                                                activeStudy.getSites().get(0)
		                                                                                           .getApiId(),
		                                                                                activeStudy.getId())
		                         .orElse(null);

		assertNull(entry, "Found not existing randimization entry!");

		entry = subjectRepository.findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId("pseudonym1", "0",
		                                                                                activeStudy.getId())
		                         .orElse(null);
		assertNull(entry, "Found not existing randimization entry!");

		// Id is because of the hibernate sequence definitely wrong
		entry = subjectRepository.findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId("pseudonym1",
		                                                                                activeStudy.getSites().get(0)
		                                                                                           .getApiId(), 0L)
		                         .orElse(null);
		assertNull(entry, "Found not existing randimization entry!");
	}

	private SubjectList getSubjectList() {
		final StratumPartBase genderM = activeStudy.getStratums().get(0).getStratumParts().get(0);
		final StratumPartBase ageGroup0 = activeStudy.getStratums().get(1).getStratumParts().get(0);
		return stratumCodeService.getSubjectListForParts(List.of(genderM, ageGroup0), activeStudy).get();
	}

	private SubjectList getPreGeneratedSubjectList() {
		return preGenereatedStudy.getSubjectLists().get(0);
	}

}
