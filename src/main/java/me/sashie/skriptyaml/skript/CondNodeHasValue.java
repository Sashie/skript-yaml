package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Does YAML Path Have Value")
@Description("Checks if one or more values exist at a path in a cached YAML file using said ID." +
		"\n  - First input is the path." +
		"\n  - Second input is the ID." +
		"\n  - If multiple paths are checked at once it will return false on the first one found to not contain a value.")
@Examples({
		"set skript-yaml value \"test.test\" from \"config\" to \"test\"",
		" ",
		"yaml path \"test.test\" in \"config\" has value:",
		"\tbroadcast \"value exists\"",
})
@Since("1.1.0")
public class CondNodeHasValue extends Condition {

	static {
		Skript.registerCondition(CondNodeHasValue.class, 
				"[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% has [a] value[s]", 
				"[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% does(n't| not) have [a] value[s]");
	}

	private Expression<String> path;
	private Expression<String> file;

	@Override
	public boolean check(final Event event) {
		String path = this.path.getSingle(event);
		String file = this.file.getSingle(event);

		YAMLProcessor yaml = SkriptYaml.YAML_STORE.get(file);
		if (yaml == null)
			return isNegated();

		if (this.path.isSingle())
			return (yaml.getProperty(path) != null) ^ isNegated();
		else {
			String[] paths = (String[]) this.path.getAll(event);
			boolean check;
			for (String p : paths) {
				check = (yaml.getProperty(p) != null);
				if (!check) {
					return isNegated();
				}
			}
			return !isNegated();
		}
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "yaml path " + path.toString(event, debug) + " in " +  file.toString(event, debug) + (isNegated() ? (path.isSingle() ? " does not have a value" : " do not have values") + " " : "has a value");
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		path = (Expression<String>) exprs[0];
		file = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
}
