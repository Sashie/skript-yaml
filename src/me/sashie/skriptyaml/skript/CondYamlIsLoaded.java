package me.sashie.skriptyaml.skript;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;

@Name("Is YAML Loaded")
@Description("Checks if one or more YAML files are loaded into memory using said ID.")
@Examples({
		"yaml \"config\" is loaded:",
		" ",
		"yaml \"config\" and \"messages\" aren't loaded:",
})
@Since("1.0.3")
public class CondYamlIsLoaded extends Condition {

	static {
		Skript.registerCondition(CondYamlIsLoaded.class,
				"y[a]ml[s] %strings% (is|are) loaded", 
				"y[a]ml[s] %strings% ((are|is) not|(is|are)n[']t) loaded");
	}

	private Expression<String> file;

	@Override
	public boolean check(final Event event) {
		return file.check(event, new Checker<String>() {
			@Override
			public boolean check(final String s) {
				if (!SkriptYaml.YAML_STORE.containsKey(file.getSingle(event)))
					return false;
				return true;
			}
		}, isNegated());
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "yaml " + file.toString(e, debug) + (file.isSingle() ? " is " : " are ") + (isNegated() ? "not loaded" : "loaded");
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		file = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
}
