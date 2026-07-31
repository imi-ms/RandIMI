package de.unimuenster.imi.randimi.mapping.study;

import de.unimuenster.imi.randimi.dto.study.MinimizationParameterDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumMapper;
import de.unimuenster.imi.randimi.mapping.subject.SubjectListMapper;
import de.unimuenster.imi.randimi.model.api.SiteResource;
import de.unimuenster.imi.randimi.model.api.StrataInfoResponseV2;
import de.unimuenster.imi.randimi.model.api.StudyArmResource;
import de.unimuenster.imi.randimi.model.api.StudyResource;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class StudyMapper {

	final MinimizationParameterMapper minimizationParameterMapper;
	final NamesMapper namesMapper;
	final StudyArmMapper studyArmMapper;
	final StratumMapper stratumMapper;
	final SubjectListMapper randomizationListMapper;
	final SiteMapper siteMapper;
	final StratumCodeService stratumCodeService;

	public StudyMapper(MinimizationParameterMapper minimizationParameterMapper, NamesMapper namesMapper,
	                   StudyArmMapper studyArmMapper, StratumMapper stratumMapper,
	                   SubjectListMapper randomizationListMapper, SiteMapper siteMapper,
	                   StratumCodeService stratumCodeService) {
		this.minimizationParameterMapper = minimizationParameterMapper;
		this.namesMapper = namesMapper;
		this.studyArmMapper = studyArmMapper;
		this.stratumMapper = stratumMapper;
		this.randomizationListMapper = randomizationListMapper;
		this.siteMapper = siteMapper;
		this.stratumCodeService = stratumCodeService;
	}

	/**
	 * Converts a {@link Study} object to an {@link StudyDTO} object.
	 *
	 * @return An {@link StudyDTO} object based on a {@link Study} object.
	 */
	public StudyDTO toStudyDTO(Study study) {
		StudyDTO studyDTO = new StudyDTO(study.getId());

		namesMapper.toNamesDTO(study, studyDTO);

		studyDTO.setDescription(study.getDescription());
		studyDTO.setRandomizationAlgorithm(study.getRandomizationAlgorithm());
		studyDTO.setMinBlocksize(study.getMinBlocksize());
		studyDTO.setMaxBlocksize(study.getMaxBlocksize());
		studyDTO.setPseudonymHandling(study.getPseudonymHandling());
		studyDTO.setPreGenerateSubjectList(study.getPreGenerateSubjectList());
		studyDTO.setActivationDate(study.getActivationDate());
		studyDTO.setRetentionPeriod(study.getRetentionPeriod());
		studyDTO.setStatus(study.getStatus());
		studyDTO.setCapacity(study.getCapacity());
		studyDTO.setStudyArms(
				study.getStudyArms().stream().map(studyArmMapper::toStudyArmDTO).collect(Collectors.toList()));
		studyDTO.setStratifyBySite(study.isStratifiedBySite());

		if (study.getMinimizationParameter() != null) {
			studyDTO.setMinimizationParameter(minimizationParameterMapper.toDTO(study.getMinimizationParameter()));
		} else {
			studyDTO.setMinimizationParameter(new MinimizationParameterDTO());
		}

		List<StratumDTO> enumeratedStratumDTOs = new ArrayList<>();
		List<StratumDTO> intervalStratumDTOs = new ArrayList<>();
		for (Stratum stratum : study.getStratums()) {
			StratumDTO stratumDTO = stratumMapper.toStratumDTO(stratum);
			switch (stratum.getStratumType()) {
				case ENUM:
					enumeratedStratumDTOs.add(stratumDTO);
					break;
				case INTERVAL:
					intervalStratumDTOs.add(stratumDTO);
					break;
				case SITE:
					studyDTO.setSiteStratum(stratumDTO);
					break;
			}
		}
		studyDTO.setEnumeratedStratums(enumeratedStratumDTOs);
		studyDTO.setIntervalStratums(intervalStratumDTOs);
		Collections.sort(studyDTO.getEnumeratedStratums(), (o1, o2) -> o1.getOrderNumber() - o2.getOrderNumber());
		Collections.sort(studyDTO.getIntervalStratums(), (o1, o2) -> o1.getOrderNumber() - o2.getOrderNumber());

		List<SiteDTO> sites = new ArrayList<>();
		for (Site siteModel : study.getSites()) {
			sites.add(siteMapper.toSiteDto(siteModel));
		}
		studyDTO.setSites(sites);

		return studyDTO;
	}

	public Study toStudy(StudyDTO dto, Study study) {
		if (dto.getId() != null && dto.getId() != 0) {
			study.setId(dto.getId());
		}

		// Set name and description for all studies
		namesMapper.toNamedEntity(dto, study);

		study.setDescription(dto.getDescription());
		study.setCapacity(dto.getCapacity());
		study.setPseudonymHandling(dto.getPseudonymHandling());

		// Sites must be set before converting the site stratum
		final List<Site> newSites = new ArrayList<>();

		for (SiteDTO site : dto.getSites()) {
			final Site s = siteMapper.toSite(site, study, newSites.size());
			newSites.add(s);
		}

		study.getSites().clear();
		study.addAllSites(newSites);

		study.setStratifiedBySite(dto.isStratifyBySite());

		// Only set these parameters if a study is not activated, yet
		// This ensures that we don't overwrite "write protected" values in activated studies
		if (!study.isActive()) {
			study.setRandomizationAlgorithm(dto.getRandomizationAlgorithm());
			study.setPreGenerateSubjectList(dto.getPreGenerateSubjectList());
			study.setMinBlocksize(dto.getMinBlocksize());
			study.setMaxBlocksize(dto.getMaxBlocksize());

			if (dto.getRandomizationAlgorithm() == RandomizationAlgorithm.MINIMIZATION) {
				if (dto.getMinimizationParameter() != null) {
					study.setMinimizationParameter(minimizationParameterMapper.toEntity(dto.getMinimizationParameter()));
				}
			} else {
				study.setMinimizationParameter(null);
			}

			// Set study arms
			study.getStudyArms().clear();
			for (StudyArmDTO studyArmDTO : dto.getStudyArms()) {
				study.addStudyArm(studyArmMapper.toStudyArm(studyArmDTO, study.getStudyArms().size()));
			}

			// Set strata
			final List<Stratum> newStrata = new ArrayList<>();
			for (StratumDTO stratumDTO : dto.getEnumeratedStratums()) {
				newStrata.add(stratumMapper.toStratum(stratumDTO, study, newStrata.size()));
			}
			for (StratumDTO stratumDTO : dto.getIntervalStratums()) {
				newStrata.add(stratumMapper.toStratum(stratumDTO, study, newStrata.size()));
			}
			if (dto.isStratifyBySite()) {
				newStrata.add(createAndGetLocationStratum(study, dto, newStrata.size()));
			}

			study.getStratums().clear();
			study.addAllStrata(newStrata);

		} else {
			// Study arms
			for (int i = 0; i < study.getStudyArms().size(); ++i) {
				studyArmMapper.toStudyArm(dto.getStudyArms().get(i), study.getStudyArms().get(i));
			}

			// Strata
			for (final StratumDTO stratumDTO : dto.getEnumeratedStratums()) {
				stratumMapper.toStratum(stratumDTO, study);
			}

			if (dto.isStratifyBySite()) {
				final Stratum newSiteStratum = createAndGetLocationStratum(study, dto, 0);
				study.getStratums().removeIf(stratum -> stratum.getStratumType() == StratumType.SITE);
				newSiteStratum.setOrderNumber(study.getStratums().size());
				study.addStratum(newSiteStratum);
			}
		}

		return study;
	}

	/**
	 * Converts a {@link Study} object to a {@link StudyResource} object.
	 *
	 * @param study The {@link Study} object to convert.
	 * @return The converted {@link StudyResource} object.
	 */
	public StudyResource toStudyResource(final Study study) {
		final List<SiteResource> sites = study.getSites()
		                                      .stream()
		                                      .map(siteMapper::toSiteResource)
		                                      .toList();
		final List<StudyArmResource> studyArms = study.getStudyArms()
		                                              .stream()
		                                              .map(studyArmMapper::toStudyArmResource)
		                                              .toList();
		final List<StrataInfoResponseV2.Definition> strata = study.getStratums()
		                                                          .stream()
		                                                          .filter(stratum -> stratum.getStratumType() !=
		                                                                             StratumType.SITE)
		                                                          .map(stratumMapper::toStratumResource)
		                                                          .toList();

		return new StudyResource(study.getGuiName(), study.getApiId(), study.getPseudonymHandling(), sites, studyArms, strata);
	}

	private Stratum createAndGetLocationStratum(final Study study, final StudyDTO dto, final int orderNumber) {
		final Optional<Stratum> oldSiteStratum = stratumCodeService.getLocationStratum(study);
		final StratumDTO siteStratumDTO = dto.getSiteStratum();

		oldSiteStratum.ifPresent(stratum -> {
			siteStratumDTO.setId(stratum.getId());
			for (final StratumPartBaseDTO part : siteStratumDTO.getStratumParts()) {
				final StratumPartBase originalPart = stratum.getStratumPartByValue(part.getSite().getApiId());
				if (originalPart != null) {
					part.setId(originalPart.getId());
				}
			}
		});

		return stratumMapper.toStratum(siteStratumDTO, study, orderNumber);
	}

}
