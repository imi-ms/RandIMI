package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.subject.Subject;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SubjectCrudRepository extends CrudRepository<Subject, Long> {

	long countBySubjectListIdAndStatusIn(Long subjectListId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListIdAndStatusNot(Long subjectListId, SubjectStatus subjectStatus);

	long countBySubjectListIdAndSiteIdAndStatusIn(
			Long subjectListId, long siteId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListIdAndStudyArmIdAndStatusIn(
			long subjectListId, long studyArmId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndSiteIdAndStatusIn(
			long studyId, long siteId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndSiteIdAndStudyArmIdAndStatusIn(
			long studyId, long siteId, long studyArmId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndStatusIn(long studyId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(
			long studyId, long siteId, SubjectStatus subjectStatus);

	long countBySubjectListStudyIdAndPseudonym(long studyId, String pseudonym);

	long countBySubjectListStudyIdAndPseudonymAndSiteId(long studyId, String pseudonym, long siteId);

	long countByStudyArmIdAndSubjectListStratumPartsIdAndStatusIn(
			long studyArmId, long stratumPartId, Collection<SubjectStatus> subjectStatusList);

	@Nullable
	Subject findFirstByIdAndSubjectListStudyId(Long id, Long studyId);

	Optional<Subject> findFirstBySubjectListStudyIdAndPseudonym(long studyId, String pseudonym);

	Optional<Subject> findFirstByPseudonymAndSiteIdAndSubjectListStudyId(String pseudonym, long siteId, Long studyId);

	Optional<Subject> findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId(
			String pseudonym, String siteApiId, Long studyId);

	List<Subject> findBySubjectListIdAndStatusNot(Long subjectListId, SubjectStatus status);

	List<Subject> findBySubjectListStudyIdAndStatusNot(Long studyId, SubjectStatus status);

	List<Subject> findBySubjectListStudyIdAndSiteApiIdAndStatusNot(
			Long studyId, String siteApiId, SubjectStatus status);

	Optional<Subject> findFirstBySubjectListStudyIdAndStatusNotOrderByRandomizationTimestampAsc(
			Long studyId, SubjectStatus status);

	@Query("select s from Subject s where s.subjectList.study.id = ?1 and s.status != ?2 order by greatest(s.randomizationTimestamp, s.releaseTimestamp) desc limit 1")
	Optional<Subject> findFirstBySubjectListStudyIdAndStatusNotOrderByGreatestDesc(Long studyId, SubjectStatus status);

	@Query("select count(*) from (select count(*) as count from Subject s where s.subjectList.study.id = ?1 group by s.pseudonym having count(*) > 1)")
	long countDuplicatePseudonyms(Long studyId);

}
