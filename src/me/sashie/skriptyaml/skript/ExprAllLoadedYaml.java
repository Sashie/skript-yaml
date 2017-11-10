package me.sashie.skriptyaml.skript;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;

public class ExprAllLoadedYaml extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAllLoadedYaml.class, String.class, ExpressionType.SIMPLE,
				"(the|all [of the]) [currently] loaded y[a]ml [files]");
	}

	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean init(Expression<?>[] args, int arg1, Kleenean arg2, ParseResult arg3) {
		return true;
	}

	@Override
	public String toString(@Nullable Event arg0, boolean arg1) {
		return "(the|all [of the]) [currently] loaded y[a]ml [files]";
	}

	@Override
	@Nullable
	protected String[] get(Event arg0) {
		if (SkriptYaml.YAML_STORE.isEmpty())
			return null;
		return SkriptYaml.YAML_STORE.keySet().toArray(new String[SkriptYaml.YAML_STORE.keySet().size()]);
	}
}