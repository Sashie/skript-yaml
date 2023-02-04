package me.sashie.skriptyaml.skript;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

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
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLFormat;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("Load all YAML from directory")
@Description("Loads a directory YAML files into memory."
		+ "\n  - The input is a directory (ie. \"plugins/MyAwesomePlugin/\")."
		+ "\n  - If for example a file in that directory is named test.yml then the output ID would be 'plugins/MyAwesomePlugin/test.yml'"
		+ "\n  - Using the optional filename ID would output `test.yml`")
@Examples({ "#This isn't something you would really want to do, or is it?",
		"load all yaml from directory \"/plugins/skript-yaml/test\"",
		"loop all of the currently loaded yaml files:",
		"\tloop yaml nodes \"\" from loop-value-1:",
		"\t\tloop yaml nodes loop-value-2 from loop-value-1:",
		"\t\t\tbroadcast yaml value \"%loop-value-2%.%loop-value-3%\" from loop-value-1" })
@Since("1.2")
public class EffLoadYamlDirectory extends Effect {

	static {
		Skript.registerEffect(EffLoadYamlDirectory.class, 
				"[re]load all [y[a]ml] from [(1¦non[(-| )]relative)] director(y|ies) %strings%",
				"[re]load all [y[a]ml] from [(1¦non[(-| )]relative)] director(y|ies) %strings% using [the] filename as [the] id");
	}

	private Expression<String> directories;
	private int mark;
	private int matchedPattern;
	private SkriptNode skriptNode;

	@Override
	protected void execute(@Nullable Event event) {
		for (String name : this.directories.getAll(event)) {
			File[] directoryFilter = SkriptYamlUtils.directoryFilter(StringUtil.checkSeparator(name), mark == 1, "Load", skriptNode);
			if (directoryFilter == null) 
				return;
			for (File yamlFile : directoryFilter) {
				YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);
				try {
					yaml.load();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (matchedPattern == 1)
						SkriptYaml.YAML_STORE.put(StringUtil.stripExtention(yamlFile.getName()), yaml);
					else
						SkriptYaml.YAML_STORE.put(StringUtil.checkLastSeparator(name) + yamlFile.getName(), yaml);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "[re]load all yaml from director(y|ies) " + this.directories.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		directories = (Expression<String>) exprs[0];
		this.mark = parse.mark;
		this.matchedPattern = matchedPattern;
		this.skriptNode = new SkriptNode(SkriptLogger.getNode());
		return true;
	}
}
