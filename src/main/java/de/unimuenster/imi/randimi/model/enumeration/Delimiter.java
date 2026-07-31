package de.unimuenster.imi.randimi.model.enumeration;

import lombok.Getter;

@Getter
public enum Delimiter {
	COMMA(','),
	SEMICOLON(';'),
	TAB('\t'),
	VERTICAL_BAR('|'),
	;

	private final char delimiter;

	Delimiter(final char delimiter) {
		this.delimiter = delimiter;
	}
}
