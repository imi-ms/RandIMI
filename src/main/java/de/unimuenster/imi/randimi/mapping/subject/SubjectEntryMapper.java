package de.unimuenster.imi.randimi.mapping.subject;

import de.unimuenster.imi.randimi.dto.subject.SubjectEntryDTO;
import de.unimuenster.imi.randimi.mapping.study.SiteMapper;
import de.unimuenster.imi.randimi.mapping.study.StudyArmMapper;
import de.unimuenster.imi.randimi.model.api.SubjectResource;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.model.subject.Subject;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SubjectEntryMapper {

    private final SiteMapper siteMapper;
    private final StudyArmMapper studyArmMapper;

    public SubjectEntryMapper(final SiteMapper siteMapper, final StudyArmMapper studyArmMapper) {
        this.siteMapper = siteMapper;
        this.studyArmMapper = studyArmMapper;
    }

    /**
     * Converts this {@link Subject} object to an
     * {@link SubjectEntryDTO} object.
     *
     * @return An {@link SubjectEntryDTO} object based on this
     * {@link Subject} object.
     */
    public SubjectEntryDTO toSubjectEntryDTO(Subject model) {
        SubjectEntryDTO subjectEntryDTO = new SubjectEntryDTO();

        subjectEntryDTO.setId(model.getId());
        subjectEntryDTO.setRandomizationListId(model.getSubjectList().getId());
        subjectEntryDTO.setOrderNumber(model.getOrderNumber());
        subjectEntryDTO.setStudyArmName(model.getStudyArm().getGuiName());
        subjectEntryDTO.setStudyArmApiId(model.getStudyArm().getApiId());
        subjectEntryDTO.setRandomizationTimestamp(model.getRandomizationTimestamp());
        subjectEntryDTO.setDeletionTimestamp(model.getDeletionTimestamp());
        subjectEntryDTO.setReleaseTimestamp(model.getReleaseTimestamp());
        subjectEntryDTO.setPseudonym(model.getPseudonym());
        subjectEntryDTO.setStatus(model.getStatus());

        if (model.getSite() != null) {
            subjectEntryDTO.setLocation(model.getSite().getGuiName());
            subjectEntryDTO.setSiteId(model.getSite().getId());
            subjectEntryDTO.setLocationApiId(model.getSite().getApiId());
        }

        return subjectEntryDTO;
    }

    /**
     * Converts a {@link Subject} to a {@link SubjectResource}.
     *
     * @param subject The subject to convert.
     * @return The converted subject resource.
     */
    public SubjectResource toSubjectResource(final Subject subject) {
        final var studyArm = studyArmMapper.toStudyArmResource(subject.getStudyArm());
        final var site = siteMapper.toSiteResource(subject.getSite());

        final Map<String, String> stratificationParameter = new HashMap<>();
        for (final StratumPartBase part : subject.getSubjectList().getStratumParts()) {
            if (part instanceof StratumPartEnumeration partEnumeration) {
                stratificationParameter.put(part.getStratum().getApiId(), partEnumeration.getApiId());
            }
        }

        return new SubjectResource(subject.getOrderNumber(), studyArm, subject.getRandomizationTimestamp(),
                                   subject.getPseudonym(), site, stratificationParameter, subject.getStatus());
    }

}
