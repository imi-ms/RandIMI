package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class SubjectCustomRepositoryImpl implements SubjectCustomRepository {

	private final SubjectCrudRepository subjectCrudRepository;

	public SubjectCustomRepositoryImpl(
			final SubjectCrudRepository subjectCrudRepository
	) {
		this.subjectCrudRepository = subjectCrudRepository;
	}

	@Override
	public boolean isEntryInStudy(Long entryId, Long studyId) {
		return subjectCrudRepository.findFirstByIdAndSubjectListStudyId(entryId, studyId) != null;
	}

	@Override
	public long countBlockingSubjectInSubjectList(Long subjectListId) {
		return subjectCrudRepository.countBySubjectListIdAndStatusIn(subjectListId, SubjectStatus.BLOCKING_STATUS);
	}


	@Override
	public long countBlockingSubjectInSubjectListAndSite(Long subjectListId, long siteId) {
		return subjectCrudRepository.countBySubjectListIdAndSiteIdAndStatusIn(
				subjectListId, siteId, SubjectStatus.BLOCKING_STATUS);
	}

	@Override
	public long countBlockingSubjectInSubjectListAndStudyArm(long subjectListId, long studyArmId) {
		return subjectCrudRepository.countBySubjectListIdAndStudyArmIdAndStatusIn(
				subjectListId, studyArmId, SubjectStatus.BLOCKING_STATUS);
	}

	@Override
	public long countBlockingSubjectInStudy(long studyId) {
		return subjectCrudRepository.countBySubjectListStudyIdAndStatusIn(studyId, SubjectStatus.BLOCKING_STATUS);
	}

	@Override
	public long countBlockingSubjectInStudyAndSite(long studyId, long siteId) {
		return subjectCrudRepository.countBySubjectListStudyIdAndSiteIdAndStatusIn(
				studyId, siteId, SubjectStatus.BLOCKING_STATUS);
	}

	@Override
	public long countBlockingSubjectInStudyAndSiteAndStudyArm(long studyId, long siteId, long studyArmId) {
		return subjectCrudRepository.countBySubjectListStudyIdAndSiteIdAndStudyArmIdAndStatusIn(
				studyId, siteId, studyArmId, SubjectStatus.BLOCKING_STATUS);
	}

	@Override
	public long countBlockingSubjectForStudyArmAndStratumPart(long studyArmId, long stratumPartId) {
		return subjectCrudRepository.countByStudyArmIdAndSubjectListStratumPartsIdAndStatusIn(
				studyArmId, stratumPartId, SubjectStatus.BLOCKING_STATUS);
	}
}
