package me.sashie.skriptyaml.skript;

import java.io.File;
import java.io.FilenameFilter;
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

@Name("Delete all loaded YAML from directory")
@Description("Only deletes a directory of loaded YAML files and removes them from memory."
		+ "\n  - The input is a directory (ie. \"plugins/MyAwesomePlugin/\")."
		+ "\n  - If for example a file in that directory is named test.yml then the output ID would be 'plugins/MyAwesomePlugin/test.yml'"
		+ "\n  - Using the optional filename ID would output `test.yml`")
@Examples({"delete all yaml from directory \"/plugins/skript-yaml/test\"" })
@Since("1.2.1")
public class EffDeleteLoadedYamlFromDirectory extends AsyncEffectOld {

	static {
		Skript.registerEffect(EffDeleteLoadedYamlFromDirectory.class, 
				"delete (all|any) loaded [y[a]ml] from [(1¦non[(-| )]relative)] director(y|ies) %strings%",
				"delete (all|any) loaded [y[a]ml] from [(1¦non[(-| )]relative)] director(y|ies) %strings% using [the] filename as [the] id");
	}

	private Expression<String> file;
	private int mark;
	private int matchedPattern;

	public File[] filter(String name) {
		File dir = null;

		if (mark == 1) {
			dir = new File(StringUtil.checkRoot(name));
		} else {
			Path server = Paths.get("").normalize().toAbsolutePath();
			dir = new File(server + File.separator + name);
		}

		if(!dir.isDirectory()) {
			SkriptYaml.warn("[Delete Yaml] " + name + " is not a directory!");
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

	@Override
	protected void execute(@Nullable Event event) {
		for (String name : this.file.getAll(event)) {
			for (File yamlFile : filter(StringUtil.checkSeparator(name))) {
				String n;
				if (matchedPattern == 1) {
					n = yamlFile.getName();
				} else {
					n = StringUtil.checkLastSeparator(name) + yamlFile.getName();
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
		return "delete all yaml from director(y|ies) " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		file = (Expression<String>) exprs[0];
		this.mark = parse.mark;
		this.matchedPattern = matchedPattern;
		return true;
	}
}
