package me.sashie.skriptyaml.skript;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.FileConfiguration;
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

@Name("All YAML Nodes")
@Description("Gets a list of all nodes of a cached YAML file.")
@Examples({
		"set yaml value \"test1.test2\" from \"config\" to \"test3\"",
		"set yaml value \"boop.beep\" from \"config\" to \"bop\"",
		" ",
		"set {_list::*} to all yaml nodes of \"config\"",
		"broadcast \"%{_list::*}%\""
})
@Since("1.0.5")
public class ExprAllYamlNodes extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAllYamlNodes.class, String.class, ExpressionType.SIMPLE,
				"[all] [[skript-]y[a]ml] (node|path)[s] (of|in|from) %string%");
	}

	private Expression<String> file;

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "all yaml nodes from " + this.file.toString(event, b);
	}

	@Override
	@Nullable
	protected String[] get(Event event) {

		final String name = this.file.getSingle(event);

		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			//SkriptYaml.warn("No yaml file by the name '" + name + "' has been loaded");
			return null;
		}

		FileConfiguration config = SkriptYaml.YAML_STORE.get(name).file;

		Set<String> nodes = config.getKeys(true);
		return nodes.toArray(new String[nodes.size()]);
	}

	@Override
	public void change(Event event, Object[] delta, Changer.ChangeMode mode) {

	}

	@Override
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		file = (Expression<String>) exprs[0];
		return true;
	}
}
