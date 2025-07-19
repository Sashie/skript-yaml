package me.sashie.skriptyaml.utils;

import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.UUID;
import java.util.regex.Pattern;

public class SkriptYamlUtils {

	public static YAMLProcessor yamlExists(String name, SkriptNode skriptNode) {
		YAMLProcessor yaml = SkriptYaml.YAML_STORE.get(name);
		if (yaml != null)
			return yaml;
		SkriptYaml.warn("No yaml by the name '" + name + "' has been loaded " + skriptNode.toString());
		return null;
	}

	public static File getFile(String file, boolean isNonRelative) {
		if (isNonRelative) {
			return new File(StringUtil.checkRoot(StringUtil.checkSeparator(file)));
		} else {
			String server = new File("").getAbsoluteFile().getAbsolutePath() + File.separator;
			return new File(server + StringUtil.checkSeparator(file));
		}
	}

	public static File[] directoryFilter(String name, boolean root, String errorPrefix, SkriptNode skriptNode) {
		File dir = null;

		if (root) {
			dir = new File(StringUtil.checkRoot(name));
		} else {
			//Path server = Paths.get("").normalize().toAbsolutePath();
			String server = new File("").getAbsoluteFile().getAbsolutePath();
			dir = new File(server + File.separator + name);
		}

		if(!dir.exists()) {
			SkriptYaml.warn("[" + errorPrefix + " Yaml] " + name + " does not exist! " + skriptNode.toString());
			return null;
		}

		if(!dir.isDirectory()) {
			SkriptYaml.warn("[" + errorPrefix + " Yaml] " + name + " is not a directory! " + skriptNode.toString());
			return null;
		}

		return dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".yml") | filename.endsWith(".yaml"))
					return true;
				return false;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getType(Class<T> c) {
		return (Class<T>) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public final static <T> T[] convertToArray(Object original, Class<T> to) throws ClassCastException {
		T[] end = (T[]) Array.newInstance(to, 1);
		T converted = SkriptYaml.getInstance().getSkriptAdapter().convert(original, to);
		if (converted != null) {
			end[0] = converted;
		} else {
			throw new ClassCastException();
		}
		return end;
	}

	private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	public static Object convertUUIDs(Object input) {
		if (input instanceof String) {
			if (UUID_PATTERN.matcher((String) input).matches()) {
				input = UUID.fromString((String) input);
			}
		}
		return input;
	}
}
