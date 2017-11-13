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

public class CondYamlExists extends Condition {

	static {
		Skript.registerCondition(CondYamlExists.class, 
				"[skript-]y[a]ml [path[s]] %strings% (of|in|from) %string% exists", 
				"[skript-]y[a]ml [path[s]] %strings% (of|in|from) %string% does(n't| not) exist");
	}

	private Expression<String> path;
	private Expression<String> name;

	@Override
	public boolean check(final Event event) {
		return path.check(event, new Checker<String>() {
			@Override
			public boolean check(final String s) {
				if (!SkriptYaml.YAML_STORE.containsKey(name.getSingle(event)))
					return false;
				if (path.isSingle())
					return SkriptYaml.YAML_STORE.get(name.getSingle(event)).file.contains(path.getSingle(event));
				else {
					String[] paths = (String[]) path.getAll(event);
					boolean check;
					for (String p : paths) {
						check = SkriptYaml.YAML_STORE.get(name.getSingle(event)).file.contains(p);
						if (!check) {
							return false;
						}
					}
					return true;
				}
			}
		}, isNegated());
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "yaml path " + path.toString(event, debug) + " in " +  name.toString(event, debug) + (isNegated() ? (path.isSingle() ? " does" : " do") + " not exist" : "exist");
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		path = (Expression<String>) exprs[0];
		name = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
}