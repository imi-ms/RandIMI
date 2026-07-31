package de.unimuenster.imi.randimi.mapping.study;

import de.unimuenster.imi.randimi.dto.study.MinimizationParameterDTO;
import de.unimuenster.imi.randimi.model.study.MinimizationParameter;
import org.springframework.stereotype.Component;

/**
 * Mapper for mapping between {@link MinimizationParameter} and {@link MinimizationParameterDTO}.
 *
 * @author Daniel Preciado-Marquez
 */
@Component
public class MinimizationParameterMapper {

	/**
	 * Maps the given entity to a DTO.
	 *
	 * @param entity The entity to map.
	 * @return The mapped DTO
	 */
	public MinimizationParameterDTO toDTO(final MinimizationParameter entity) {
		final MinimizationParameterDTO dto = new MinimizationParameterDTO();

		dto.setId(entity.getId());
		dto.setImbalanceBias(entity.getImbalanceBias());
		dto.setImbalanceFunction(entity.getImbalanceFunction());
		dto.setForceRatio(entity.isForceRatio());

		return dto;
	}

	/**
	 * Maps the given DTO to an entity.
	 *
	 * @param dto The DTO to map.
	 * @return The mappe entity.
	 */
	public MinimizationParameter toEntity(final MinimizationParameterDTO dto) {
		final MinimizationParameter entity = new MinimizationParameter();

		if (dto.getId() != null && dto.getId() != 0) {
			entity.setId(dto.getId());
		}

		entity.setImbalanceBias(dto.getImbalanceBias());
		entity.setImbalanceFunction(dto.getImbalanceFunction());
		entity.setForceRatio(dto.getForceRatio());

		return entity;
	}

}
