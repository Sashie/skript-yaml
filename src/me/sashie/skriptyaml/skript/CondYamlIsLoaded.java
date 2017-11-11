package me.sashie.skriptyaml.skript;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;

public class CondYamlIsLoaded extends Condition {
	
	static {
		Skript.registerCondition(CondYamlIsLoaded.class,
				"y[a]ml[s] %strings% (is|are) loaded", 
				"y[a]ml[s] %strings% ((are|is) not|(is|are)n[']t) loaded");
	}
	
	private Expression<String> name;
	
	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		name = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event event) {
		return name.check(event, new Checker<String>() {
			@Override
			public boolean check(final String s) {
				if (!SkriptYaml.YAML_STORE.containsKey(name.getSingle(event)))
					return false;
				return true;
			}
		}, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "yaml " + name.toString(e, debug) + (name.isSingle() ? " is " : " are ") + (isNegated() ? "not loaded" : "loaded");
	}
	
}
