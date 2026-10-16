package de.unimuenster.imi.randimi.repository.subject;

/**
 * @author Daniel Preciado-Marquez
 */
public interface SubjectCustomRepository {

	boolean isEntryInStudy(Long entryId, Long studyId);

	long countBlockingSubjectInSubjectList(Long subjectListId);

	long countBlockingSubjectInSubjectListAndSite(Long subjectListId, long siteId);
	/**
	 * Counts the number of blocking subjects in a subject list assigned to the given study arm.
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
	 *
	 * @param studyId    The ID of the study.
	 * @param siteId     The ID of the site.
	 * @param studyArmId The ID of the study arm.
	 * @return The number of blocking subjects in the study from the given site assigned to the given study arm.
	 */
	long countBlockingSubjectInStudyAndSiteAndStudyArm(long studyId, long siteId, long studyArmId);

	/**
	 * Counts the number of subjects assigned to the given study arm and with the given stratum part.
	 *
	 * @param studyArmId    The ID of the study arm.
	 * @param stratumPartId The ID of the stratum part.
	 * @return The number of blocking subjects.
	 */
	long countBlockingSubjectForStudyArmAndStratumPart(long studyArmId, long stratumPartId);

}
