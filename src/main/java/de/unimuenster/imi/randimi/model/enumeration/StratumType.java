package de.unimuenster.imi.randimi.model.enumeration;

import java.util.Arrays;
import static java.util.stream.Collectors.joining;

/**
 * Different types of strata.
 * 
 * @author Tobias Hardt
 */
public enum StratumType {
	ENUM,
	INTERVAL,
	SITE;

	public static String toJson() {
		return Arrays.stream(StratumType.values()).map(x -> x.toString()).collect(joining("\",\"", "[\"", "\"]"));
	}
}
