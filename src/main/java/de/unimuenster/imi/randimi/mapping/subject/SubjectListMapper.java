package de.unimuenster.imi.randimi.mapping.subject;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectEntryDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectListDTO;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumPartMapper;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;

import de.unimuenster.imi.randimi.service.StratumCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SubjectListMapper {

    private final SubjectEntryMapper subjectEntryMapper;
    private final StratumCodeService stratumCodeService;
    private final StratumPartMapper stratumPartMapper;

    @Autowired
    public SubjectListMapper(final SubjectEntryMapper subjectEntryMapper, final StratumCodeService stratumCodeService,
                             final StratumPartMapper stratumPartMapper) {
        this.subjectEntryMapper = subjectEntryMapper;
        this.stratumCodeService = stratumCodeService;
        this.stratumPartMapper = stratumPartMapper;
    }

    /**
     * Converts this {@link SubjectList} object to an {@link SubjectListDTO} object without subjects.
     *
     * @return An {@link SubjectListDTO} object based on this {@link SubjectList} object without subjects.
     */
    public SubjectListDTO toSimpleSubjectListDTO(SubjectList model) {
        SubjectListDTO subjectListDTO = new SubjectListDTO();

        subjectListDTO.setId(model.getId());
        subjectListDTO.setStudyId(model.getStudy().getId());

        final List<StratumPartBaseDTO> stratumParts = new ArrayList<>();
        for(final StratumPartBase stratumPartBase : model.getStratumParts()) {
            stratumParts.add(stratumPartMapper.toStratumPartBaseDTO(stratumPartBase));
        }
        subjectListDTO.setStratumParts(stratumParts);

        return subjectListDTO;
    }

    /**
     * Converts this {@link SubjectList} object to an
     * {@link SubjectListDTO} object.
     *
     * @return An {@link SubjectListDTO} object based on this
     * {@link SubjectList} object.
     */
    public SubjectListDTO toSubjectListDTO(SubjectList model) {
        SubjectListDTO subjectListDTO = toSimpleSubjectListDTO(model);

        List<SubjectEntryDTO> subjectEntryDTOS = new ArrayList<>();
        for (Subject entryModel : model.getSubjects()) {
            if (entryModel.getPseudonym() == null) {
                // Uncomment to display pre-generated subjects
//                SubjectEntryDTO subjectEntryDTO = new SubjectEntryDTO();
//                subjectEntryDTO.setStatus(entryModel.getStatus());
//                subjectEntryDTO.setOrderNumber(entryModel.getOrderNumber());
//                subjectEntryDTO.setStudyArmName(entryModel.getStudyArm().getGuiName());
//                subjectEntryDTOS.add(subjectEntryDTO);
                continue;
            }
            SubjectEntryDTO entryDto = subjectEntryMapper.toSubjectEntryDTO(entryModel);
            subjectEntryDTOS.add(entryDto);
        }
        subjectListDTO.setSubjectEntries(subjectEntryDTOS);

        return subjectListDTO;
    }
}
