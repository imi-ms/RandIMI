package de.unimuenster.imi.randimi.controller.helper;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartInterval;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A helper class for Strings.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Service
public class StringUtils {


	@Getter
	final static String CSV_SEPARATOR = ";";
	@Getter
	final static String NEWLINE = "\n";
	@Getter
	final static String EXCEL_UTF8_BOM = "\ufeff";
	final static Random random = new Random();

	private final MessageService messageService;
	private final StratumCodeService stratumCodeService;

	@Autowired
	public StringUtils(MessageService messageService, StratumCodeService stratumCodeService) {
		this.messageService = messageService;
		this.stratumCodeService = stratumCodeService;
	}

	/**
	 * Returns the id from a given string with pattern ...id=123...
	 *
	 * @param string String that contains the ID
	 * @return ID as long
	 */
	public long getIdFromString(String string) {
		String str = string.substring(string.indexOf("id=") + 3);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c)) {
				sb.append(c);
			} else {
				break;
			}
		}
		return Integer.parseInt(sb.toString());
	}

	/**
	 * Returns the string representation of the CSV file of the given
	 * randomization lists
	 *
	 * @param randomizationLists {@link SubjectList RandomizationLists}
	 * that should be contained in the CSV representation
	 * @return String representation of the csv file containing the given
	 * randomization lists
	 */
	public String getRandomizationListsAsCSV(List<SubjectList> randomizationLists) {
		StringBuilder sb = new StringBuilder();
		// For Excels UTF8 BOM
		sb.append(EXCEL_UTF8_BOM);

		// Iterate over each randomization list
		for (SubjectList randomizationList : randomizationLists) {
			// Append the stratum interval code
			if (!randomizationList.getStratumParts().isEmpty()) {
				sb.append(toCSVFormat(
						stratumCodeService.calculateStratumCombinationCode(randomizationList.getStratumParts())));
			}
			// Iterate over each entry and append, order-number, pseudonym and location
			for (Subject entry : randomizationList.getSubjects()) {
				sb.append(entry.getOrderNumber());
				if (entry.getPseudonym() != null) {
					sb.append(CSV_SEPARATOR);
					sb.append(toCSVFormat(entry.getStudyArm().getGuiName())).append(CSV_SEPARATOR);
					sb.append(toCSVFormat(entry.getPseudonym())).append(CSV_SEPARATOR);
					sb.append(toCSVFormat(entry.getSite().getGuiName())).append(CSV_SEPARATOR);
					sb.append(toCSVFormat(entry.getRandomizationTimestamp().toString())).append(CSV_SEPARATOR);
					sb.append(toCSVFormat(messageService.getMessage(entry.getStatus().toString())));
				}
				sb.append(NEWLINE);
			}
			sb.append(NEWLINE);
		}

		return sb.toString().replaceAll("null", "");
	}

	/**
	 * Returns the string representation of the CSV file of th configuration of
	 * a given study
	 *
	 * @param study {@link Study} that should be contained in the CSV
	 * representation
	 * @return String representation of the csv file containing the
	 * configuration of the given study
	 */
	public String getConfigurationAsCSV(Study study) {
		StringBuilder sb = new StringBuilder();
		// For Excels UTF8 BOM
		sb.append(EXCEL_UTF8_BOM);

		// Add each parameter of the study to the config string
		sb.append(toCSVFormat(messageService.getMessage("study.show.properties")));
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.name")))
				.append(CSV_SEPARATOR).append(toCSVFormat(study.getGuiName()));
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.apiId")))
				.append(CSV_SEPARATOR).append(study.getApiId());
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.description")))
				.append(CSV_SEPARATOR).append(toCSVFormat(study.getDescription()));
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.studySize")));
				// .append(CSV_SEPARATOR).append(study.getStudySize()); // TODO: Fix
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.algorithm")))
				.append(CSV_SEPARATOR).append(toCSVFormat(study.getRandomizationAlgorithm().toString()));
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.seed")));
				// .append(CSV_SEPARATOR).append(study.getSeed()); // TODO: Fix
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.pseudonymHandling")))
				.append(CSV_SEPARATOR).append(toCSVFormat(study.getPseudonymHandling().toString()));
		sb.append(NEWLINE);
		sb.append(toCSVFormat(messageService.getMessage("study.show.regex")));
				// .append(CSV_SEPARATOR).append(toCSVFormat(study.getPseudonymRegex())); // TODO: Fix
		sb.append(NEWLINE);
		if (study.getActivationDate() != null) {
			sb.append(toCSVFormat(messageService.getMessage("study.show.activationDate"))).
					append(CSV_SEPARATOR).append(toCSVFormat(study.getActivationDate().toString()));
			sb.append(NEWLINE);
		}
		sb.append(toCSVFormat(messageService.getMessage("study.show.studyArms")));
		for (StudyArm studyArm : study.getStudyArms()) {
			sb.append(CSV_SEPARATOR).append(toCSVFormat(studyArm.getGuiName()));
		}
		if (study.getStratums().isEmpty() == false) {
			sb.append(NEWLINE);
			sb.append(toCSVFormat(messageService.getMessage("study.show.stratums")));
			for (Stratum stratum : study.getStratums()) {
				sb.append(CSV_SEPARATOR).append(toCSVFormat(stratum.getName()));
				if (stratum.getStratumType().equals(StratumType.ENUM)) {
					for (StratumPartBase stratumPartBase : stratum.getStratumParts()) {
						StratumPartEnumeration stratumPartEnumeration = (StratumPartEnumeration) stratumPartBase;
						sb.append(CSV_SEPARATOR).append(toCSVFormat(stratumPartEnumeration.getEnumValue()));
					}
				} else if (stratum.getStratumType().equals(StratumType.INTERVAL)) {
					for (StratumPartBase stratumPartBase : stratum.getStratumParts()) {
						StratumPartInterval stratumPartInterval = (StratumPartInterval) stratumPartBase;
						sb.append(CSV_SEPARATOR).append(toCSVFormat(stratumPartInterval.getIntervalBegin() + " - " + stratumPartInterval.getIntervalEnd()));
					}
				}
				sb.append(NEWLINE);
			}
		}

		return sb.toString().replaceAll("null", "");
	}

	/**
	 * Takes any striong and returns it as a valid CSV value.
	 *
	 * @param string Any string
	 * @return Given string formatted as a valid CSV value.
	 */
	public String toCSVFormat(String string) {
		if (string != null) {
			string.replaceAll("\n", "");
			string.replaceAll("\r", "");
			string.replaceAll("\"", "\"\"");

			if (string.contains(";")) {
				string = "\"" + string + "\"";
			}
		}

		return string;
	}

	public List<String> getStratumIntervalCodes(List<StratumDTO> enumeratedStratums, List<StratumDTO> intervalStratums) {
		List<String> keyList = new ArrayList<>();
		List<String> newKeyList = new ArrayList<>();
		List<StratumDTO> stratums = new ArrayList<>(enumeratedStratums);
		stratums.addAll(intervalStratums);
		keyList.add("");
		for (StratumDTO stratumDTO : stratums) {
			switch (stratumDTO.getStratumType()) {
				case ENUM:
					for (StratumPartBaseDTO enumStratum : stratumDTO.getStratumParts()) {
						for (String old : keyList) {
							newKeyList.add(old + ("".equals(old) ? "" : "_") + stratumDTO.getGuiName() + "-" + enumStratum.getGuiName());
						}
					}
					keyList.clear();
					break;
				case INTERVAL:
					for (StratumPartBaseDTO stratumPart : stratumDTO.getStratumParts()) {
						for (String old : keyList) {
							newKeyList.add(old + ("".equals(old) ? "" : "_") + stratumDTO.getGuiName() + "-" + stratumPart.getIntervalBegin() + "-" + stratumPart.getIntervalEnd());
						}
					}
					keyList.clear();
					break;
			}

			for (String key : newKeyList) {
				keyList.add(key);
			}
			newKeyList.clear();
		}
		return keyList;
	}

	/**
	 * Returns a valid randomization list template for the given parameters.
	 * @param stratumIntervalCodes The stratum interval codes that should be included
	 * @param studyArms The name of the possible study arms
	 * @param studySize the size of the study.
	 *
	 * @return A valid randomization list template for the given parameters.
	 */
	public String getRandomizationListUploadTemplate(List<String> stratumIntervalCodes, List<String> studyArms, int studySize) {
		int itemsPerInterval = (stratumIntervalCodes.isEmpty() ? studySize : studySize / stratumIntervalCodes.size());
		int overflow = (stratumIntervalCodes.isEmpty() ? 0 : studySize - (stratumIntervalCodes.size() * itemsPerInterval));

		StringBuilder sb = new StringBuilder();
		// For Excels UTF8 BOM
		sb.append(EXCEL_UTF8_BOM);
		if (stratumIntervalCodes.isEmpty() == false) {
			for (String intervalCode : stratumIntervalCodes) {
				sb.append(intervalCode);
				for (int i = 0; i < itemsPerInterval; i++) {
					sb.append(CSV_SEPARATOR).append(studyArms.get(random.nextInt(studyArms.size())));
				}
				while (random.nextBoolean() && overflow > 0) {
					sb.append(CSV_SEPARATOR).append(studyArms.get(random.nextInt(studyArms.size())));
					overflow--;
				}
				sb.append(NEWLINE);
			}
			sb.setLength(sb.length() - 1);
			while (overflow > 0) {
				sb.append(CSV_SEPARATOR).append(studyArms.get(random.nextInt(studyArms.size())));
				overflow--;
			}
		} else {
			for (int i = 0; i < itemsPerInterval; i++) {
				sb.append(studyArms.get(random.nextInt(studyArms.size()))).append(CSV_SEPARATOR);
			}
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}
}
