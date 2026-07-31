package de.unimuenster.imi.randimi.mapping.study.stratum;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.mapping.study.NamesMapper;
import de.unimuenster.imi.randimi.model.api.StrataInfoResponseV2;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StratumMapper {

    private final NamesMapper namesMapper;
    private final StratumPartMapper stratumPartMapper;

    @Autowired
    public StratumMapper(NamesMapper namesMapper, StratumPartMapper stratumPartMapper) {
	    this.namesMapper = namesMapper;
	    this.stratumPartMapper = stratumPartMapper;
    }

    /**
     * Overrides the corresponding stratum of the given StratumDTO in an active study.
     * Fields that may not be overwritten in an active study will not.
     *
     * @param stratumDTO The DTO with the value to write.
     * @param study The study.
     */
    public void toStratum(final StratumDTO stratumDTO, final Study study) {
        study.getStratums().stream()
             .filter(s -> s.getId() == stratumDTO.getId())
             .findFirst()
             .ifPresent(stratum -> {
                 namesMapper.toNamedEntity(stratumDTO, stratum);

                 for (StratumPartBaseDTO stratumPartBaseDTO : stratumDTO.getStratumParts()) {
                     stratumPartMapper.toStratumPartBase(stratumPartBaseDTO, stratum);
                 }
             });
    }

    public Stratum toStratum(StratumDTO dto, Study study, int orderNumber) {
        final Stratum stratum;
        if (dto.getId() != 0) {
            stratum = study.getStratums().stream()
                           .filter(s -> s.getId() == dto.getId())
                           .findFirst()
                           .orElseGet(Stratum::new);
        } else {
            stratum = new Stratum();
        }
        return toStratum(dto, stratum, study, orderNumber);
    }

    /**
     * Overrides the given {@link Stratum} with the values of the {@link StratumDTO}.
     * The study must have already converted sites for the conversion of a {@link StratumPartSite}.
     *
     * @param dto The DTO to convert.
     * @param stratum The stratum to override.
     * @param study The parent study.
     * @param orderNumber The order number for the converted stratum.
     * @return The converted stratum.
     */
    public Stratum toStratum(final StratumDTO dto, final Stratum stratum, final Study study, final int orderNumber) {
        stratum.setId(dto.getId());
        stratum.setStudy(study);
        stratum.setStratumType(dto.getStratumType());

        namesMapper.toNamedEntity(dto, stratum);

        stratum.setOrderNumber(orderNumber);

        List<StratumPartBase> stratumParts = new ArrayList<>();
        for (StratumPartBaseDTO stratumPartBaseDTO : dto.getStratumParts()) {
            stratumParts.add(stratumPartMapper.toStratumPartBase(stratumPartBaseDTO, stratum, stratumParts.size()));
        }
        stratum.getStratumParts().clear();
        stratum.addAllStratumParts(stratumParts);

        return stratum;
    }

    /**
     * Converts this {@link Stratum} object to an {@link StratumDTO} object.
     *
     * @return An {@link StratumDTO} object based on this {@link Stratum} object.
     */
    public StratumDTO toStratumDTO(Stratum stratum) {
        StratumDTO stratumDTO = new StratumDTO();

        stratumDTO.setId(stratum.getId());
        stratumDTO.setStratumType(stratum.getStratumType());
        namesMapper.toNamesDTO(stratum, stratumDTO);
        stratumDTO.setStudyId(stratum.getStudy().getId());
        stratumDTO.setOrderNumber(stratum.getOrderNumber());
        List<StratumPartBaseDTO> stratumPartBaseDTOs = new ArrayList<>();
        for(StratumPartBase stratumPartBase : stratum.getStratumParts()) {
            stratumPartBaseDTOs.add(stratumPartMapper.toStratumPartBaseDTO(stratumPartBase));
        }
        stratumDTO.setStratumParts(stratumPartBaseDTOs);

        return stratumDTO;
    }

    /**
     * Converts a {@link Stratum} object to a {@link StrataInfoResponseV2.Definition} object.
     *
     * @param stratum The {@link Stratum} object to convert.
     * @return The converted {@link StrataInfoResponseV2.Definition} object.
     */
    public StrataInfoResponseV2.Definition toStratumResource(final Stratum stratum) {
        switch (stratum.getStratumType()) {
            case ENUM: {
                List<StrataInfoResponseV2.FactorValue> values = new ArrayList<>();
                stratum.getStratumParts().stream().map(partBase -> (StratumPartEnumeration) partBase)
                       .forEach(partEnum -> values.add(
                               new StrataInfoResponseV2.FactorValue(partEnum.getName(), partEnum.getApiId())));
                return new StrataInfoResponseV2.FactorDefinition(stratum.getName(), stratum.getApiId(), values);
            }

//            case INTERVAL: {
//                List<RandimiResponse.IntervalPart> values = new ArrayList<>();
//                stratum.getStratumParts().stream().map(base -> (StratumPartInterval) base)
//                       .forEach(part -> values.add(
//                               new RandimiResponse.IntervalPart(part.getIntervalBegin(), part.getIntervalEnd())));
//                stratums.add(new RandimiResponse.IntervalDefinition(stratum.getName(), values));
//            }

        }

        return null;
    }
}
