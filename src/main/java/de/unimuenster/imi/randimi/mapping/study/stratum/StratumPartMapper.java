package de.unimuenster.imi.randimi.mapping.study.stratum;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.mapping.study.SiteMapper;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.stratum.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StratumPartMapper {

	private final SiteMapper siteMapper;

	@Autowired
	public StratumPartMapper(final SiteMapper siteMapper) {
		this.siteMapper = siteMapper;
	}

	//========================
	//-- From entity to DTO --
	//========================

	/**
	 * Converts an implementation of {@link StratumPartBase} to a {@link StratumPartBaseDTO}.
	 *
	 * @param stratumPartBase The stratum part to convert.
	 * @return The converted DTO.
	 */
	public StratumPartBaseDTO toStratumPartBaseDTO(final StratumPartBase stratumPartBase) {
		final StratumPartBaseDTO stratumPartBaseDTO = new StratumPartBaseDTO();

		stratumPartBaseDTO.setId(stratumPartBase.getId());
		stratumPartBaseDTO.setStratumId(stratumPartBase.getStratum().getId());
		stratumPartBaseDTO.setOrderNumber(stratumPartBase.getOrderNumber());

		if (stratumPartBase instanceof StratumPartInterval) {
			setIntervalFields(stratumPartBaseDTO, (StratumPartInterval) stratumPartBase);
		} else if (stratumPartBase instanceof StratumPartEnumeration) {
			setEnumerationFields(stratumPartBaseDTO, (StratumPartEnumeration) stratumPartBase);
		} else if (stratumPartBase instanceof StratumPartSite) {
			setSiteFields(stratumPartBaseDTO, (StratumPartSite) stratumPartBase);
		} else {
			throw new IllegalArgumentException(
					"Unknown subclass of StratumPartBase encountered: " + stratumPartBase.getClass().getName());
		}

		return stratumPartBaseDTO;
	}

	//========================
	//-- From DTO to entity --
	//========================

	/**
	 * Overrides the given Stratum with the values of the given DTO.
	 * Only values that are allowed to be overwritten in an active study.
	 *
	 * @param dto The DTO with the new values.
	 * @param stratum The existing Stratum.
	 */
	public void toStratumPartBase(final StratumPartBaseDTO dto, final Stratum stratum) {
		stratum.getStratumParts().stream()
		       .filter(part -> part.getId() == dto.getId())
		       .findFirst()
		       .ifPresent(part -> {
			       if (stratum.getStratumType() == StratumType.ENUM) {
				       toStratumPartEnumeration(dto, part);
			       }
		       });
	}

	/**
	 * Converts a {@link StratumPartBaseDTO} to a {@link StratumPartBase} implementation
	 * based on the value of the {@link StratumType} of the given {@link Stratum}.
	 * The stratum must have a valid study with already converted sites for the conversion of a {@link StratumPartSite}.
	 *
	 * @param dto The DTO to transform.
	 * @param stratum The parent Stratum.
	 * @param orderNumber Order number of the converted StratumPart.
	 * @return The converted stratum part.
	 */
	public StratumPartBase toStratumPartBase(final StratumPartBaseDTO dto, final Stratum stratum,
	                                         final int orderNumber) {
		StratumPartBase originalEntity = null;

		if (dto.getId() != 0) {
			originalEntity = stratum.getStratumParts().stream()
			                        .filter(part -> part.getId() == dto.getId())
			                        .findFirst().orElse(null);
		}

		final StratumPartBase entity = switch (stratum.getStratumType()) {
			case ENUM -> toStratumPartEnumeration(dto, originalEntity);
			case INTERVAL -> toStratumPartInterval(dto, originalEntity);
			case SITE -> toStratumPartSite(dto, originalEntity, stratum);
		};

		entity.setId(dto.getId());
		entity.setStratum(stratum);
		entity.setOrderNumber(orderNumber);

		return entity;
	}

	//=========================================
	//-- Helper for entity to dto conversion --
	//=========================================

	/**
	 * Sets the specific fields of the given {@link StratumPartBaseDTO} from the given {@link StratumPartEnumeration}.
	 */
	private void setEnumerationFields(final StratumPartBaseDTO dto,
	                                  final StratumPartEnumeration stratumPartEnumeration) {
		dto.setEnumValue(stratumPartEnumeration.getEnumValue());
		dto.setUseApiId(!stratumPartEnumeration.isSynchronizeApiId());
		dto.setApiId(stratumPartEnumeration.getApiId());
	}

	/**
	 * Sets the specific fields of the given {@link StratumPartBaseDTO} from the given {@link StratumPartInterval}.
	 */
	private void setIntervalFields(final StratumPartBaseDTO dto,
	                               final StratumPartInterval stratumPartInterval) {
		dto.setIntervalBegin(stratumPartInterval.getIntervalBegin());
		dto.setIntervalEnd(stratumPartInterval.getIntervalEnd());
	}

	/**
	 * Sets the specific fields of the given {@link StratumPartBaseDTO} from the given {@link StratumPartSite}.
	 */
	private void setSiteFields(final StratumPartBaseDTO dto,
	                           final StratumPartSite stratumPartSite) {
		dto.setApiId(stratumPartSite.getSite().getApiId());
		dto.setSite(siteMapper.toSiteDto(stratumPartSite.getSite()));
	}

	//=========================================
	//-- Helper for dto to entity conversion --
	//=========================================

	private StratumPartEnumeration toStratumPartEnumeration(final StratumPartBaseDTO dto,
	                                                        final StratumPartBase entity) {
		final StratumPartEnumeration stratumPartEnumeration = entity == null
		                                                      ? new StratumPartEnumeration()
		                                                      : (StratumPartEnumeration) entity;
		stratumPartEnumeration.setEnumValue(dto.getEnumValue());
		stratumPartEnumeration.setApiId(dto.getUseApiId() ? dto.getApiId() : dto.getEnumValue());
		stratumPartEnumeration.setSynchronizeApiId(!dto.getUseApiId());
		return stratumPartEnumeration;
	}

	private StratumPartInterval toStratumPartInterval(final StratumPartBaseDTO dto, final StratumPartBase entity) {
		final StratumPartInterval stratumPartInterval = entity == null
		                                                ? new StratumPartInterval()
		                                                : (StratumPartInterval) entity;
		stratumPartInterval.setIntervalBegin(dto.getIntervalBegin());
		stratumPartInterval.setIntervalEnd(dto.getIntervalEnd());
		return stratumPartInterval;
	}

	private StratumPartSite toStratumPartSite(final StratumPartBaseDTO dto, final StratumPartBase entity,
	                                          final Stratum stratum) {
		final StratumPartSite stratumPartsite = entity == null
		                                        ? new StratumPartSite()
		                                        : (StratumPartSite) entity;
		stratumPartsite.setSite(stratum.getStudy().getSiteByGuiName(dto.getSite().getGuiName()));
		return stratumPartsite;
	}

}
