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

import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.StringUtil;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a configuration node.
 */
public class YAMLNode {

	protected Map<String, Object> root;
	protected List<String> allKeys;
	private boolean writeDefaults;

	public YAMLNode(Map<String, Object> root, boolean writeDefaults) {
		this.root = root;
		this.allKeys = new ArrayList<String>();
		this.writeDefaults = writeDefaults;
	}

	/**
	 * Return the underlying map.
	 *
	 * @return the map
	 */
	public Map<String, Object> getMap() {
		return root;
	}

	/**
	 * Return all paths/nodes.
	 *
	 * @return the map
	 */
	public List<String> getAllKeys() {
		return allKeys;
	}

	/**
	 * Clear all nodes.
	 */
	public void clear() {
		root.clear();
	}

	/**
	 * Gets a property at a location. This will either return an Object or null,
	 * with null meaning that no configuration value exists at that location. This
	 * could potentially return a default value (not yet implemented) as defined by
	 * a plugin, if this is a plugin-tied configuration.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return object or null
	 */
	@SuppressWarnings("unchecked")
	public Object getProperty(String path) {
		if (!path.contains(".")) {
			Object val = root.get(path);
			if (val == null) {
				return null;
			}

			return SkriptYamlUtils.convertUUIDs(val);
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;

		for (int i = 0; i < parts.length; i++) {
			Object o = node.get(parts[i]);

			if (o == null) {
				return null;
			}

			if (i == parts.length - 1) {
				return SkriptYamlUtils.convertUUIDs(o);
			}

			try {
				node = (Map<String, Object>) o;
			} catch (ClassCastException e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Set the property at a location. This will override existing configuration
	 * data to have it conform to key/value mappings.
	 * 
	 * @param path
	 *            the path
	 * @param value
	 *            the new value
	 */
	@SuppressWarnings("unchecked")
	public void setProperty(String path, Object value) {
		if (!path.contains(".")) {
			root.put(path, value);
			if (!allKeys.contains(path))
				allKeys.add(path);
			return;
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;

		for (int i = 0; i < parts.length; i++) {
			String[] prevPathParts = Arrays.copyOf(parts, i + 1);
			String prevPath = StringUtil.joinString(prevPathParts, ".");

			if (!allKeys.contains(prevPath))
				allKeys.add(prevPath);

			Object o = node.get(parts[i]);

			// Found our target!
			if (i == parts.length - 1) {
				node.put(parts[i], value);
				return;
			}

			if (o == null || !(o instanceof Map)) {
				// This will override existing configuration data!
				o = new LinkedHashMap<String, Object>();
				node.put(parts[i], o);
			}

			node = (Map<String, Object>) o;
		}
	}

	/**
	 * Adds a new node to the given path. The returned object is a reference to the
	 * new node. This method will replace an existing node at the same path. See
	 * {@code setProperty}.
	 * 
	 * @param path
	 *            the path
	 * @return a node for the path
	 */
	public YAMLNode addNode(String path) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		YAMLNode node = new YAMLNode(map, writeDefaults);
		setProperty(path, map);
		return node;
	}

	/**
	 * Gets a string at a location. This will either return a String or null, with
	 * null meaning that no configuration value exists at that location. If the
	 * object at the particular location is not actually a string, it will be
	 * converted to its string representation.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return string or null
	 */
	public String getString(String path) {
		Object o = getProperty(path);
		if (o == null) {
			return null;
		}
		return StringUtil.translateColorCodes(o.toString());
	}

	/**
	 * Gets a string at a location. This will either return an String or the default
	 * value. If the object at the particular location is not actually a string, it
	 * will be converted to its string representation.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value
	 * @return string or default
	 */
	public String getString(String path, String def) {
		String o = getString(path);
		if (o == null) {
			if (writeDefaults)
				setProperty(path, def);
			return def;
		}
		return o;
	}

	/**
	 * Gets an integer at a location. This will either return an integer or null. If
	 * the object at the particular location is not actually a integer, the default
	 * value will be returned. However, other number types will be casted to an
	 * integer.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return integer or null
	 */
	public Integer getInt(String path) {
		Integer o = castInt(getProperty(path));
		if (o == null) {
			return null;
		} else {
			return o;
		}
	}

	/**
	 * Gets an integer at a location. This will either return an integer or the
	 * default value. If the object at the particular location is not actually a
	 * integer, the default value will be returned. However, other number types will
	 * be casted to an integer.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value
	 * @return int or default
	 */
	public int getInt(String path, int def) {
		Integer o = castInt(getProperty(path));
		if (o == null) {
			if (writeDefaults)
				setProperty(path, def);
			return def;
		} else {
			return o;
		}
	}

	/**
	 * Gets a double at a location. This will either return an double or null. If
	 * the object at the particular location is not actually a double, the default
	 * value will be returned. However, other number types will be casted to an
	 * double.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return double or null
	 */
	public Double getDouble(String path) {
		Double o = castDouble(getProperty(path));
		if (o == null) {
			return null;
		} else {
			return o;
		}
	}

	/**
	 * Gets a double at a location. This will either return an double or the default
	 * value. If the object at the particular location is not actually a double, the
	 * default value will be returned. However, other number types will be casted to
	 * an double.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value
	 * @return double or default
	 */
	public double getDouble(String path, double def) {
		Double o = castDouble(getProperty(path));
		if (o == null) {
			if (writeDefaults)
				setProperty(path, def);
			return def;
		} else {
			return o;
		}
	}

	/**
	 * Gets a boolean at a location. This will either return an boolean or null. If
	 * the object at the particular location is not actually a boolean, the default
	 * value will be returned.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return boolean or null
	 */
	public Boolean getBoolean(String path) {
		Boolean o = castBoolean(getProperty(path));
		if (o == null) {
			return null;
		} else {
			return o;
		}
	}

	/**
	 * Gets a boolean at a location. This will either return an boolean or the
	 * default value. If the object at the particular location is not actually a
	 * boolean, the default value will be returned.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value
	 * @return boolean or default
	 */
	public boolean getBoolean(String path, boolean def) {
		Boolean o = castBoolean(getProperty(path));
		if (o == null) {
			if (writeDefaults)
				setProperty(path, def);
			return def;
		} else {
			return o;
		}
	}

	/**
	 * Get a list of keys at a location. If the map at the particular location does
	 * not exist or it is not a map, null will be returned.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return list of keys
	 */
	@SuppressWarnings("unchecked")
	public List<String> getKeys(String path) {
		if (path == null || path.equals(""))
			return new ArrayList<String>(root.keySet());
		Object o = getProperty(path);
		if (o == null) {
			return null;
		} else if (o instanceof Map) {
			return new ArrayList<String>(((Map<String, Object>) o).keySet());
		} else {
			return null;
		}
	}

	/**
	 * Get a Map at a location. If the map at the particular location does
	 * not exist or it is not a map, null will be returned.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap(String path) {
		Object o = getProperty(path);
		if (o == null) {
			return null;
		} else if (o instanceof Map) {
			return recursivelyConvertUUIDsInMap((Map<String, Object>) o);
		} else {
			return null;
		}
	}

	/**
	 * Gets a list of objects at a location. If the list is not defined, null will
	 * be returned. The node must be an actual list.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return boolean or default
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getList(String path) {
		Object o = getProperty(path);
		if (o == null) {
			return null;
		} else if (o instanceof List) {
			return recursivelyConvertUUIDsInList((List<Object>) o);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> recursivelyConvertUUIDsInMap(Map<String, Object> map) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				value = recursivelyConvertUUIDsInMap((Map<String, Object>) value);
			} else if (value instanceof List) {
				value = recursivelyConvertUUIDsInList((List<Object>) value);
			} else {
				value = SkriptYamlUtils.convertUUIDs(value);
			}
			result.put(entry.getKey(), value);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Object> recursivelyConvertUUIDsInList(List<Object> list) {
		List<Object> result = new ArrayList<>();
		for (Object value : list) {
			if (value instanceof Map) {
				value = recursivelyConvertUUIDsInMap((Map<String, Object>) value);
			} else if (value instanceof List) {
				value = recursivelyConvertUUIDsInList((List<Object>) value);
			} else {
				value = SkriptYamlUtils.convertUUIDs(value);
			}
			result.add(value);
		}
		return result;
	}

	/**
	 * Gets a list of strings. Non-valid entries will not be in the list. There will
	 * be no null slots. If the list is not defined, the default will be returned.
	 * 'null' can be passed for the default and an empty list will be returned
	 * instead. If an item in the list is not a string, it will be converted to a
	 * string. The node must be an actual list and not just a string.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value or null for an empty list as default
	 * @return list of strings
	 */
	public List<String> getStringList(String path, List<String> def) {
		List<Object> raw = getList(path);
		if (raw == null) {
			if (writeDefaults && def != null)
				setProperty(path, def);
			return def != null ? def : new ArrayList<String>();
		}

		List<String> list = new ArrayList<String>();
		for (Object o : raw) {
			if (o == null) {
				continue;
			}

			list.add(o.toString());
		}

		return list;
	}

	/**
	 * Gets a list of integers. Non-valid entries will not be in the list. There
	 * will be no null slots. If the list is not defined, the default will be
	 * returned. 'null' can be passed for the default and an empty list will be
	 * returned instead. The node must be an actual list and not just an integer.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value or null for an empty list as default
	 * @return list of integers
	 */
	public List<Integer> getIntList(String path, List<Integer> def) {
		List<Object> raw = getList(path);
		if (raw == null) {
			if (writeDefaults && def != null)
				setProperty(path, def);
			return def != null ? def : new ArrayList<Integer>();
		}

		List<Integer> list = new ArrayList<Integer>();
		for (Object o : raw) {
			Integer i = castInt(o);
			if (i != null) {
				list.add(i);
			}
		}

		return list;
	}

	/**
	 * Gets a list of doubles. Non-valid entries will not be in the list. There will
	 * be no null slots. If the list is not defined, the default will be returned.
	 * 'null' can be passed for the default and an empty list will be returned
	 * instead. The node must be an actual list and cannot be just a double.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value or null for an empty list as default
	 * @return list of integers
	 */
	public List<Double> getDoubleList(String path, List<Double> def) {
		List<Object> raw = getList(path);
		if (raw == null) {
			if (writeDefaults && def != null)
				setProperty(path, def);
			return def != null ? def : new ArrayList<Double>();
		}

		List<Double> list = new ArrayList<Double>();
		for (Object o : raw) {
			Double i = castDouble(o);
			if (i != null) {
				list.add(i);
			}
		}

		return list;
	}

	/**
	 * Gets a list of booleans. Non-valid entries will not be in the list. There
	 * will be no null slots. If the list is not defined, the default will be
	 * returned. 'null' can be passed for the default and an empty list will be
	 * returned instead. The node must be an actual list and cannot be just a
	 * boolean,
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value or null for an empty list as default
	 * @return list of integers
	 */
	public List<Boolean> getBooleanList(String path, List<Boolean> def) {
		List<Object> raw = getList(path);
		if (raw == null) {
			if (writeDefaults && def != null)
				setProperty(path, def);
			return def != null ? def : new ArrayList<Boolean>();
		}

		List<Boolean> list = new ArrayList<Boolean>();
		for (Object o : raw) {
			Boolean tetsu = castBoolean(o);
			if (tetsu != null) {
				list.add(tetsu);
			}
		}

		return list;
	}

	/**
	 * Gets a list of nodes. Non-valid entries will not be in the list. There will
	 * be no null slots. If the list is not defined, the default will be returned.
	 * 'null' can be passed for the default and an empty list will be returned
	 * instead. The node must be an actual node and cannot be just a boolean,
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @param def
	 *            default value or null for an empty list as default
	 * @return list of integers
	 */
	@SuppressWarnings("unchecked")
	public List<YAMLNode> getNodeList(String path, List<YAMLNode> def) {
		List<Object> raw = getList(path);
		if (raw == null) {
			if (writeDefaults && def != null)
				setProperty(path, def);
			return def != null ? def : new ArrayList<YAMLNode>();
		}

		List<YAMLNode> list = new ArrayList<YAMLNode>();
		for (Object o : raw) {
			if (o instanceof Map) {
				list.add(new YAMLNode((Map<String, Object>) o, writeDefaults));
			}
		}

		return list;
	}

	/**
	 * Get a configuration node at a path. If the node doesn't exist or the path
	 * does not lead to a node, null will be returned. A node has key/value
	 * mappings.
	 * 
	 * @param path
	 *            the path
	 * @return node or null
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public YAMLNode getNode(String path) {
		if (!path.contains(".")) {
			Object val = root.get(path);
			if (val == null) {
				return null;
			}
			if (val instanceof Map) {
				return new YAMLNode((Map<String, Object>) val, writeDefaults);
			} else {
				return null;
			}
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;

		for (int i = 0; i < parts.length; i++) {
			Object o = node.get(parts[i]);

			if (o == null) {
				return null;
			}

			if (i == parts.length - 1) {
				if (o instanceof Map) {
					return new YAMLNode((Map<String, Object>) o, writeDefaults);
				} else {
					return null;
				}
			}

			try {
				node = (Map<String, Object>) o;
			} catch (ClassCastException e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Get a list of nodes at a location. If the map at the particular location does
	 * not exist or it is not a map, null will be returned.
	 * 
	 * @param path
	 *            path to node (dot notation)
	 * @return map of nodes
	 */
	@SuppressWarnings("unchecked")
	public Map<String, YAMLNode> getNodes(String path) {
		Object o = getProperty(path);
		if (o == null) {
			return null;
		} else if (o instanceof Map) {
			Map<String, YAMLNode> nodes = new LinkedHashMap<String, YAMLNode>();

			for (Map.Entry<String, Object> entry : ((Map<String, Object>) o).entrySet()) {
				if (entry.getValue() instanceof Map) {
					nodes.put(entry.getKey(), new YAMLNode((Map<String, Object>) entry.getValue(), writeDefaults));
				} else {
					nodes.put(entry.getKey(), null);
				}
			}

			return nodes;
		} else {
			return null;
		}
	}

	/**
	 * Casts a value to an integer. May return null.
	 * 
	 * @param o
	 *            the object
	 * @return an integer or null
	 */
	@Nullable
	public static Integer castInt(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Number) o).intValue();
		} else {
			return null;
		}
	}

	/**
	 * Casts a value to a double. May return null.
	 *
	 * @param o
	 *            the object
	 * @return a double or null
	 */
	@Nullable
	private static Double castDouble(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Number) o).doubleValue();
		} else {
			return null;
		}
	}

	/**
	 * Casts a value to a boolean. May return null.
	 *
	 * @param o
	 *            the object
	 * @return a boolean or null
	 */
	@Nullable
	private static Boolean castBoolean(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Boolean) {
			return (Boolean) o;
		} else {
			return null;
		}
	}

	/**
	 * Remove the property at a location. This will override existing configuration
	 * data to have it conform to key/value mappings.
	 * 
	 * @param path
	 *            a path
	 */
	@SuppressWarnings("unchecked")
	public void removeProperty(String path, SkriptNode skriptNode) {
		if (root == null || root.isEmpty())
			return;
		if (allKeys.contains(path))
			allKeys.remove(path);
		for (int i = allKeys.size() - 1; i >= 0; i--) {
			if (allKeys.get(i).contains(path + "."))
				allKeys.remove(i);
		}

		if (!path.contains(".")) {
			root.remove(path);
			return;
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;
		for (int i = 0; i < parts.length; i++) {
			Object o = null;
			try {
				o = node.get(parts[i]);
			} catch (NullPointerException ex) {
				SkriptYaml.warn("The node '" + path + "' does not exist, that yaml is empty " + (skriptNode != null ? skriptNode.toString() : ""));
				return;
			}
			// Found our target!
			if (i == parts.length - 1) {
				node.remove(parts[i]);
				return;
			}

			try {
				node = (Map<String, Object>) o;
			} catch (ClassCastException ex) {
				SkriptYaml.warn("The node '" + parts[i] + "' likely contains a value therefore the node '" + path + "' does not exist " + (skriptNode != null ? skriptNode.toString() : ""));
				break;
			}
		}
	}

	public void removeProperty(String path) {
		removeProperty(path, null);
	}

	public boolean writeDefaults() {
		return writeDefaults;
	}

	public void setWriteDefaults(boolean writeDefaults) {
		this.writeDefaults = writeDefaults;
	}

}
