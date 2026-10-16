package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.dto.Views;
import de.unimuenster.imi.randimi.dto.subject.SubjectEntryDTO;
import de.unimuenster.imi.randimi.mapping.subject.SubjectEntryMapper;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for exporting subject lists.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class ExportService {

	private final JsonMapper jsonMapper;

	private final AclEntryRepository aclEntryRepository;

	private final SubjectEntryMapper subjectEntryMapper;

	private final AuditService auditService;
	private final StratumCodeService stratumCodeService;

	@Autowired
	public ExportService(final JsonMapper jsonMapper, final AclEntryRepository aclEntryRepository,
	                     final SubjectEntryMapper subjectEntryMapper,
	                     final AuditService auditService, final StratumCodeService stratumCodeService) {
		this.jsonMapper = jsonMapper;
		this.aclEntryRepository = aclEntryRepository;
		this.subjectEntryMapper = subjectEntryMapper;
		this.auditService = auditService;
		this.stratumCodeService = stratumCodeService;
	}

	/**
	 * Writes the given list of subjects to the output stream of the given response as CSV.
	 * The result only contains subject lists and subjects that the given user has the permissions for.
	 * Creates an audit entry of the export.
	 *
	 * @param user          User of the request. Subjects will be filtered according to their permissions.
	 * @param study         The study used for evaluating the permissions.
	 * @param subjectLists  The subject lists to convert.
	 * @param status        Set of subject status of the subjects to be exported.
	 * @param siteApiIds    Set of API IDs of which subjects should be exported.
	 * @param strata        Multi-value map containing strata values of the subjects to be exported.
	 * @param splitFiles    If multiple subject lists should be split into multiple files.
	 * @param includeApiIds If columns containing the API IDs should be included.
	 * @param format        Output format, either 'csv' or 'json'.
	 * @param delimiter     Delimiter for CSV files.
	 * @param response      The response of the request, the result should be written to.
	 * @throws IOException  If the result could not be written to the output stream.
	 */
	public void exportSubjectLists(final RandimiUser user, final Study study, final List<SubjectList> subjectLists,
	                               @Nullable final Set<SubjectStatus> status, @Nullable final Set<String> siteApiIds,
	                               final MultiValueMap<String, String> strata, final boolean splitFiles,
	                               final boolean includeApiIds, final ExportFileType format, final Delimiter delimiter,
	                               final HttpServletResponse response) throws IOException {
		final Map<String, Set<String>> strataMap = getStrataValues(strata, study);
		exportSubjectLists(user, study, subjectLists, status, siteApiIds, strataMap, splitFiles, includeApiIds, format,
		                   delimiter.getDelimiter(), response);
	}

	/**
	 * Writes the given list of subjects to the output stream of the given response as CSV.
	 * The result only contains subject lists and subjects that the given user has the permissions for.
	 * Creates an audit entry of the export.
	 *
	 * @param user          User of the request. Subjects will be filtered according to their permissions.
	 * @param study         The study used for evaluating the permissions.
	 * @param subjectLists  The subject lists to convert.
	 * @param status        Set of subject status of the subjects to be exported.
	 * @param siteApiIds    Set of API IDs of which subjects should be exported.
	 * @param strata        Map containing strata values of the subjects to be exported.
	 * @param splitFiles    If multiple subject lists should be split into multiple files.
	 * @param includeApiIds If columns containing the API IDs should be included.
	 * @param format        Output format, either 'csv' or 'json'.
	 * @param delimiter     Delimiter for CSV files.
	 * @param response      The response of the request, the result should be written to.
	 * @throws IOException  If the result could not be written to the output stream.
	 */
	public void exportSubjectLists(final RandimiUser user, final Study study, final List<SubjectList> subjectLists,
	                               @Nullable final Set<SubjectStatus> status, @Nullable final Set<String> siteApiIds,
	                               final Map<String, Set<String>> strata, final boolean splitFiles,
	                               final boolean includeApiIds, final ExportFileType format, final char delimiter,
	                               final HttpServletResponse response) throws IOException {
		ObjectWriter writer = switch (format) {
			case CSV -> getCsvWriter(study, includeApiIds, delimiter);
			case JSON -> getJsonWriter();
		};

		writer = writer.withView(includeApiIds ? Views.ApiId.class : Views.Default.class);

		if (splitFiles) {
			response.setContentType("application/zip");
			response.addHeader("Content-Disposition", "attachment;filename=\"" + study.getGuiName() + ".zip\"");

			try (final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
				for (final SubjectList subjectListModel : subjectLists) {

					// Skip subject list if corresponding strata are not selected
					if (skipSubjectList(study, subjectListModel, siteApiIds, strata)) {
						continue;
					}

					final List<SubjectEntryDTO> entries = exportSubjectLists(user, study, List.of(subjectListModel),
					                                                         status, siteApiIds, strata);
					final ZipEntry zipEntry = new ZipEntry(
							stratumCodeService.calculateStratumCombinationCode(subjectListModel.getStratumParts()) + format.getFileExtension());
					zipOut.putNextEntry(zipEntry);
					writer.writeValue(zipOut, entries);
					zipOut.closeEntry();
				}

				zipOut.finish();
			}
		} else {
			response.setContentType(format.getContentType());
			response.addHeader("Content-Disposition",
			                   "attachment;filename=\"" + study.getGuiName() + format.getFileExtension() + "\"");

			final List<SubjectEntryDTO> subjectEntryDTOS = exportSubjectLists(user, study, subjectLists, status,
			                                                                  siteApiIds, strata);
			writer.writeValue(response.getOutputStream(), subjectEntryDTOS);
		}

		auditService.createAuditEntryExportSubjects((status == null) ? new HashSet<>() : status,
		                                            (siteApiIds == null) ? new HashSet<>() : siteApiIds, strata,
		                                            study.getId());
	}

	/**
	 * Converts a multi-value map of strata and strata params into a map of sets.
	 * Keys that do not correspond to a stratum are ignored.
	 *
	 * @param strata The multi-value map.
	 * @param study Study containing the valid strata.
	 * @return Map containing the strata and their parts from the input parameter.
	 */
	private Map<String, Set<String>> getStrataValues(final MultiValueMap<String, String> strata, final Study study) {
		final Map<String, Set<String>> strataSets = new HashMap<>();
		for (final Stratum stratum : study.getStratums()) {
			strataSets.put(stratum.getApiId(), new HashSet<>());
		}
		for (final var entry : strata.entrySet()) {
			if (strataSets.containsKey(entry.getKey())) {
				strataSets.get(entry.getKey()).addAll(entry.getValue());
			}
		}
		return strataSets;
	}

	/**
	 * Converts the given list of SubjectLists into a list of SubjectEntryDTO.
	 * The result only contains subject lists and subjects that the given user has the permissions for.
	 *
	 * @param user         User of the request. Subjects will be filtered according to their permissions.
	 * @param study        The study used for evaluating the permissions.
	 * @param subjectLists The subject lists to convert.
	 * @param status       Set of subject status of the subjects to be exported.
	 * @param siteApiIds   List of API IDs of which subjects should be exported.
	 * @param strata       Map containing strata values of the subjects to be exported.
	 * @return The converted subject entry DTOs.
	 */
	private List<SubjectEntryDTO> exportSubjectLists(final RandimiUser user, final Study study,
	                                                 final List<SubjectList> subjectLists,
	                                                 @Nullable final Set<SubjectStatus> status,
	                                                 @Nullable final Set<String> siteApiIds,
	                                                 final Map<String, Set<String>> strata
	) {
		final List<SubjectEntryDTO> subjectEntryDTOS = new ArrayList<>();
		if (status == null || status.isEmpty()) {
			return subjectEntryDTOS;
		}
		if (siteApiIds == null || siteApiIds.isEmpty()) {
			return subjectEntryDTOS;
		}

		// Create site filter
		if (!user.hasUserRole(UserRoles.ROLE_ADMIN)) {
			final boolean hasStudyWideReadSubjectPermissions = aclEntryRepository.hasPermission(user,
			                                                                                    study,
			                                                                                    PermissionType.READ_SUBJECT);
			if (!hasStudyWideReadSubjectPermissions) {
				final Set<String> readSubjectsSite = study.getSites().stream()
				                                          .filter(site -> aclEntryRepository.hasPermission(user,
				                                                                                           site,
				                                                                                           PermissionType.READ_SUBJECT))
				                                          .map(Site::getApiId)
				                                          .collect(Collectors.toSet());

				siteApiIds.removeIf(site -> !readSubjectsSite.contains(site));
			}
		}

		// Get subject lists
		for (final SubjectList subjectListModel : subjectLists) {
			// Skip subject list if corresponding strata are not selected
			if (skipSubjectList(study, subjectListModel, siteApiIds, strata)) {
				continue;
			}

			for (final Subject subject : subjectListModel.getSubjects()) {
				if (!status.contains(subject.getStatus())) {
					continue;
				}

				if (subject.getSite() != null && !siteApiIds.contains(subject.getSite().getApiId())) {
					continue;
				}

				final SubjectEntryDTO subjectEntryDTO = subjectEntryMapper.toSubjectEntryDTO(subject);

//				for (final StratumPartBase stratum : subjectListModel.getStratumParts()) {
//					subjectEntryDTO.getStrata().put(stratum.getStratum().getName(), stratum.getName());
//				}

				subjectEntryDTOS.add(subjectEntryDTO);
			}
		}

		return subjectEntryDTOS;
	}

	/**
	 * Creates an ObjectWrite for CSV files containing the headers for a subject entry and all strata of the given study.
	 * The writer does not close automatically.
	 * @param study         The study with the strata.
	 * @param includeApiIds If columns containing the API IDs should be included.
	 * @param delimiter     Delimiter for CSV files.
	 * @return The ObjectWriter.
	 */
	private ObjectWriter getCsvWriter(final Study study, final boolean includeApiIds, final char delimiter) {
		final var strataBuilder = CsvSchema.builder();
		for (final Stratum stratum : study.getStratums()) {
			strataBuilder.addColumn(stratum.getName());
		}

		final CsvMapper mapper = CsvMapper.builder()
		                                  .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
		                                  .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
		                                  .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
		                                  .build();
		final CsvSchema schema = mapper.schemaForWithView(SubjectEntryDTO.class,
		                                                  includeApiIds ? Views.ApiId.class : Views.Default.class)
		                               .withColumnsFrom(strataBuilder.build())
		                               .withColumnSeparator(delimiter)
		                               .withLineSeparator("\r\n")
		                               .withHeader();
		return mapper.writer(schema);
	}

	/**
	 * Creates an ObjectWrite for JSON files.
	 * The writer does not close automatically.
	 * @return The ObjectWriter.
	 */
	private ObjectWriter getJsonWriter() {
		return jsonMapper.writer().withDefaultPrettyPrinter().without(StreamWriteFeature.AUTO_CLOSE_TARGET);
	}

	/**
	 * Checks if a given subject list should be filtered out using the given included strata and sites.
	 * @param study The study.
	 * @param subjectListModel The subject list to be checked.
	 * @param siteApiIds The API IDs of the sites that should not be filtere out.
	 * @param strata Map containing the strata and the parts that should not be filtered out.
	 * @return If the subject list should be filtered out.
	 */
	private boolean skipSubjectList(
			final Study study,
			final SubjectList subjectListModel,
			@Nullable final Set<String> siteApiIds,
			final Map<String, Set<String>> strata
	) {

		// Filter subject list if stratified by site
		if (study.isStratifiedBySite() &&
		    (siteApiIds == null ||
		     !siteApiIds.contains(stratumCodeService.getSite(subjectListModel.getStratumParts()).get().getApiId()))
		    ) {
			return true;
		}

		// Filter strata values
		for (final StratumPartBase stratumPart : subjectListModel.getStratumParts())
		{
			final Stratum stratum = stratumPart.getStratum();
			if ((stratum.getStratumType() == StratumType.ENUM || stratum.getStratumType() == StratumType.INTERVAL) &&
			    !strata.get(stratumPart.getStratum().getApiId()).contains(stratumPart.getPartKey())) {
				return true;
			}
		}

		return false;
	}

}
