package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.subject.Subject;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Data access object used for the RandomizationEntry class.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SubjectRepository extends CrudRepository<Subject, Long> {

	long countBySubjectListIdAndStatusIn(Long subjectListId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListIdAndStatusNot(Long subjectListId, SubjectStatus subjectStatus);

	long countBySubjectListIdAndSiteIdAndStatusIn(Long subjectListId, long siteId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListIdAndStudyArmIdAndStatusIn(long subjectListId, long studyArmId,
	                                                  List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndSiteIdAndStatusIn(long studyId, long siteId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndSiteIdAndStudyArmIdAndStatusIn(long studyId, long siteId, long studyArmId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndStatusIn(long studyId, List<SubjectStatus> subjectStatusList);

	long countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(long studyId, long siteId, SubjectStatus subjectStatus);

	long countBySubjectListStudyIdAndPseudonym(long studyId, String pseudonym);

	long countBySubjectListStudyIdAndPseudonymAndSiteId(long studyId, String pseudonym, long siteId);

	long countByStudyArmIdAndSubjectListStratumPartsIdAndStatusIn(long studyArmId, long stratumPartId, Collection<SubjectStatus> subjectStatusList);

	Subject findFirstByIdAndSubjectListStudyId(Long id, Long studyId);

	Optional<Subject> findFirstBySubjectListStudyIdAndPseudonym(long studyId, String pseudonym);

	Optional<Subject> findFirstByPseudonymAndSiteIdAndSubjectListStudyId(String pseudonym, long siteId, Long studyId);

	Optional<Subject> findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId(String pseudonym, String siteApiId, Long studyId);

	List<Subject> findBySubjectListIdAndStatusNot(Long subjectListId, SubjectStatus status);

	List<Subject> findBySubjectListStudyIdAndStatusNot(Long studyId, SubjectStatus status);

	List<Subject> findBySubjectListStudyIdAndSiteApiIdAndStatusNot(Long studyId, String siteApiId, SubjectStatus status);

	Optional<Subject> findFirstBySubjectListStudyIdAndStatusNotOrderByRandomizationTimestampAsc(Long studyId, SubjectStatus status);

	@Query("select s from Subject s where s.subjectList.study.id = ?1 and s.status != ?2 order by greatest(s.randomizationTimestamp, s.releaseTimestamp) desc limit 1")
	Optional<Subject> findFirstBySubjectListStudyIdAndStatusNotOrderByGreatestDesc(Long studyId, SubjectStatus status);

	@Query("select count(*) from (select count(*) as count from Subject s where s.subjectList.study.id = ?1 group by s.pseudonym having count(*) > 1)")
	long countDuplicatePseudonyms(Long studyId);

	boolean isEntryInStudy(Long entryId, Long studyId);

	long countBlockingSubjectInSubjectList(Long subjectListId);

	long countBlockingSubjectInSubjectListAndSite(Long subjectListId, long siteId);
	/**
	 * Counts the number of blocking subjects in a subject list assigned to the given study arm.
	 * Implemented by {@link SubjectRepositoryImpl#countBlockingSubjectInSubjectListAndStudyArm(long, long)}.
	 *
	 * @param subjectListId The ID of the subject list.
	 * @param studyArmId    The ID of the study arm.
	 * @return The number of blocking subjects in the subject list assigned to the given study arm.
	 */
	long countBlockingSubjectInSubjectListAndStudyArm(long subjectListId, long studyArmId);

	long countBlockingSubjectInStudy(long studyId);

	long countBlockingSubjectInStudyAndSite(long studyId, long siteId);

	/**
	 * Counts the number of blocking subjects in the study from the given site assigned to the given study arm.
	 * Implemented by {@link SubjectRepositoryImpl#countBlockingSubjectInStudyAndSiteAndStudyArm(long, long, long)}.
	 *
	 * @param studyId    The ID of the study.
	 * @param siteId     The ID of the site.
	 * @param studyArmId The ID of the study arm.
	 * @return The number of blocking subjects in the study from the given site assigned to the given study arm.
	 */
	long countBlockingSubjectInStudyAndSiteAndStudyArm(long studyId, long siteId, long studyArmId);

	/**
	 * Counts the number of subjects assigned to the given study arm and with the given stratum part.
	 * Implemented by {@link SubjectRepositoryImpl#countBlockingSubjectForStudyArmAndStratumPart(long, long)}
	 *
	 * @param studyArmId    The ID of the study arm.
	 * @param stratumPartId The ID of the stratum part.
	 * @return The number of blocking subjects.
	 */
	long countBlockingSubjectForStudyArmAndStratumPart(long studyArmId, long stratumPartId);

}
