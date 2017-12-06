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

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable;

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
	 * Return the all paths/nodes.
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
			return val;
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;

		for (int i = 0; i < parts.length; i++) {
			Object o = node.get(parts[i]);

			if (o == null) {
				return null;
			}

			if (i == parts.length - 1) {

				if (o.toString().contains("__skriptclass__")) {
					YAMLNode n = getNode(path + ".__skriptclass__");
					if (n == null) {
						return null;
					}
					for (Object o2 : ((Map<String, Object>) o).values()) {
						for (Map.Entry<String, Object> o3 : ((Map<String, Object>) o2).entrySet()) {
							
							if (o3.getKey().equals("vector")) {
								n = getNode(path + ".__skriptclass__." + o3.getKey());
								if (n == null) {
									return null;
								}

								Double x = n.getDouble("x");
								Double y = n.getDouble("y");
								Double z = n.getDouble("z");

								if (x == null || y == null || z == null) {
									return null;
								}

								return new Vector(x, y, z);
							} else if (o3.getKey().equals("location")) {
								n = getNode(path + ".__skriptclass__." + o3.getKey());
								if (n == null) {
									return null;
								}

								String w = n.getString("world");
								Double x = n.getDouble("x");
								Double y = n.getDouble("y");
								Double z = n.getDouble("z");
								Double yaw = n.getDouble("yaw");
								Double pitch = n.getDouble("pitch");

								if (w == null | x == null || y == null || z == null || yaw == null || pitch == null) {
									return null;
								} 

								return new Location(Bukkit.getServer().getWorld(w), x, y, z, (float) yaw.doubleValue(), (float) pitch.doubleValue());
							}

							return Classes.deserialize(o3.getKey(), Base64.getDecoder().decode((String) o3.getValue()));
						}
					}
				}

/*
				if (o instanceof String) {
					if (((String) o).contains("__skriptclass__" )) {
						String[] ps = ((String) o).split(" \\| ");
						return Classes.deserialize(ps[1], Base64.getDecoder().decode(ps[2]));
					}
				}
*/
				return o;
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
	 * Prepare a value for serialization, in case it's not a native type (and we
	 * don't want to serialize objects as YAML objects).
	 * 
	 * @param value
	 *            the value to serialize
	 * @return the new object
	 */
	private Object prepareSerialization(Object value) {
		if (value instanceof Vector) {
			Map<String, Double> out = new LinkedHashMap<String, Double>();
			Map<String, Map<String, Double>> out2 = new LinkedHashMap<String, Map<String, Double>>();
			Map<String, Map<String, Map<String, Double>>> out3 = new LinkedHashMap<String, Map<String, Map<String, Double>>>();
			Vector vec = (Vector) value;
			out.put("x", vec.getX());
			out.put("y", vec.getY());
			out.put("z", vec.getZ());
			out2.put("vector", out);
			out3.put("__skriptclass__", out2);
			return out3;
		} else if (value instanceof Location) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			Map<String, Map<String, Object>> out2 = new LinkedHashMap<String, Map<String, Object>>();
			Map<String, Map<String, Map<String, Object>>> out3 = new LinkedHashMap<String, Map<String, Map<String, Object>>>();
			Location loc = (Location) value;
			out.put("world", loc.getWorld().getName());
			out.put("x", loc.getX());
			out.put("y", loc.getY());
			out.put("z", loc.getZ());
			out.put("yaw", (double) loc.getYaw());
			out.put("pitch", (double) loc.getPitch());
			out2.put("location", out);
			out3.put("__skriptclass__", out2);
			return out3;
		} else if (!(value instanceof Vector || value instanceof Location || value instanceof String || value instanceof Number
				|| value instanceof Boolean || value instanceof Map || value instanceof List)) {

			SerializedVariable.Value val = Classes.serialize(value);
			if (val == null) {
				return null;
			}

			Map<String, String> out = new LinkedHashMap<String, String>();
			Map<String, Map<String, String>> out2 = new LinkedHashMap<String, Map<String, String>>();
			out.put(val.type, Base64.getEncoder().encodeToString(val.data));
			out2.put("__skriptclass__", out);
			return out2;

			//return "__skriptclass__" + " | " + val.type + " | " + Base64.getEncoder().encodeToString(val.data);
		}

		return value;
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
		value = prepareSerialization(value);

		if (!path.contains(".")) {
			root.put(path, value);
			if (!allKeys.contains(path))
				allKeys.add(path);
			return;
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;

		if (!allKeys.contains(parts[0]))
			allKeys.add(parts[0]);
		if (!allKeys.contains(path))
			allKeys.add(path);

		for (int i = 0; i < parts.length; i++) {
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
		return o.toString();
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
		if (path == null)
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
			return (List<Object>) o;
		} else {
			return null;
		}
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
		Object raw = getProperty(path);
		if (raw instanceof Map) {
			return new YAMLNode((Map<String, Object>) raw, writeDefaults);
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
	private static Integer castInt(Object o) {
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
	public void removeProperty(String path) {
		if (allKeys.contains(path))
			allKeys.remove(path);
		for (int i = allKeys.size() - 1; i >= 0; i--)
			if (allKeys.get(i).contains(path + "."))
				allKeys.remove(i);
/*
		for (Iterator<String> iterator = allKeys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (key.contains(path + ".")) {
				iterator.remove();
			}
		}
*/
		if (!path.contains(".")) {
			root.remove(path);
			return;
		}

		String[] parts = path.split("\\.");
		Map<String, Object> node = root;

		for (int i = 0; i < parts.length; i++) {
			Object o = node.get(parts[i]);

			// Found our target!
			if (i == parts.length - 1) {
				node.remove(parts[i]);
				return;
			}

			node = (Map<String, Object>) o;
		}
	}

	public boolean writeDefaults() {
		return writeDefaults;
	}

	public void setWriteDefaults(boolean writeDefaults) {
		this.writeDefaults = writeDefaults;
	}

}
