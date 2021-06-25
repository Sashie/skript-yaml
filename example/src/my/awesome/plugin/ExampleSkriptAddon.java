package example.src.my.awesome.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.sashie.skriptyaml.ExampleClass;
import me.sashie.skriptyaml.SkriptYaml;

public class ExampleSkriptAddon extends JavaPlugin {

	private static ExampleSkriptAddon instance;
	
	public ExampleSkriptAddon() {
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
			/*
			try {
				SkriptAddon addonInstance = Skript.registerAddon(this);
				addonInstance.loadClasses("my.awesome.plugin", "skript");
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
			Plugin skriptYaml = Bukkit.getServer().getPluginManager().getPlugin("skript-yaml");
			if (skriptYaml != null) {
				if (SkriptYaml.isTagRegistered("example")) {
					
					SkriptYaml.registerTag(this, "example", ExampleClass.class, new ExampleRepresentedClass(), new ExampleConstructedClass());
					//config.setProperty(path + "potatotest", new ExampleClass("boop1", "beep2", null));
					Bukkit.broadcastMessage("skript-yaml found, hooks enabled.");
				}
			}
		} else {
			Bukkit.getPluginManager().disablePlugin(this);
			Bukkit.broadcastMessage("Skript not found, plugin disabled.");
		}
	}

	public static ExampleSkriptAddon getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance;
	}
}