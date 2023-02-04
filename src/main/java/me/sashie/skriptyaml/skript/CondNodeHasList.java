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
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.List;

@Name("Does YAML Path Have List")
@Description("Checks if one or more paths contain a list in a cached YAML file using said ID." +
		"\n  - First input is the path." +
		"\n  - Second input is the ID.")
@Examples({
		"if yaml node \"listnode\" from \"example\" has list:",
		"\tloop yaml list \"listnode\" from \"example\":",
		"\t\tbroadcast \"%loop-val%\"",
})
@Since("1.3")
public class CondNodeHasList extends Condition {

	static {
		Skript.registerCondition(CondNodeHasList.class, 
				"[skript-]y[a]ml [(node|path)[s]] %string% (of|in|from) %string% has [a] list", 
				"[skript-]y[a]ml [(node|path)[s]] %string% (of|in|from) %string% does(n't| not) have [a] list");
	}

	private Expression<String> path;
	private Expression<String> file;

	@Override
	public boolean check(final Event event) {
		if (!SkriptYaml.YAML_STORE.containsKey(file.getSingle(event)))
			return false;
		Object o =  SkriptYaml.YAML_STORE.get(file.getSingle(event)).getProperty(path.getSingle(event));
		return o != null ? (o instanceof List) ^ isNegated() : false ^ isNegated();
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "yaml path " + path.toString(event, debug) + " in " +  file.toString(event, debug) + (isNegated() ? " is not a list " + " " : "is a list");
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
