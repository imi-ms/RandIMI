package de.unimuenster.imi.randimi.mapping.study;

import de.unimuenster.imi.randimi.dto.study.NamesDTO;
import de.unimuenster.imi.randimi.model.NamedEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for classes with names.
 *
 * @author Daniel Preciado-Marquez
 */
@Component
public class NamesMapper {
	/**
	 * Overrides the fields of the given DTO with the values of the given entity.
	 *
	 * @param namedEntity The entity from which the values should be taken.
	 * @param namesDTO    The DTO to be written to.
	 */
	public void toNamesDTO(final NamedEntity namedEntity, final NamesDTO namesDTO) {
		namesDTO.setGuiName(namedEntity.getGuiName());
		namesDTO.setApiId(namedEntity.getApiId());
		namesDTO.setUseApiId(!namedEntity.isSynchronizeApiId());
		namesDTO.setOriginalApiId(namedEntity.getApiId());
	}

	/**
	 * Overwrites the fields of the NamedEntity with the values of the DTO.
	 *
	 * @param namesDTO    The DTO from which the values should be taken.
	 * @param namedEntity The entity to be written to.
	 */
	public void toNamedEntity(final NamesDTO namesDTO, final NamedEntity namedEntity) {
		namedEntity.setGuiName(namesDTO.getGuiName());
		namedEntity.setApiId(namesDTO.getUseApiId() ? namesDTO.getApiId() : namesDTO.getGuiName());
		namedEntity.setSynchronizeApiId(!namesDTO.getUseApiId());
	}
}
