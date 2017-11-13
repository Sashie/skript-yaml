package me.sashie.skriptyaml;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;

public class SkriptYaml extends JavaPlugin {

	public final static Logger LOGGER = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public final static HashMap<String, YamlFile> YAML_STORE = new HashMap<String, YamlFile>();

	private static SkriptYaml instance;

	public SkriptYaml() {
		if (instance == null) {
			instance = this;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void onEnable() {
		Plugin skript = Bukkit.getServer().getPluginManager().getPlugin("Skript");
		if (skript != null) {
			try {
				SkriptAddon addonInstance = Skript.registerAddon(this);
				addonInstance.loadClasses("me.sashie.skriptyaml", "skript");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			new MetricsLite(this);
			/*
			Metrics metrics = new Metrics(this);
			metrics.addCustomChart(new Metrics.SimplePie("skript_version", new Callable<String>() {
			    @Override
			    public String call() throws Exception {
			        return Skript.getVersion().toString();
			    }
			}));
			*/
		} else {
			Bukkit.getPluginManager().disablePlugin(this);
			error("Skript not found, plugin disabled.");
		}
	}

	public static SkriptYaml getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance;
	}

	public static void warn(String error) {
		LOGGER.warning("[skript-yaml] " + error);
	}

	public static void error(String error) {
		LOGGER.severe("[skript-yaml] " + error);
	}
}