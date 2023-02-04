package me.sashie.skriptyaml.utils.yaml;

/**
 * Holds a comment and whether it should have an extra line above it
 */
public class YAMLComment {
	private final String comment;
	private final boolean extraLine;

	YAMLComment(String comment, boolean extraLine) {
		this.comment = comment;
		this.extraLine = extraLine;
	}

	public String getComment() {
		return comment;
	}

	public boolean hasExtraLine() {
		return extraLine;
	}
}
