package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.sashie.skriptyaml.SimpleExpressionFork;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

@Name("YAML list value")
@Description("Gets, sets, removes values from a list from a cached yaml file using an index" +
		"\n  - Requires index between 1 and the size of the list" +
		"\n  - Requires the id used/created from the load effect" +
		"\n  - This expression does not save to file" +
		"\n  - Using 'without string checks' optional is a tiny bit faster but doesn't check/convert strings for numbers or booleans")
@Examples({
		"set index 1 in yaml list \"test1.test2\" from \"config\" to \"test3\"",
		" ",
		"set {_test} to yaml index 1 in list \"test1.test2\" from \"config\"",
		"broadcast \"%{_test}%\""
})
@Since("1.3.3")
public class ExprListValue<T> extends SimpleExpressionFork<T> {

	static {
		Skript.registerExpression(ExprListValue.class, Object.class, ExpressionType.SIMPLE,
				"(index|value) %number% (of|in|from) [skript-]y[a]ml list %string% (of|in|from) %string% [without string checks]");
	}

	private boolean checks = false;
	private Expression<Number> index;
	private Expression<String> path, name;
	private SkriptNode skriptNode;

	private final ExprListValue<?> source;
	private final Class<T> superType;

	@SuppressWarnings("unchecked")
	public ExprListValue() {
		this(null, (Class<? extends T>) Object.class);
	}

	@SuppressWarnings("unchecked")
	private ExprListValue(ExprListValue<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.index = source.index;
			this.path = source.path;
			this.name = source.name;
			this.checks = source.checks;
			this.skriptNode = source.skriptNode;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprListValue<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "index " + this.index.toString(event, b) + " in list " + this.path.toString(event, b) + " from " + this.name.toString(event, b) + (!checks ? "" : " without string checks");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	protected T[] get(Event event) {
		final String path = this.path.getSingle(event);
		final int index = this.index.getSingle(event).intValue();

		Object[] objects = check(event, this.name.getSingle(event), path, index, false, null);
		if (objects == null)
			return null;

		List<Object> items = (List<Object>) objects[0];

		Object o = items.get(index - 1);
		if (o != null) {
			if (!checks && String.class.isAssignableFrom(o.getClass()))
				o = StringUtil.translateColorCodes((String) o);
			try {
				return SkriptYamlUtils.convertToArray(o, (Class<T>) o.getClass());
			} catch (ClassCastException e) {
				return (T[]) Array.newInstance((Class<T>) o.getClass(), 0);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void change(Event event, Object[] delta, ChangeMode mode) {
		final String path = this.path.getSingle(event);
		final int index = this.index.getSingle(event).intValue();

		Object[] objects = check(event, this.name.getSingle(event), path, index, true, delta);
		if (objects == null)
			return;

		List<Object> items = (List<Object>) objects[0];

		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			items.remove(index - 1);
		} else if (mode == ChangeMode.SET) {
			items.set(index - 1, StringUtil.parseString(delta[0], checks));
		}
		((YAMLProcessor) objects[1]).setProperty(path, items);
	}

	public Object[] check(Event event, String name, String path, int index, boolean alsoReturnConfig, Object[] delta) {
		YAMLProcessor config = SkriptYamlUtils.yamlExists(name, skriptNode);
		if (config == null)
			return null;
		Object[] objects;
		List<Object> items;
		if (alsoReturnConfig) {
			items = ((YAMLProcessor) ((objects = new Object[2])[1] = config)).getList(path);
		} else {
			objects = new Object[1];
			items = config.getList(path);
		}
		if (items == null && delta != null) {
			// If no list is initialized then we'll do it here
			config.setProperty(path, ExprYaml.arrayToList(new LinkedList<Object>(), delta, checks));
			items = config.getList(path);
		}
		if (items == null) {
			SkriptYaml.warn("The node '" + path + "' in yaml '" + name + "' is not a list " + skriptNode.toString());
			return null;
		}
		if (index < 1 || index > items.size()) {
			SkriptYaml.warn("The index of node '" + path + "' in yaml '" + name + "' needs to be between 1 and " + items.size() + " " + skriptNode.toString());
			return null;
		}
		objects[0] = items;
		return objects;
	}

	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
			return CollectionUtils.array(Object.class);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] e, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		index = (Expression<Number>) e[0];
		path = (Expression<String>) e[1];
		name = (Expression<String>) e[2];
		if (parse.expr.toLowerCase().endsWith(" without string checks"))
			this.checks = true;
		skriptNode = new SkriptNode(SkriptLogger.getNode());
		return true;
	}
}