package me.sashie.skriptyaml.skript;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;

@Name("Return All Cached YAML")
@Description("Returns a list of all \"cached\" yaml file IDs.")
@Examples({
		"set {_list::*} to the currently loaded yaml files",
		"broadcast \"%{_list::*}%\""
})
@Since("1.0.0")
public class ExprAllLoadedYaml extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAllLoadedYaml.class, String.class, ExpressionType.SIMPLE,
				"[(the|all (of the|the))] [currently] loaded y[a]ml [files]");
	}

	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "(the|all [of the]) [currently] loaded y[a]ml [files]";
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		if (SkriptYaml.YAML_STORE.isEmpty())
			return null;
		return SkriptYaml.YAML_STORE.keySet().toArray(new String[SkriptYaml.YAML_STORE.keySet().size()]);
	}

	@Override
	public void change(Event event, Object[] delta, Changer.ChangeMode mode) {

	}

	@Override
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		return null;
	}
}
