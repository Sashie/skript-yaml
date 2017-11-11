package me.sashie.skriptyaml.skript;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.AsyncEffect;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.YamlFile;

public class EffLoadYaml extends AsyncEffect {

	static {
		Skript.registerEffect(EffLoadYaml.class, "[re]load [y[a]ml] %string% [as %-string%]");
	}

	private Expression<String> file;
	private Expression<String> id;

	@Override
	protected void execute(@Nullable Event event) {
		final String name = this.file.getSingle(event);

		String server = (new File("").getAbsolutePath()) + File.separator;
		File yamlFile = new File(server + name);
		if (name.contains("/")) {
			yamlFile = new File(server + name.replaceAll("/", Matcher.quoteReplacement(File.separator)));
		}

		if (!yamlFile.exists()) {
			//SkriptYaml.warn("No yaml file by the name '" + yamlFile.getName() + "' exists at that location");
			//return;
			//SkriptYaml.warn("No yaml by the name '" + yamlFile.getName() + "' exists at that location, generating one...");
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
				error.printStackTrace();
			}
		}

		YamlFile yaml = new YamlFile();
		yaml.file = YamlConfiguration.loadConfiguration(yamlFile);
		yaml.path = name;

		if (null != id) {
			SkriptYaml.YAML_STORE.put(this.id.getSingle(event), yaml);
		} else {
			String n = yamlFile.getName();
			int pos = n.lastIndexOf(".");
			if (pos > 0) {
			    n = n.substring(0, pos);
			}
			SkriptYaml.YAML_STORE.put(n, yaml);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "[re]load yaml " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		file = (Expression<String>) exprs[0];
		id = (Expression<String>) exprs[1];
		return true;
	}
}