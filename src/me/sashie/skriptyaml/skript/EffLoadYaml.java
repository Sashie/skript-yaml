package me.sashie.skriptyaml.skript;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.AsyncEffectOld;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.StringUtil;
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
public class EffLoadYaml extends AsyncEffectOld {

	static {
		Skript.registerEffect(EffLoadYaml.class, "[re]load [(1¦non[(-| )]relative)] [y[a]ml] %string% [as %-string%]");
	}

	private Expression<String> file;
	private Expression<String> id;
	private int mark;

	@Override
	protected void execute(@Nullable Event event) {
		final String name = StringUtil.checkSeparator(this.file.getSingle(event));
		
		File yamlFile = null;
		if (mark == 1) {
			yamlFile = new File(StringUtil.checkRoot(name));
		} else {
			Path server = Paths.get("").normalize().toAbsolutePath();
			yamlFile = new File(server + File.separator + name);
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
				SkriptYaml.error("[Load Yaml] " + error.getMessage() + " (" + name + ")");
				return;
			}
		}

		YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);

		try {
			yaml.load();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (id != null) {
				SkriptYaml.YAML_STORE.put(this.id.getSingle(event), yaml);
			} else {
				SkriptYaml.YAML_STORE.put(StringUtil.stripExtention(yamlFile.getName()), yaml);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "[re]load yaml " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		file = (Expression<String>) exprs[0];
		id = (Expression<String>) exprs[1];
		this.mark = parse.mark;
		return true;
	}
}
