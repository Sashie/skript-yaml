package me.sashie.skriptyaml.skript;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.AsyncEffect;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.yaml.YAMLFormat;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("Load YAML")
@Description("Loads a YAML file into memory." +
		"\n  - The first input is the YAML file path (ie. \"plugins/MyAwesomePlugin/config.yml\")." +
		"\n  - The second input allows you to choose your own ID for this file." +
		"\n  - If the second input isn't used then the files name minus the extention is used as the ID for example `config.yml` becomes `config`.")
@Examples({
		"#Both examples produce the same id for use in other effects/expressions",
		"load yaml \"plugins/MyAwesomePlugin/config.yml\"",
		"load yaml \"plugins/MyAwesomePlugin/config.yml\" as \"config\"",
		" ",
		"#to get similar function as the other addons you would do this sort of thing with the id...",
		"\tload yaml \"plugins/MyAwesomePlugin/config.yml\" as \"plugins/MyAwesomePlugin/config.yml\"",
		"\tset yaml value \"version\" from \"plugins/MyAwesomePlugin/config.yml\" to 1.0",
		"\tbroadcast \"%yaml value \"\"version\"\" from \"\"plugins/MyAwesomePlugin/config.yml\"\"%\""
})
@Since("1.0.0")
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

		YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);

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
