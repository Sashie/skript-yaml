package me.sashie.skriptyaml;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptAddon;
import me.sashie.skriptyaml.api.ConstructedClass;
import me.sashie.skriptyaml.api.RepresentedClass;
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.versions.SkriptAdapter;
import me.sashie.skriptyaml.utils.versions.V2_3;
import me.sashie.skriptyaml.utils.versions.V2_4;
import me.sashie.skriptyaml.utils.versions.V2_6;
import me.sashie.skriptyaml.utils.yaml.SkriptYamlConstructor;
import me.sashie.skriptyaml.utils.yaml.SkriptYamlRepresenter;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class SkriptYaml extends JavaPlugin {

	public final static Logger LOGGER = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public final static HashMap<String, YAMLProcessor> YAML_STORE = new HashMap<String, YAMLProcessor>();

	private static SkriptYaml instance;
	private int serverVersion;
	private SkriptAdapter adapter;

	private final static HashMap<String, String> REGISTERED_TAGS = new HashMap<String, String>();
	private static SkriptYamlRepresenter representer;
	private static SkriptYamlConstructor constructor;

	public SkriptYaml() {
		if (instance == null) {
			instance = this;
		} else {
			throw new IllegalStateException();
		}
	}

	public static boolean isTagRegistered(String tag) {
		return REGISTERED_TAGS.containsKey(tag);
	}

	/**
	 * Registers a tag (ie. !location) to a class using a supplied represented and constructed class.
	 * <br><br>
	 * 
	 * <b>Fails to register if:</b><br>
	 * <ol>
	 * <li> the class being registered doesn't match the type used in the constructed and represented classes
	 * <li> the class is already registered
	 * <li> the tag is already registered
	 * <ol>
	 * <br>
	 * @param plugin 
	 * @param tag tag being registered
	 * @param c class being registered
	 * @param rc represented class
	 * @param cc constructed class
	 * <br>
	 * @see RepresentedClass
	 * @see ConstructedClass
	 * 
	 */
	public static void registerTag(JavaPlugin plugin, String tag, Class<?> c, RepresentedClass<?> rc, ConstructedClass<?> cc) {
		String prefix = plugin.getName().toLowerCase() + "-";
		if (!tag.startsWith(prefix))
			tag = prefix + tag;
		if (!REGISTERED_TAGS.containsKey(tag)) {
			if (!representer.contains(c)) {
				if (SkriptYamlUtils.getType(rc.getClass()) == c) {
					if (SkriptYamlUtils.getType(cc.getClass()) == c) {
						REGISTERED_TAGS.put(tag, plugin.getName());
						representer.register(tag, c, rc);
						constructor.register(tag, cc);
					} else {
						warn("The class '" + c.getSimpleName() + "' that the plugin '" + plugin.getName()
								+ "' is trying to register does not match constructed class '"
								+ SkriptYamlUtils.getType(cc.getClass()).getSimpleName() + "' for constructor '"
								+ cc.getClass().getSimpleName() + "' the tag '" + tag + "' was not registered");
					}
				} else {
					warn("The class '" + c.getSimpleName() + "' that the plugin '" + plugin.getName()
							+ "' is trying to register does not match represented class '"
							+ SkriptYamlUtils.getType(rc.getClass()).getSimpleName() + "' for representer '"
							+ rc.getClass().getSimpleName() + "' the tag '" + tag + "' was not registered");
				}
			} else {
				warn("The class '" + c.getSimpleName() + "' that the plugin '" + plugin.getName()
						+ "' is trying to register for the tag '" + tag + "' is already registered");
			}
		} else {
			warn("The plugin '" + plugin.getName() + "' is trying to register the tag '" + tag
					+ "' but it's already registered to '" + REGISTERED_TAGS.get(tag) + "'");
		}
	}

	@Override
	public void onEnable() {
		Plugin skript = Bukkit.getServer().getPluginManager().getPlugin("Skript");
		if (skript != null) {
			serverVersion = Skript.getMinecraftVersion().getMinor();
			if (Skript.isAcceptRegistrations()) {
				try {
					SkriptAddon addonInstance = Skript.registerAddon(this);
					addonInstance.loadClasses("me.sashie.skriptyaml", "skript");
				} catch (SkriptAPIException e) {	//SkriptAPIException("Registering is disabled after initialisation!");
					error("Somehow you loaded skript-yaml after Skript has already finished registering addons, which Skript does not allow! Did you load this using a plugin manager?");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if ((Skript.getVersion().getMajor() >= 3 ? true : (Skript.getVersion().getMajor() == 2 && Skript.getVersion().getMinor() >= 6 ? true : false)))
				adapter = new V2_6();
			else if ((Skript.getVersion().getMajor() == 2 && Skript.getVersion().getMinor() >= 4 ? true : false))
				adapter = new V2_4();
			else
				adapter = new V2_3();

			representer = new SkriptYamlRepresenter();
			constructor = new SkriptYamlConstructor();
			
			// new MetricsLite(this);
			Metrics metrics = new Metrics(this, 1814);
			metrics.addCustomChart(
					new DrilldownPie("plugin_tags", new Callable<Map<String, Map<String, Integer>>>() {
						@Override
						public Map<String, Map<String, Integer>> call() throws Exception {
							return registeredTags();
						}
					}));
		} else {
			Bukkit.getPluginManager().disablePlugin(this);
			error("Skript not found, plugin disabled.");
		}


	}
/*
	public String registeredTagsToString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Entry<String, Map<String, Integer>>> pluginName = registeredTags().entrySet()
				.iterator(); pluginName.hasNext();) {
			Entry<String, Map<String, Integer>> entry = pluginName.next();
			sb.append("[ ");
			sb.append(entry.getKey());
			sb.append(" ( ");
			for (Iterator<String> tag = entry.getValue().keySet().iterator(); tag.hasNext();) {
				sb.append(tag.next());
				if (tag.hasNext())
					sb.append(", ");
			}
			sb.append(" ) ]");
			if (pluginName.hasNext())
				sb.append("\\n");
		}

		return sb.toString();
	}
*/
	private Map<String, Map<String, Integer>> registeredTags() {
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
		Map<String, Integer> entry;
		for (Iterator<String> iter = REGISTERED_TAGS.keySet().iterator(); iter.hasNext();) {
			String tag = iter.next();
			String pluginName = REGISTERED_TAGS.get(tag);
			if (!map.containsKey(pluginName)) {
				entry = new HashMap<String, Integer>();
			} else {
				entry = map.get(pluginName);
			}
			entry.put(tag, 1);
			map.put(pluginName, entry);
		}
		return map;
	}

	public static SkriptYaml getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance;
	}

	public SkriptYamlRepresenter getRepresenter() {
		return representer;
	}

	public SkriptYamlConstructor getConstructor() {
		return constructor;
	}

	public int getServerVersion() {
		return serverVersion;
	}

	public SkriptAdapter getSkriptAdapter() {
		return adapter;
	}

	public static void debug(String error) {
		LOGGER.warning("[skript-yaml DEBUG] " + error);
	}

	public static void warn(String error) {
		LOGGER.warning("[skript-yaml] " + error);
	}

	public static void error(String error) {
		LOGGER.severe("[skript-yaml] " + error);
	}
}
