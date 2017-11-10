package me.sashie.skriptyaml.skript;

import java.io.IOException;
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
				"[skript-]y[a]ml (1¦value|2¦node[s]|3¦node[s with] keys|4¦list) %string% (in|at|from) [id] %string%");
	}

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

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] e, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		if (parse.mark == 1) {
			state = States.VALUE;
		} else if (parse.mark == 2) {
			state = States.NODES;
		} else if (parse.mark == 3) {
			state = States.NODES_KEYS;
		} else if (parse.mark == 4) {
			state = States.LIST;
		}
		node = (Expression<String>) e[0];
		file = (Expression<String>) e[1];
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "[skript-]y[a]ml (1¦value|2¦node[s]|3¦node[s with] keys|4¦list) %string% (in|at|from) [id] " + this.file.toString(event, b);
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {

		final String name = this.file.getSingle(event);
		final String path = this.node.getSingle(event);

		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			SkriptYaml.warn("No yaml file by the name '" + name + "' has been registered/loaded");
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
		Object target = delta[0] == null ? "" : delta[0];
		if (state == States.VALUE) {
			if (mode == ChangeMode.SET) {
				config.set(path, delta[0]);
			}
		} else if (state == States.NODES_KEYS) {
			if (mode == ChangeMode.ADD) {
				config.createSection(path);
			} else if (mode == ChangeMode.REMOVE) {
				config.set(path + "." + target, null);
			}
		} else if (state == States.LIST) {
			if (mode == ChangeMode.ADD) {
				@SuppressWarnings("unchecked")
				ArrayList<Object> objects = (ArrayList<Object>) config.getList(path);
				if (config.getList(path) == null) {
					ArrayList<Object> obj = new ArrayList<>();
					obj.add(delta[0]);
					config.set(path, obj);
				} else {
					objects.add(delta[0]);
				}
			} else if (mode == ChangeMode.REMOVE) {
				config.getList(path).remove(delta[0]);
			}
		}
		
		/*
		try {
			config.save(name);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		*/
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
			if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
				return CollectionUtils.array(Object.class);
			}
		}
		return null;
	}
}