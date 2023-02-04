package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLFormat;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

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
		"\tload yaml \"plugins/MyAwesomePlugin/config.yml\" as file path",
		"\tset yaml value \"version\" from \"plugins/MyAwesomePlugin/config.yml\" to 1.0",
		"\tbroadcast \"%yaml value \"\"version\"\" from \"\"plugins/MyAwesomePlugin/config.yml\"\"%\""
})
@Since("1.0.0")
public class EffLoadYaml extends Effect {

	static {
		Skript.registerEffect(EffLoadYaml.class, 
				"[re]load [(1¦non[(-| )]relative)] [y[a]ml] %strings%",
				"[re]load [(1¦non[(-| )]relative)] [y[a]ml] %string% as %string%",
				"[re]load [(1¦non[(-| )]relative)] [y[a]ml] %strings% using [the] [file] path[s] as [the] id[s]");
	}

	private Expression<String> file;
	private Expression<String> id;
	private int mark;
	private int matchedPattern;
	private SkriptNode skriptNode;

	@Override
	protected void execute(@Nullable Event event) {
		if (matchedPattern == 1) {
			if (!this.file.isSingle()) {
				SkriptYaml.warn("[Load Yaml] Input has to be single if using a custom id " + skriptNode.toString());
				return;
			}
			load(this.file.getSingle(event), event);
		} else {
			for (String name : this.file.getAll(event))
				load(name, event);
		}
	}

	private void load(String name, Event event) {
		File yamlFile = null;
		String server = new File("").getAbsoluteFile().getAbsolutePath() + File.separator;
		if (mark == 1) {
			yamlFile = new File(StringUtil.checkRoot(StringUtil.checkSeparator(name)));
		} else {
			yamlFile = new File(server + StringUtil.checkSeparator(name));
		}

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
			SkriptYaml.error("[Load Yaml] " + error.getMessage() + " (" + name + ") " + skriptNode.toString());
			return;
		}

		YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);

		try {
			yaml.load();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			String n = null;
			if (matchedPattern == 0)
				n = StringUtil.stripExtention(yamlFile.getName());
			else if (matchedPattern == 1)
				n = this.id.getSingle(event);
			else if (matchedPattern == 2)
				n = name;
			SkriptYaml.YAML_STORE.put(n, yaml);
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "[re]load yaml" + (mark == 1 ? " non-relative " : " ") + this.file.toString(event, b) + (id != null ? " as " + this.id.toString(event, b) : (matchedPattern == 2 ? " using the file path as the id" : ""));
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		file = (Expression<String>) exprs[0];
		if (matchedPattern == 1)
			id = (Expression<String>) exprs[1];
		this.mark = parse.mark;
		this.matchedPattern = matchedPattern;
		this.skriptNode = new SkriptNode(SkriptLogger.getNode());
		return true;
	}
}
