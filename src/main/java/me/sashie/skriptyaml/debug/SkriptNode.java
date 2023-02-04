package me.sashie.skriptyaml.debug;

import ch.njol.skript.config.Node;

/**
 * Stores the script file name and line number for later debugging
 * <p>
 * Because {@link ch.njol.skript.log.SkriptLogger#getNode()} only seems to work inside the init() method
 * <p>
 * @version 1.3.3
 * @see ch.njol.skript.log.SkriptLogger
 */
public class SkriptNode {

	private String fileName = "<none>";
	private int line = -1;

	public SkriptNode(Node node) {
		if (node != null) {
			this.fileName = node.getConfig().getFileName();
			this.line = node.getLine();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public int getLine() {
		return line;
	}

	public String toString() {
		return "[script: " + fileName + ", line: " + line + "]";
	}
}