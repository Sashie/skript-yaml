package me.sashie.skriptyaml.skript;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.sashie.skriptyaml.SkriptYaml;

public class ExprYaml extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprYaml.class, Object.class, ExpressionType.SIMPLE,
				"[[skript-]y[a]ml] (1¦value|2¦(node|path)[s]|3¦(node|path)[s with] keys|4¦list) %string% (of|in|from) %string%",
				"[[skript-]y[a]ml] (1¦value|2¦(node|path)[s]|3¦(node|path)[s with] keys|4¦list) %string% (of|in|from) %string% without string checks");
	}

	private int matchedPattern;
	private Expression<String> node, file;

	private static enum States {
		VALUE, NODES, NODES_KEYS, LIST
	}

	private States state;

	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}

	@Override
	public boolean isSingle() {
		return state == States.VALUE ? true : false;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "yaml " + state.toString().toLowerCase() + " " + this.node.toString(event, b) + " from " + this.file.toString(event, b) + (matchedPattern == 0 ? "" : " without string checks");
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		final String name = this.file.getSingle(event);
		final String path = this.node.getSingle(event);

		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			SkriptYaml.warn("No yaml file by the name '" + name + "' has been loaded");
			return null;
		}

		FileConfiguration config = SkriptYaml.YAML_STORE.get(name).file;

		if (!config.contains(path)) {
			return null;
		}
		if (state == States.VALUE) {
			return CollectionUtils.array(config.get(path));
		} else if (state == States.NODES) {
			Set<String> nodes = config.getConfigurationSection(path).getKeys(false);
			return nodes.toArray(new String[nodes.size()]);
		} else if (state == States.NODES_KEYS) {
			Set<String> nodesKeys = config.getConfigurationSection(path).getKeys(true);
			return nodesKeys.toArray(new String[nodesKeys.size()]);
		} else if (state == States.LIST) {
			List<?> items = config.getList(path);
			return items.toArray();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void change(Event event, Object[] delta, Changer.ChangeMode mode) {
		final String name = this.file.getSingle(event);
		final String path = this.node.getSingle(event);
		
		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			SkriptYaml.warn("No yaml by the name '" + name + "' has been loaded");
			return;
		}

		FileConfiguration config = SkriptYaml.YAML_STORE.get(name).file;

		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			config.set(path, null);
			return;
		}
		//TODO add possible warning if setting a value or list with the same path
		if (state == States.VALUE) {
			if (mode == ChangeMode.SET) 
				config.set(path, parseString(delta[0]));
		} else if (state == States.NODES_KEYS) {
			if (mode == ChangeMode.ADD)
				config.createSection(path);
			else if (mode == ChangeMode.REMOVE)
				config.set(path + "." + (delta[0] == null ? "" : delta[0]), null);
		} else if (state == States.LIST) {
			ArrayList<Object> objects = (ArrayList<Object>) config.getList(path);
			if (mode == ChangeMode.ADD) {
				if (objects == null)
					config.set(path, arrayToList(new ArrayList<Object>(), delta));
				else
					arrayToList(objects, delta);
			} else if (mode == ChangeMode.REMOVE) {
				for (Object o : delta)
					objects.remove(parseString(o));
			} else if (mode == ChangeMode.SET) {
				if (objects == null) {
					config.set(path, arrayToList(new ArrayList<Object>(), delta));
				} else {
					objects.clear();
					arrayToList(objects, delta);
				}
			}
		}
	}

	private ArrayList<Object> arrayToList(ArrayList<Object> list, Object[] array) {
		for (Object o : array)
			list.add(parseString(o));
		return list;
	}

	private Object parseString(Object delta) {
		if (matchedPattern == 0 && String.class.isAssignableFrom(delta.getClass())) {
			String s = ((String) delta);
			//if (s.matches("true|false")) {
			//	config.set(path, Boolean.valueOf(s));
			if (s.matches("true|false|yes|no|on|off")) {
				return s.matches("true|yes|on");
			} else if (s.matches("(-)?\\d+")) {
				return Long.parseLong(s);
			} else if (s.matches("(-)?\\d+(\\.\\d+)")) {
				return Double.parseDouble(s);
			} else {
				return s;
			}
		}
		return delta;
	}

	@Override
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
			return CollectionUtils.array(Object.class);
		}
		if (state == States.VALUE) {
			if (mode == Changer.ChangeMode.SET) {
				return CollectionUtils.array(Object.class);
			}
		} else if (state == States.LIST) {
			if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.SET) {
				return CollectionUtils.array(Object[].class);
				
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] e, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		if (parse.mark == 1)
			state = States.VALUE;
		else if (parse.mark == 2)
			state = States.NODES;
		else if (parse.mark == 3)
			state = States.NODES_KEYS;
		else if (parse.mark == 4)
			state = States.LIST;
		
		node = (Expression<String>) e[0];
		file = (Expression<String>) e[1];
		this.matchedPattern = matchedPattern;
		return true;
	}
}