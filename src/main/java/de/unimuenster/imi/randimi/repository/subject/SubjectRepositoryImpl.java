package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class SubjectRepositoryImpl {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	@Lazy
	private SubjectRepository subjectRepository;

	public boolean isEntryInStudy(Long entryId, Long studyId) {
		return subjectRepository.findFirstByIdAndSubjectListStudyId(entryId, studyId) != null;
	}

	public long countBlockingSubjectInSubjectList(Long subjectListId) {
		return subjectRepository.countBySubjectListIdAndStatusIn(subjectListId, SubjectStatus.BLOCKING_STATUS);
	}


	public long countBlockingSubjectInSubjectListAndSite(Long subjectListId, long siteId) {
		return subjectRepository.countBySubjectListIdAndSiteIdAndStatusIn(subjectListId, siteId,
		                                                                  SubjectStatus.BLOCKING_STATUS);
	}

	/**
	 * Implementation for {@link SubjectRepository#countBlockingSubjectInSubjectListAndStudyArm(long, long)}.
	 */
	public long countBlockingSubjectInSubjectListAndStudyArm(long subjectListId, long studyArmId) {
		return subjectRepository.countBySubjectListIdAndStudyArmIdAndStatusIn(subjectListId, studyArmId, SubjectStatus.BLOCKING_STATUS);
	}

	public long countBlockingSubjectInStudy(long studyId) {
		return subjectRepository.countBySubjectListStudyIdAndStatusIn(studyId, SubjectStatus.BLOCKING_STATUS);
	}

	public long countBlockingSubjectInStudyAndSite(long studyId, long siteId) {
		return subjectRepository.countBySubjectListStudyIdAndSiteIdAndStatusIn(studyId, siteId,
		                                                                       SubjectStatus.BLOCKING_STATUS);
	}

	/**
	 * Implementation for {@link SubjectRepository#countBlockingSubjectInStudyAndSiteAndStudyArm(long, long, long)}.
	 */
	public long countBlockingSubjectInStudyAndSiteAndStudyArm(long studyId, long siteId, long studyArmId) {
		return subjectRepository.countBySubjectListStudyIdAndSiteIdAndStudyArmIdAndStatusIn(studyId, siteId, studyArmId,
		                                                                                    SubjectStatus.BLOCKING_STATUS);
	}

	/**
	 * Implementation for {@link SubjectRepository#countBlockingSubjectForStudyArmAndStratumPart(long, long)}
	 */
	public long countBlockingSubjectForStudyArmAndStratumPart(long studyArmId, long stratumPartId) {
		return subjectRepository.countByStudyArmIdAndSubjectListStratumPartsIdAndStatusIn(studyArmId, stratumPartId,
		                                                                                  SubjectStatus.BLOCKING_STATUS);
	}

}
