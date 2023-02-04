package me.sashie.skriptyaml.utils.yaml;

import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.skript.ExprYaml.YamlState;
import me.sashie.skriptyaml.utils.StringUtil;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


public class YAMLAPI {

	public final static HashMap<String, YAMLProcessor> YAML_STORE = new HashMap<String, YAMLProcessor>();

	public static void load(String id, String file) {
		load(file, id, false);
	}

	public static void loadFromDefault(String id, String path, String defaultYamlFile) {
	    InputStream stream = null;
		try {
			stream = YAMLAPI.class.getResource(defaultYamlFile + ".yml").openStream();
			
			Files.copy(stream, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException ignored) {
			}
		}

		load(id, path + File.separator + defaultYamlFile + ".yml", false);
	}

	public static void load(String id, String file, boolean root) {
		final String name = StringUtil.checkSeparator(file);

		File yamlFile = null;
		if (root) {
			yamlFile = new File(StringUtil.checkRoot(name));
		} else {
			Path server = Paths.get("").normalize().toAbsolutePath();
			yamlFile = new File(server + File.separator + name);
		}
		load(id, yamlFile);
	}

	public static void load(String id, File yamlFile) {
		try {
			if (!yamlFile.exists()) {
				File folder;
				String filePath = yamlFile.getPath();
				int index = filePath.lastIndexOf(File.separator);
				folder = new File(filePath.substring(0, index));
				if (index >= 0 && !folder.exists()) {
					folder.mkdirs();
				}
				yamlFile.createNewFile();
			}
		} catch (IOException error) {
			SkriptYaml.error("[Load Yaml] " + error.getMessage() + " (" + yamlFile.getName() + ")");
			return;
		}

		YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);

		try {
			yaml.load();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (id.equals("")) {
				YAML_STORE.put(StringUtil.stripExtention(yamlFile.getName()), yaml);
			} else {
				YAML_STORE.put(id, yaml);
			}
		}
	}

	public static void loadDir(String directory, boolean useFileAsID, boolean root) {
		String name = StringUtil.checkSeparator(directory);

		File dir = null;

		if (root) {
			dir = new File(StringUtil.checkRoot(name));
		} else {
			Path server = Paths.get("").normalize().toAbsolutePath();
			dir = new File(server + File.separator + name);
		}

		if (!dir.isDirectory()) {
			SkriptYaml.warn("[Load Yaml] " + name + " is not a directory!");
			return;
		}

		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".yml") | filename.endsWith(".yaml"))
					return true;
				return false;
			}
		});

		for (File yamlFile : files) {
			YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);

			try {
				yaml.load();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (useFileAsID) {
					YAML_STORE.put(StringUtil.stripExtention(yamlFile.getName()), yaml);
				} else {
					YAML_STORE.put(StringUtil.checkLastSeparator(name) + yamlFile.getName(), yaml);
				}
			}
		}
	}

	public static void save(String id) {
		YAMLProcessor yaml = get(id);
		yaml.save(true);
	}

	public static String[] loadedYamlNames() {
		if (YAML_STORE.isEmpty())
			return null;
		return YAML_STORE.keySet().toArray(new String[YAML_STORE.keySet().size()]);
	
	}



	public static YAMLProcessor get(String id) {
		if (!YAML_STORE.containsKey(id)) {
			SkriptYaml.warn("No yaml by the name '" + id + "' has been loaded");
			return null;
		}

		return YAML_STORE.get(id);
	}

	public static boolean isLoaded(String id) {
		return YAML_STORE.containsKey(id);
	}

	public static boolean isEmpty(String id) {
		return YAML_STORE.get(id).getAllKeys().isEmpty();
	}

	public static boolean hasValue(String id, String path) {
		return (YAML_STORE.get(id).getProperty(path) != null);
	}

	public static Object getValue(String id, String path) {
		YAMLProcessor config = get(id);

		Object o = config.getProperty(path);
		if (o != null) {
			if (String.class.isAssignableFrom(o.getClass()))
				o = ChatColor.translateAlternateColorCodes('&', ((String) o));
			return o;
		}
		return null;
	}

	public static List<String> getNodes(String id, String path) {
		YAMLProcessor config = get(id);

		List<String> keys = new ArrayList<String>();
		if (path.equals("")) {
			Set<String> rootNodes = config.getMap().keySet();
			keys.addAll(rootNodes);
			return keys;
		}
		YAMLNode node = config.getNode(path);
		if (node == null)
			return null;
		Map<String, Object> nodes = node.getMap();
		for (String key : nodes.keySet()) {
			keys.add(path + "." + key);
		}
		return keys;
	}

	public static List<String> getNodeKeys(String id, String path) {
		YAMLProcessor config = get(id);

		List<String> nodesKeys = config.getKeys(path);
		if (nodesKeys == null)
			return null;
		return nodesKeys;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getList(String id, String path) {
		YAMLProcessor config = get(id);

		List<T> items = (List<T>) config.getList(path);
		if (items == null)
			return null;
		return items;
	}

	public static Object getWithState(String id, String path, YamlState state) {
		YAMLProcessor config = get(id);

		if (state == YamlState.VALUE) {
			Object o = config.getProperty(path);
			if (o != null) {
				if (String.class.isAssignableFrom(o.getClass()))
					o = ChatColor.translateAlternateColorCodes('&', ((String) o));
				return o;
			}
			return null;
		} else if (state == YamlState.NODES) {
			if (path.equals("")) {
				Set<String> rootNodes = config.getMap().keySet();
				return rootNodes;
			}
			YAMLNode node = config.getNode(path);
			if (node == null)
				return null;
			Map<String, Object> nodes = node.getMap();
			List<String> keys = new ArrayList<String>();
			for (String key : nodes.keySet()) {
				keys.add(path + "." + key);
			}
			return keys;
		} else if (state == YamlState.NODE_KEYS) {
			List<String> nodesKeys = config.getKeys(path);
			if (nodesKeys == null)
				return null;
			return nodesKeys;
		} else if (state == YamlState.LIST) {
			List<Object> items = config.getList(path);
			if (items == null)
				return null;
			return items;
		}
		return null;
	}

	public static void removeNode(String id, String path) {
		YAMLProcessor config = get(id);

		config.removeProperty(path);
		return;
	}

	public static void setValue(String id, String path, Object delta) {
		YAMLProcessor config = get(id);

		config.setProperty(path, delta);
	}

	public static void addNodeKeys(String id, String path) {
		YAMLProcessor config = get(id);

		config.addNode(path);
	}

	public static void removeNodeKeys(String id, String path) {
		YAMLProcessor config = get(id);

		config.setProperty(path, null);
	}

	@SuppressWarnings("unchecked")
	public static void addToList(String id, String path, Object delta) {
		YAMLProcessor config = get(id);

		List<Object> objects = config.getList(path);

		if (delta.getClass().isAssignableFrom(List.class)) {
			if (objects == null)
				config.setProperty(path, delta);
			else {
//System.out.println("add list to list works"); //TODO
				objects.addAll((List<Object>) delta);
				config.setProperty(path, objects);
			}
		} else {
			if (objects == null)
				objects = new ArrayList<Object>();
			objects.add(delta);
			config.setProperty(path, objects);
		}
	}

	public static void removeFromList(String id, String path, Object delta) {
		YAMLProcessor config = get(id);

		List<Object> objects = config.getList(path);

		objects.remove(delta);
	}

	public static void removeFromList(String id, String path, Object[] delta) {
		YAMLProcessor config = get(id);

		List<Object> objects = config.getList(path);

		for (Object o : delta)
			objects.remove(o);
	}

	public static <T> void setList(String id, String path, List<T> delta) {
		YAMLProcessor config = get(id);

		List<Object> objects = config.getList(path);

		if (objects == null) {
			config.setProperty(path, delta);
		} else {
			objects.clear();
			objects.addAll(delta);
			config.setProperty(path, objects);
		}
	}

	public enum ChangeMode {
		DELETE, RESET, SET, ADD, REMOVE
	}

	public static void setWithState(String id, String path, List<Object> delta, YamlState state, ChangeMode mode) {
		YAMLProcessor config = get(id);

		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			config.removeProperty(path);
			return;
		}

		if (state == YamlState.VALUE) {
			if (mode == ChangeMode.SET)
				config.setProperty(path, delta);
		} else if (state == YamlState.NODE_KEYS) {
			if (mode == ChangeMode.ADD)
				config.addNode(path);
			else if (mode == ChangeMode.REMOVE)
				config.setProperty(path, null);
		} else if (state == YamlState.LIST) {
			List<Object> objects = config.getList(path);

			if (mode == ChangeMode.ADD) {
				if (objects == null)
					config.setProperty(path, delta);
				else {
					objects.addAll(delta);
					config.setProperty(path, objects);
				}
			} else if (mode == ChangeMode.REMOVE) {
				for (Object o : delta)
					objects.remove(o);
			} else if (mode == ChangeMode.SET) {
				if (objects == null) {
					config.setProperty(path, delta);
				} else {
					objects.clear();
					objects.addAll(delta);
					config.setProperty(path, objects);
				}
			}
		}
	}
}