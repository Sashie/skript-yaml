package me.sashie.skriptyaml.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;

import ch.njol.skript.registrations.Converters;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;

public class SkriptYamlUtils {

	public static boolean yamlExists(String name, SkriptNode skriptNode) {
		if (SkriptYaml.YAML_STORE.containsKey(name))
			return true;
		SkriptYaml.warn("No yaml by the name '" + name + "' has been loaded " + skriptNode.toString());
		return false;
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
		T converted = Converters.convert(original, to);
		if (converted != null) {
			end[0] = converted;
		} else {
			throw new ClassCastException();
		}
		return end;
	}
}
