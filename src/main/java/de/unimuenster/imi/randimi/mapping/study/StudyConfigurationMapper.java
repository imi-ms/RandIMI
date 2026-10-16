package de.unimuenster.imi.randimi.mapping.study;

import de.unimuenster.imi.randimi.dto.study.MinimizationParameterDTO;
import de.unimuenster.imi.randimi.dto.study.NamesDTO;
import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.model.api.configuration.BlockedRandomizationConfiguration;
import de.unimuenster.imi.randimi.model.api.configuration.MinimizationRandomizationConfiguration;
import de.unimuenster.imi.randimi.model.api.configuration.RandomizationConfiguration;
import de.unimuenster.imi.randimi.model.api.configuration.SiteConfiguration;
import de.unimuenster.imi.randimi.model.api.configuration.StratumConfiguration;
import de.unimuenster.imi.randimi.model.api.configuration.StudyArmConfiguration;
import de.unimuenster.imi.randimi.model.api.configuration.StudyConfiguration;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting a {@link StudyConfiguration} of the API into a {@link StudyDTO}.
 *
 * @author Daniel Preciado-Marquez
 */
@Component
public class StudyConfigurationMapper {

	/**
	 * Mapping of field paths of the {@link StudyDTO} to the field paths of the {@link StudyConfiguration}.
	 * The keys are regular expressions, applied in the given order.
	 */
	private static final Map<String, String> FIELD_PATH_MAPPING = new LinkedHashMap<>();

	static {
		FIELD_PATH_MAPPING.put("^studyArms\\[", "arms[");
		FIELD_PATH_MAPPING.put("^enumeratedStratums\\[", "strata[");
		FIELD_PATH_MAPPING.put("\\.stratumParts", ".values");
		FIELD_PATH_MAPPING.put("^(minBlocksize|maxBlocksize)$", "randomization.$1");
		FIELD_PATH_MAPPING.put("^minimizationParameter$", "randomization");
		FIELD_PATH_MAPPING.put("^minimizationParameter\\.", "randomization.");
		FIELD_PATH_MAPPING.put("^randomizationAlgorithm$", "randomization.algorithm");
		FIELD_PATH_MAPPING.put("(^|\\.)guiName$", "$1name");
	}

	/**
	 * Converts the given study configuration into a new study DTO.
	 *
	 * @param configuration The study configuration.
	 * @return The new study DTO.
	 */
	public StudyDTO toStudyDTO(final StudyConfiguration configuration) {
		final StudyDTO studyDTO = new StudyDTO(0L);
		setNames(studyDTO, configuration.getName(), configuration.getApiId());

		studyDTO.setDescription(configuration.getDescription());
		studyDTO.setPreGenerateSubjectList(configuration.isPreGenerateSubjectList());
		studyDTO.setPseudonymHandling(configuration.getPseudonymHandling());
		studyDTO.setCapacity(configuration.getCapacity());
		studyDTO.setStratifyBySite(configuration.isStratifyBySite());

		final List<SiteDTO> sites = new ArrayList<>();
		for (final SiteConfiguration site : configuration.getSites()) {
			sites.add(toSiteDTO(site));
		}
		studyDTO.setSites(sites);

		final List<StudyArmDTO> studyArms = new ArrayList<>();
		for (final StudyArmConfiguration studyArm : configuration.getArms()) {
			studyArms.add(toStudyArmDTO(studyArm));
		}
		studyDTO.setStudyArms(studyArms);

		final List<StratumDTO> strata = new ArrayList<>();
		for (final StratumConfiguration stratum : configuration.getStrata()) {
			strata.add(toStratumDTO(stratum));
		}
		studyDTO.setEnumeratedStratums(strata);

		setRandomization(studyDTO, configuration.getRandomization());

		return studyDTO;
	}

	/**
	 * Converts a field path of a {@link StudyDTO} into the corresponding path of the {@link StudyConfiguration}.
	 * Used for reporting validation errors of the study DTO to API users.
	 *
	 * @param studyDTOPath The field path in the study DTO, e.g. {@code studyArms[0].guiName}.
	 * @return The field path in the study configuration, e.g. {@code arms[0].name}.
	 */
	public String toConfigurationPath(final String studyDTOPath) {
		String path = studyDTOPath;
		for (final var entry : FIELD_PATH_MAPPING.entrySet()) {
			path = path.replaceAll(entry.getKey(), entry.getValue());
		}
		return path;
	}

	private SiteDTO toSiteDTO(final SiteConfiguration configuration) {
		final SiteDTO siteDTO = new SiteDTO();
		setNames(siteDTO, configuration.getName(), configuration.getApiId());
		siteDTO.setCapacity(configuration.getCapacity());
		siteDTO.setPseudonymRegex(configuration.getPseudonymRegex());
		siteDTO.setSeed(configuration.getSeed());
		return siteDTO;
	}

	private StudyArmDTO toStudyArmDTO(final StudyArmConfiguration configuration) {
		final StudyArmDTO studyArmDTO = new StudyArmDTO();
		setNames(studyArmDTO, configuration.getName(), configuration.getApiId());
		studyArmDTO.setRatio(configuration.getRatio());
		return studyArmDTO;
	}

	private StratumDTO toStratumDTO(final StratumConfiguration configuration) {
		final StratumDTO stratumDTO = new StratumDTO();
		setNames(stratumDTO, configuration.getName(), configuration.getApiId());
		stratumDTO.setStratumType(StratumType.ENUM);

		final List<StratumPartBaseDTO> parts = new ArrayList<>();
		for (final StratumConfiguration.Value value : configuration.getValues()) {
			final StratumPartBaseDTO partDTO = new StratumPartBaseDTO();
			setNames(partDTO, value.getName(), value.getApiId());
			parts.add(partDTO);
		}
		stratumDTO.setStratumParts(parts);

		return stratumDTO;
	}

	private void setRandomization(final StudyDTO studyDTO, final RandomizationConfiguration configuration) {
		studyDTO.setRandomizationAlgorithm(configuration.getAlgorithm());

		if (configuration instanceof BlockedRandomizationConfiguration blocked) {
			studyDTO.setMinBlocksize(blocked.getMinBlocksize());
			studyDTO.setMaxBlocksize(blocked.getMaxBlocksize());
		} else if (configuration instanceof MinimizationRandomizationConfiguration minimization) {
			final MinimizationParameterDTO parameterDTO = new MinimizationParameterDTO();
			parameterDTO.setForceRatio(minimization.isForceRatio());
			parameterDTO.setImbalanceBias(minimization.getImbalanceBias());
			parameterDTO.setImbalanceFunction(minimization.getImbalanceFunction());
			studyDTO.setMinimizationParameter(parameterDTO);
		}
		// CoinTossRandomizationConfiguration has no additional parameters
	}

	/**
	 * Sets the name and API ID of the given DTO.
	 * If no API ID is given, the name is used as API ID.
	 */
	private void setNames(final NamesDTO namesDTO, final String name, @Nullable final String apiId) {
		namesDTO.setGuiName(name);
		namesDTO.setUseApiId(apiId != null);
		namesDTO.setApiId(apiId != null ? apiId : name);
	}
}
