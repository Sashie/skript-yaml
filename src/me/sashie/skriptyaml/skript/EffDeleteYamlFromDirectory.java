package me.sashie.skriptyaml.skript;

import java.io.File;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.AsyncEffectOld;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.StringUtil;

@Name("Delete all YAML from one or more directories")
@Description("Syntax 1: Deletes all YAML files from one or more directories and removes them from memory."
		+ "\nSyntax 2&3: Only deletes any loaded YAML files from one or more directories and removes them from memory."
		+ "\n  - The input is one or more directories (ie. \"plugins/MyAwesomePlugin/\" and \"plugins/skript-yaml/\").")
@Examples({
	"delete all yaml from directory \"/plugins/skript-yaml/test\"",
	" ",
	"delete all loaded yaml from directory \"/plugins/skript-yaml/test\"",
	" ",
	"delete all loaded yaml from directory \"/plugins/skript-yaml/test\" using the filename as the id"
	})
@Since("1.2.1")
public class EffDeleteYamlFromDirectory extends AsyncEffectOld {

	static {
		Skript.registerEffect(EffDeleteYamlFromDirectory.class,
				"delete (all|any) [loaded] [y[a]ml] from [(1¦non[(-| )]relative)] director(y|ies) %strings%",
				"delete (all|any) [loaded] [y[a]ml] from [(1¦non[(-| )]relative)] director(y|ies) %strings% using [the] filename as [the] id");
	}

	private Expression<String> directories;
	private int mark;
	private int matchedPattern;
	private SkriptNode skriptNode;

	@Override
	protected void execute(@Nullable Event event) {
		for (String name : this.directories.getAll(event)) {
			File[] directoryFilter = SkriptYamlUtils.directoryFilter(StringUtil.checkSeparator(name), mark == 1, "Delete", skriptNode);
			if (directoryFilter == null) 
				return;
			for (File yamlFile : directoryFilter) {
				String n = null;
				if (matchedPattern == 0) {
					n = StringUtil.checkLastSeparator(name) + yamlFile.getName();
				} else if (matchedPattern == 1) {
					n = StringUtil.stripExtention(yamlFile.getName());
				}
				if (SkriptYaml.YAML_STORE.containsKey(n)) {
					SkriptYaml.YAML_STORE.get(n).getFile().delete();
					SkriptYaml.YAML_STORE.remove(n);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "delete all" + (matchedPattern == 0 ? " loaded " : " ") + "yaml from" + (mark == 1 ? " non-relative " : " ") + "director" + (directories.isSingle() ? "y " : "ies ") + this.directories.toString(event, b) + (matchedPattern == 2 ? " using the filename as the id" : "");
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
