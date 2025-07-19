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

@Name("Is YAML Empty")
@Description("Checks if a cached YAML file using said ID is empty." +
		"\n  - Input is the ID.")
@Examples({
		"yaml \"config\" is empty:",
		"\tbroadcast \"is empty\"",
})
@Since("1.1.5")
public class CondYamlIsEmpty extends Condition {

	static {
		Skript.registerCondition(CondYamlIsEmpty.class, 
				"[skript-]y[a]ml %string% is empty", 
				"[skript-]y[a]ml %string% is(n't| not) empty");
	}

	private Expression<String> file;

	@Override
	public boolean check(final Event event) {
		YAMLProcessor yaml = SkriptYaml.YAML_STORE.get(file.getSingle(event));
		if (yaml == null)
			return isNegated();
		return yaml.getAllKeys().isEmpty() ^ isNegated();
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "yaml " +  file.toString(event, debug) + (isNegated() ? " is empty" : "isn't empty");
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		file = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
}
