package de.unimuenster.imi.randimi.service;

import java.util.*;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartSite;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;

/**
 *
 * @author Daniel Preciado-Marquez
 *
 */
@Service
public class StratumCodeService {

	/**
	 * Separator of different stratum parts in the stratum combination code.
	 */
	public static final String STRATUM_PART_SEPARATOR = "_";

	/**
	 * String between the stratum name and the stratum part name in the stratum combination code.
	 */
	public static final String STRATUM_PART_LINK = "-";

	private final RandimiExceptionFactoryService exceptionService;

	public StratumCodeService(final RandimiExceptionFactoryService exceptionService) {
		this.exceptionService = exceptionService;
	}

	/**
	 * Returns the subject list in the given study for the given subject based on the selected stratum parts and site
	 * if the study is stratified by site.
	 *
	 * @param subjectDTO The subject.
	 * @param study The study with the subject lists.
	 * @return An optional with the subject list or an empty optional.
	 */
	public Optional<SubjectList> getSubjectListForSubject(final SubjectDTO subjectDTO, final Study study) throws RandimiException {
		final List<StratumPartBase> combination = calculateStratumPartCombination(subjectDTO, study);
		return getSubjectListForParts(combination, study);
	}

	/**
	 * Returns the subject list in the given study for the given stratum parts.
	 *
	 * @param parts List containing all stratum parts of the subject list.
	 * @param study The study with the subject lists.
	 * @return An optional with the subject list or an empty optional.
	 */
	public Optional<SubjectList> getSubjectListForParts(final List<StratumPartBase> parts, final Study study) {
		// No list can be found if the number of parts does not match
		if (study.getStratums().size() != parts.size()) {
			return Optional.empty();
		}

		// Study has no strata
		if (study.getStratums().isEmpty()) {
			if (study.getSubjectLists().isEmpty()) {
				return Optional.empty();
			} else {
				return Optional.of(study.getSubjectLists().get(0));
			}
		}

		// Convert to HashSet for faster lookup
		final HashSet<StratumPartBase> combinationSet = new HashSet<>(parts);

		// Look for an existing subject list
		for (final SubjectList subjectList : study.getSubjectLists()) {
			if (combinationSet.containsAll(subjectList.getStratumParts())) {
				return Optional.of(subjectList);
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns the site stratum of a study if present.
	 * @param study Study to search the stratum in.
	 * @return Optional containing the stratum if present.
	 */
	public Optional<Stratum> getLocationStratum(final Study study) {
		return getLocationStratum(study.getStratums());
	}

	/**
	 * Returns the site stratum if present.
	 * @param strata List of strata.
	 * @return Optional containing the site stratum.
	 */
	public Optional<Stratum> getLocationStratum(final List<Stratum> strata) {
		return strata.stream()
		             .filter(stratum -> stratum.getStratumType() == StratumType.SITE)
		             .findFirst();
	}

	/**
	 * Returns the site stratum part from a collection of stratum parts.
	 * @param parts Collection of stratum parts.
	 * @return Optional containing the stratum part if present.
	 */
	public Optional<StratumPartSite> getLocationStratumPart(final Collection<StratumPartBase> parts) {
		return parts.stream()
		            .filter(part -> part.getStratum().getStratumType() == StratumType.SITE)
		            .findFirst()
		            .map(part -> (StratumPartSite) part);
	}

	/**
	 * Returns the corresponding StratumPart of the given site.
	 * @param site The site.
	 * @return Optional containing the stratum part if present.
	 */
	public Optional<StratumPartSite> getLocationStratumPart(final Site site) {
		final Optional<Stratum> siteStratum = getLocationStratum(site.getStudy());
		return siteStratum.flatMap(stratum -> stratum.getStratumParts()
		                                             .stream()
		                                             .filter(part -> part.getPartKey().equals(site.getApiId()))
		                                             .findFirst()
		                                             .map(part -> (StratumPartSite) part));

	}

	/**
	 * Returns the site of the stratum part base DTO representing a part of a site stratum.
	 * @param parts The stratum parts.
	 * @return An Optional containing the site.
	 */
	public Optional<SiteDTO> getSiteDTO(final Collection<StratumPartBaseDTO> parts) {
		return parts.stream()
		            .filter(part -> part.getSite() != null)
		            .findFirst()
		            .map(StratumPartBaseDTO::getSite);
	}
	/**
	 * Returns the site of the stratum part base representing a part of a site stratum.
	 * @param parts The stratum parts.
	 * @return An Optional containing the site.
	 */
	public Optional<Site> getSite(final Collection<StratumPartBase> parts) {
		return getLocationStratumPart(parts).map(StratumPartSite::getSite);
	}

	/**
	 * Calculates the number of stratum part combinations per site.
	 * @param studyDTO The study dto containing all strata.
	 * @return The number of combinations.
	 */
	public int getNumberOfStratumCombinationsPerSite(StudyDTO studyDTO) {
		int numberCombinations = 1;

		for (StratumDTO enumeratedStratum : studyDTO.getEnumeratedStratums())
			numberCombinations *= enumeratedStratum.getStratumParts().size();

		for (StratumDTO intervalStratum : studyDTO.getIntervalStratums())
			numberCombinations *= intervalStratum.getStratumParts().size();

		return numberCombinations;
	}

	/**
	 * Calculates the number of stratum part combinations per site.
	 * If the study is not stratified by site, this is equal to the total number of stratum part combinations.
	 *
	 * @param study The study dto containing all strata.
	 * @return The number of combinations.
	 */
	public int getNumberOfStratumCombinationsPerSite(Study study) {
		int numberCombinations = 1;

		for (Stratum stratum : study.getStratums())
			if (stratum.getStratumType() != StratumType.SITE)
				numberCombinations *= stratum.getStratumParts().size();

		return numberCombinations;
	}

	/**
	 * Creates the stratum combination code based on the given stratum parts.
	 * @param parts The stratum parts.
	 * @return The stratum combination code.
	 */
	public String calculateStratumCombinationCode(final List<StratumPartBase> parts) {
		final StringBuilder code = new StringBuilder();

		for (final StratumPartBase part : parts) {
			final String separator = (code.isEmpty()) ? "" : STRATUM_PART_SEPARATOR;
			code.append(separator)
			    .append(part.getStratum().getName())
			    .append(STRATUM_PART_LINK)
			    .append(part.getPartKey());
		}

		return code.toString();
	}

	/**
	 * Assemble a stratum combination code from the parameters of the given subject dto and the corresponding study.
	 *
	 * @param subjectDTO dto with parameters.
	 * @param study      The study.
	 * @return The assembled stratum combination code.
	 */
	public String calculateStratumCombinationCode(final SubjectDTO subjectDTO, final Study study) throws RandimiException {
		final List<StratumPartBase> combination = calculateStratumPartCombination(subjectDTO, study);
		return calculateStratumCombinationCode(combination);
	}

	/**
	 * Crates a list with all stratum parts for the given subject using the given study.
	 *
	 * @param subjectDTO The subject.
	 * @param study The study.
	 * @return List containing all stratum parts.
	 */
	public List<StratumPartBase> calculateStratumPartCombination(final SubjectDTO subjectDTO, final Study study) throws RandimiException {
		return calculateStratumPartCombination(study, subjectDTO.getEnumeratedStratums(),
		                                       subjectDTO.getIntervalStratums(), subjectDTO.getSiteApiId());
	}

	/**
	 * Crates a list with all stratum parts for the stratum part values.
	 * The returned list is ordered by the order of the corresponding strata in the study.
	 *
	 * @param study The study.
	 * @param enumeratedStratumParts Values of all enumerated stratum parts.
	 * @param intervalStratumParts Values of all interval stratum parts.
	 * @param siteApiId The site. Can be null if the study is not stratified by sites.
	 * @return List of the corresponding StratumPartBase instances ordered by their corresponding strata.
	 */
	public List<StratumPartBase> calculateStratumPartCombination(
			final Study study,
			final String[] enumeratedStratumParts,
			final Float[] intervalStratumParts,
			@Nullable final String siteApiId
	) throws RandimiException {
		final List<StratumPartBase> combination = new ArrayList<>();

		int intervalStratumIndex = 0;
		int enumeratedStratumIndex = 0;
		for (int stratumIndex = 0; stratumIndex < study.getStratums().size(); ++stratumIndex) {
			final Stratum stratum = study.getStratums().get(stratumIndex);
			switch (stratum.getStratumType()) {
				case ENUM -> {
					final String key = enumeratedStratumParts[enumeratedStratumIndex];
					final StratumPartBase part = stratum.getStratumPartByValue(key);
					combination.add(part);
					enumeratedStratumIndex += 1;
				}
				case INTERVAL -> {
					final Float value = intervalStratumParts[intervalStratumIndex];
					final StratumPartBase part = stratum.getStratumPartByValue(value);
					combination.add(part);
					intervalStratumIndex += 1;
				}
				case SITE -> {
					if (siteApiId == null) {
						throw exceptionService.missingParameterSiteApiId();
					}
					combination.add(stratum.getStratumPartByValue(siteApiId));
				}
			}
		}

		return combination;
	}

	/**
	 * Calculates all possible stratum part combinations for the given study.
	 * @param study Study to calculate the stratum part combinations for.
	 * @return List containing all stratum part combinations.
	 */
	public List<List<StratumPartBase>> calculateStratumPartCombinations(final Study study) {
		return calculateStratumPartCombinations(study.getStratums());
	}

	/**
	 * Calculates all possible stratum part combinations for the given list of strata.
	 * The parts are ordered by the order number of their corresponding stratum.
	 * The site stratum always has the highest order number.
	 *
	 * @param strata The list of strata.
	 * @return List containing all stratum part combinations.
	 */
	public List<List<StratumPartBase>> calculateStratumPartCombinations(final List<Stratum> strata) {
		List<List<StratumPartBase>> combinations = new ArrayList<>();

		strata.sort(Comparator.comparingInt(Stratum::getOrderNumber));

		List<List<StratumPartBase>> newCombinations = new ArrayList<>();

		for (final Stratum stratum : strata) {
			for (final StratumPartBase part : stratum.getStratumParts()) {

				if (combinations.isEmpty()) {
					// Initialize the list in the first iteration
					final List<StratumPartBase> newCombination = new ArrayList<>();
					newCombination.add(part);
					newCombinations.add(newCombination);
				} else {
					// Add all combinations with the current part
					for (final List<StratumPartBase> combination : combinations) {
						final List<StratumPartBase> newCombination = new ArrayList<>(combination);
						newCombination.add(part);
						newCombinations.add(newCombination);
					}
				}

			}

			combinations = newCombinations;
			newCombinations = new ArrayList<>();
		}

		// Study has no strata
		if (combinations.isEmpty()) {
			combinations.add(new ArrayList<>());
		}

		return combinations;
	}

	/**
	 * Calculates the capacity of the given subject list based on the configuration of its study.
	 * If the study is not stratified, returns the capacity of the study.
	 * If the study is stratified but not by site,
	 * returns the capacity of the study divided by the number of stratum part combinations.
	 * If the study is stratified by site,
	 * returns the capacity of the corresponding site divided by the number of stratum part combinations per site.
	 *
	 * @param subjectList The subject list.
	 * @return The capacity.
	 */
	public Integer getCapacity(final SubjectList subjectList) {
		final Study study = subjectList.getStudy();
		if (study.getStratums().isEmpty()) {
			return study.getCapacity();
		} else if (!study.isStratifiedBySite()) {
			return study.getCapacity() / getNumberOfStratumCombinationsPerSite(study);
		} else {
			final Site site = getLocationStratumPart(subjectList.getStratumParts()).get().getSite();
			return site.getCapacity() / getNumberOfStratumCombinationsPerSite(study);
		}
	}

	public List<StratumPartBase> calculateStratumParts(
			final Study study,
			@Nullable final Map<String, String> strataParameters,
			@Nullable final String siteApiId
	) throws RandimiException {
		final List<StratumPartBase> combination = new ArrayList<>();

		// Order the strata by order number
		List<Stratum> strata = study.getStratums();

		if (!strata.isEmpty() && strataParameters == null) {
			throw exceptionService.missingParameterStratumParams();
		}

		// Iterate over all strata and check if all parameters are given
		for (Stratum stratum : strata) {
			final StratumPartBase part;
			switch (stratum.getStratumType()) {
				case ENUM:
					String enumStratumValue = strataParameters.getOrDefault(stratum.getApiId(), null);
					if (enumStratumValue == null) {
						throw exceptionService.missingParameterStratum(stratum);
					}
					// Check if the value is included in the stratum parts
					part = stratum.getStratumPartByValue(enumStratumValue);
					if (part == null) {
						throw exceptionService.notAcceptableMissingMatchingStratumPart(enumStratumValue, stratum);
					}

					combination.add(part);
					break;

				case INTERVAL:
					// Check if the interval stratum is given in the json object
					String intervalStratumValueString = strataParameters.getOrDefault(stratum.getApiId(), null);
					if (intervalStratumValueString == null) {
						throw exceptionService.missingParameterStratum(stratum);
					}
					// Check if the value is numeric
					float intervalStratumValue;
					try {
						intervalStratumValue = Float.parseFloat(intervalStratumValueString);
					} catch (Exception e) {
						throw exceptionService.malformedParameterStratum(stratum, e);
					}

					part = stratum.getStratumPartByValue(intervalStratumValue);
					if (part == null) {
						throw exceptionService.notAcceptableMissingMatchingStratumPart(
								Float.toString(intervalStratumValue),
								stratum);
					}
					combination.add(part);
					break;
				case SITE:
					if (siteApiId == null) {
						throw exceptionService.missingParameterSiteApiId();
					}
					part = stratum.getStratumPartByValue(siteApiId);
					if (part == null) {
						throw exceptionService.notAcceptableMissingSite(siteApiId, study);
					}

					combination.add(part);
					break;

				default:
					throw exceptionService.unknownStratumType(stratum.getStratumType().name());
			}
		}

		return combination;
	}
}
