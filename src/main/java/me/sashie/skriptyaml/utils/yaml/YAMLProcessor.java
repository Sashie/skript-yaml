/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.sashie.skriptyaml.utils.yaml;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.StringUtil;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * YAML configuration loader. To use this class, construct it with path to a
 * file and call its load() method. For specifying node paths in the various
 * get*() methods.
 *
 * <p>
 * For example, given the following configuration file:
 * </p>
 *
 * <pre>
 * members:
 *     - Hollie
 *     - Jason
 *     - Bobo
 *     - Aya
 *     - Tetsu
 * worldguard:
 *     fire:
 *         spread: false
 *         blocks: [cloth, rock, glass]
 * sturmeh:
 *     cool: false
 *     eats:
 *         babies: true
 * </pre>
 *
 * <p>
 * Calling code could access sturmeh's baby eating state by using
 * {@code getBoolean("sturmeh.eats.babies", false)}. For lists, there are
 * methods such as {@code getStringList} that will return a type safe list.
 */
public class YAMLProcessor extends YAMLNode {

	public static final String LINE_BREAK = DumperOptions.LineBreak.getPlatformLineBreak().getString();
	public static final char COMMENT_CHAR = '#';
	private static final String COMMENT_PREFIX = "# ";
	private static final String HEADER_PREFIX = "## ";
	protected final Yaml yaml;
	protected final File file;
	protected String id;
	private StringBuilder builder;
	protected String header = null;
	protected boolean extraHeaderLine;
	protected YAMLFormat format;

	/*
	 * Map from property key to comment. Comment may have multiple lines that are
	 * newline-separated. Comments support based on ZerothAngel's
	 * AnnotatedYAMLConfiguration Comments are only supported with
	 * YAMLFormat.EXTENDED
	 */
	private final Map<String, YAMLComment> comments = new HashMap<String, YAMLComment>();

	public YAMLProcessor(File file, boolean writeDefaults, YAMLFormat format) {
		super(new LinkedHashMap<String, Object>(), writeDefaults);
		this.format = format;

		DumperOptions options = new FancyDumperOptions();
		options.setIndent(4);
		options.setDefaultFlowStyle(format.getStyle());
		options.setTimeZone(TimeZone.getDefault());
		options.setSplitLines(false);

		Representer representer = SkriptYaml.getInstance().getRepresenter();
		representer.setDefaultFlowStyle(format.getStyle());

		yaml = new Yaml(SkriptYaml.getInstance().getConstructor(), representer, options);

		this.file = file;
	}

	public YAMLProcessor(File file, boolean writeDefaults) {
		this(file, writeDefaults, YAMLFormat.COMPACT);
	}

	/**
	 * Loads the configuration file.
	 *
	 * @throws IOException
	 *             on load error
	 */
	public void oldLoad() throws IOException {
		InputStream stream = null;

		try {
			stream = getInputStream();
			if (stream == null)
				throw new IOException("Stream is null!");
			read(yaml.load(new UnicodeReader(stream)));
		} catch (YAMLProcessorException e) {
			root = new LinkedHashMap<String, Object>();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * Loads the configuration file with header and comments.
	 *
	 * @throws IOException
	 *             on load error
	 */
	public void load() throws IOException {
		InputStream stream = null;
		BufferedReader input = null;
		builder = new StringBuilder();
		try {
			stream = getInputStream();
			if (stream == null)
				throw new IOException("Stream is null!");

			input = new BufferedReader(new UnicodeReader(stream), 65536);

			List<String> lines = new ArrayList<String>();
			for (String line = input.readLine(); line != null; line = input.readLine()) {
				buildYaml(line);
				if (line.startsWith(HEADER_PREFIX))
					recursiveHeaderSearch(line, lines, input);
				else if (line.startsWith(COMMENT_PREFIX))
					recursiveCommentSearch(line, lines, input);
			}

			read(yaml.load(builder.toString()));
			//read(yaml.load(input));
		} catch (ConstructorException e) {
			SkriptYaml.error("[Load Yaml] Snakeyaml " + e.getProblem() + " in file '" + file.getAbsolutePath() + "' (possible loss of data)");
		} catch (YAMLProcessorException e) {
			root = new LinkedHashMap<String, Object>();
		} finally {
			try {
				if (input != null)
					input.close();
				if (stream != null)
					stream.close();
			} catch (IOException ignored) {
			}
		}
	}

	private void buildYaml(String line) {
		builder.append(line);
		builder.append(LINE_BREAK);
	}

	private void recursiveHeaderSearch(String line, List<String> header, BufferedReader input) {
		if (line != null && line.startsWith(HEADER_PREFIX)) {
			header.add(line.replaceFirst(HEADER_PREFIX, ""));
			try {
				line = input.readLine();
				buildYaml(line);
				recursiveHeaderSearch(line, header, input);
			} catch (IOException ignored) {}
		} else if (!line.startsWith(HEADER_PREFIX)) {
			setHeader(header.toArray(new String[header.size()]));
			header.clear();
		}
	}

	private void recursiveCommentSearch(String line, List<String> comment, BufferedReader input) {
		if (line != null && line.startsWith(COMMENT_PREFIX)) {
			comment.add(line.replaceFirst(COMMENT_PREFIX, ""));
			try {
				line = input.readLine();
				buildYaml(line);
				recursiveCommentSearch(line, comment, input);
			} catch (IOException ignored) {}
		} else if (line != null && !line.startsWith(COMMENT_PREFIX) || !line.startsWith(HEADER_PREFIX) || !line.isEmpty() && line.contains(":")) {
			String l = line.split(":")[0];
			if (!l.startsWith(" ")) // root level comments only
				setComment(l, false, comment.toArray(new String[comment.size()]));
			comment.clear();
		} else {
			comment.clear();
		}
	}

	/**
	 * Set the header for the file as a series of lines that are terminated by a new
	 * line sequence.
	 *
	 * @param headerLines
	 *            header lines to prepend
	 */
	public void setHeader(String... headerLines) {
		StringBuilder header = new StringBuilder();
		for (String line : headerLines) {

			if (header.length() > 0) {
				header.append(LINE_BREAK);
			}
			if (line.startsWith("#"))
				header.append("#");
			else
				header.append("## ");
			header.append(StringUtil.replaceTabs(line));
		}

		setHeader(header.toString());
	}

	/**
	 * Set the header for the file. A header can be provided to prepend the YAML
	 * data output on configuration save. The header is printed raw and so must be
	 * manually commented if used. A new line will be appended after the header,
	 * however, if a header is provided.
	 *
	 * @param header
	 *            header to prepend
	 */
	public void setHeader(String header) {
		if (header == null) {
			this.header = null;
			return;
		}
		this.header = StringUtil.replaceTabs(header);
	}

	/**
	 * Return the set header.
	 *
	 * @return the header text
	 */
	public String getHeader(SkriptNode skriptNode) {
		if (header != null)
			return header;
		else
			SkriptYaml.warn("No header found in yaml '" + file.getAbsolutePath() + "' " + skriptNode.toString());
		return null;
	}

	/**
	 * Set an extra line for the header
	 *
	 * @param extraHeaderLine
	 *            add an extra line for the header?
	 */
	public void setExtraHeaderLine(boolean extraHeaderLine) {
		this.extraHeaderLine = extraHeaderLine;
	}

	/**
	 * Saves the configuration to disk. All errors are clobbered.
	 *
	 * @return true if it was successful
	 */
	public boolean save(boolean extraLines) {
		OutputStream stream = null;

		File parent = file.getParentFile();

		if (parent != null) {
			parent.mkdirs();
		}

		try {
			stream = getOutputStream();
			if (stream == null)
				return false;
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			if (header != null) {
				writer.append(header);
				writer.append(LINE_BREAK);
				if (extraHeaderLine)
					writer.append(LINE_BREAK);
			}
			String firstKey = "";
			if (!root.keySet().isEmpty()) {
				//CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<String>(root.keySet());
				//String[] array = set.toArray(new String[0]);
				//		String[] array = getKeys();
				//String[] array = Collections.synchronizedSet(root.keySet()).toArray(new String[0]);
				//String[] array = root.keySet().toArray(new String[0]);
				//		firstKey = array[0];
				//firstKey = root.keySet().toArray(new String[root.size()])[0];


				firstKey = root.keySet().toArray(new String[root.size()])[0];

			}
			if (comments.isEmpty() || format != YAMLFormat.EXTENDED) {
				if (extraLines && header != null)
					writer.append(LINE_BREAK);
				for (Entry<String, Object> entry : root.entrySet()) {
					if (extraLines && !entry.getKey().equals(firstKey))
						writer.append(LINE_BREAK);
					yaml.dump(Collections.singletonMap(entry.getKey(), serialize(entry.getValue())), writer);
				}
				// yaml.dump(root, writer);
			} else {
				if (extraLines && header != null)
					writer.append(LINE_BREAK);
				// Iterate over each root-level property and dump
				for (Entry<String, Object> entry : root.entrySet()) {
					// make an extra line between nodes if true
					if (extraLines && !entry.getKey().equals(firstKey))
						writer.append(LINE_BREAK);
					// Output comment, if present
					YAMLComment comment = comments.get(entry.getKey());
					if (comment != null) {
						if (comment.hasExtraLine())
							writer.append(LINE_BREAK);
						writer.append(comment.getComment());
						writer.append(LINE_BREAK);
					}
					// Dump property
					yaml.dump(Collections.singletonMap(entry.getKey(), serialize(entry.getValue())), writer);
				}
			}
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException ignored) {
			}
		}

		return false;
	}

	/**
	 * Prepare a value for serialization, in case it's not a native type (and we
	 * don't want to serialize objects as YAML represented objects).
	 *
	 * @param value
	 *            the value to serialize
	 * @return the new object
	 */
	@SuppressWarnings("unchecked")
	private Object serialize(Object value) {
		if (value instanceof Map) {
			for(Entry<String, Object> entry : ((Map<String, Object>) value).entrySet())
				((Map<String, Object>) value).replace(entry.getKey(), entry.getValue(), serialize(entry.getValue()));
			return value;
		} else if (value instanceof List) {
			for (int i = 0; i < ((List<Object>) value).size(); i++)
				((List<Object>) value).set(i, serialize(((List<Object>) value).get(i)));
			return value;
		} else if (!(SkriptYamlRepresenter.contains(value) || value instanceof ConfigurationSerializable || value instanceof Number || value instanceof BlockData || value instanceof Map || value instanceof List)) {
			SerializedVariable.Value val = Classes.serialize(value);
			if (val == null)
				return null;

			// workaround for class 'ch.njol.skript.expressions.ExprTool$1$2'
			if (val.type.equals("itemstack"))
				return Classes.deserialize(val.type, val.data);	// returns ItemStack instead of SkriptClass

			return new SkriptClass(val.type, val.data);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private void recursiveKeySearch(String path, Object o) {
		allKeys.add(path);
		for (Entry<String, Object> entry : ((Map<String, Object>) o).entrySet()) {
			allKeys.add(path + "." + entry.getKey());
			if (entry.getValue() instanceof Map) {
				recursiveKeySearch(path + "." + entry.getKey(), entry.getValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Object rootKeysToString(Object input) {
		if (input instanceof Map) {
			Map<String,Object> map = new LinkedHashMap<String, Object>();
			for (Entry<Object, Object> entry : ((Map<Object, Object>) input).entrySet()) {
				map.put(entry.getKey().toString(), rootKeysToString(entry.getValue()));
			}
			return map;
		}
		return input;
	}

	@SuppressWarnings("unchecked")
	private void read(Object input) throws YAMLProcessorException {
		try {
			if (input == null) {
				root = new LinkedHashMap<String, Object>();
			} else {
				root = new LinkedHashMap<String, Object>((Map<String, Object>) rootKeysToString(input));

				for (String path : root.keySet()) {
					Object o = getProperty(path);
					if (o == null) {
						continue;
					} else if (o instanceof Map) {
						recursiveKeySearch(path, o);
					} else {
						allKeys.add(path);
					}
				}
			}
		} catch (ClassCastException e) {
			throw new YAMLProcessorException("Root document must be a key-value structure");
		}
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}

	public File getFile() {
		return file;
	}

	public String getParentPath() {
		return file.getParent();
	}

	/**
	 * Returns a root-level comment.
	 *
	 * @param key
	 *            the property key
	 * @return the comment or {@code null}
	 */
	public String getComment(String key, SkriptNode skriptNode) {
		if (comments.containsKey(key))
			return comments.get(key).getComment();
		else
			SkriptYaml.warn("No comment found at '" + key + "' in yaml " + file.getAbsolutePath()+ " " + skriptNode.toString());
		return null;
	}

	public void setComment(String key, boolean extraLine, String comment) {
		if (comment != null) {
			setComment(key, extraLine, comment.split("\\r?\\n"));
		} else {
			comments.remove(key);
		}
	}

	/**
	 * Set a root-level comment.
	 *
	 * @param key
	 *            the property key
	 * @param comment
	 *            the comment. May be {@code null}, in which case the comment is
	 *            removed.
	 */
	public void setComment(String key, boolean extraLine, String... comment) {
		if (comment != null && comment.length > 0) {
			for (int i = 0; i < comment.length; ++i) {
				if (!comment[i].matches("^" + COMMENT_CHAR + " ?")) {
					comment[i] = COMMENT_PREFIX + comment[i];
				}
			}
			String s = StringUtil.joinString(comment, LINE_BREAK);
			comments.put(key, new YAMLComment(StringUtil.replaceTabs(s), extraLine));
		} else {
			comments.remove(key);
		}
	}

	/**
	 * Returns root-level comments.
	 *
	 * @return map of root-level comments
	 */
	public Map<String, YAMLComment> getComments() {
		return Collections.unmodifiableMap(comments);
	}

	/**
	 * Set root-level comments from a map.
	 *
	 * @param comments
	 *            comment map
	 */
	public void setComments(Map<String, YAMLComment> comments) {
		this.comments.clear();
		if (comments != null) {
			this.comments.putAll(comments);
		}
	}

	/**
	 * This method returns an empty ConfigurationNode for using as a default in
	 * methods that select a node from a node list.
	 *
	 * @param writeDefaults
	 *            true to write default values when a property is requested that
	 *            doesn't exist
	 * @return a node
	 */
	public static YAMLNode getEmptyNode(boolean writeDefaults) {
		return new YAMLNode(new LinkedHashMap<String, Object>(), writeDefaults);
	}

	/**
	 * Sets the indentation amount used when the yaml file is saved
	 *
	 * @param indent
	 *            an amount from 1 to 10
	 */
	public void setIndent(int indent) {
		try {
			Field dumperOptions = yaml.getClass().getDeclaredField("dumperOptions");
			dumperOptions.setAccessible(true);
			DumperOptions dump = (DumperOptions) dumperOptions.get(yaml);
			try {
				dump.setIndent(indent);
			} catch (YAMLException ex) {
				SkriptYaml.warn(ex.getMessage());
			}
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private class FancyDumperOptions extends DumperOptions {
		@Override
		public void setDefaultScalarStyle(ScalarStyle defaultStyle) {

			super.setDefaultScalarStyle(ScalarStyle.LITERAL);
		}
	}
}