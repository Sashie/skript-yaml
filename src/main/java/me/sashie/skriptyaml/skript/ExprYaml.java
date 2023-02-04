package me.sashie.skriptyaml.skript;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.sashie.skriptyaml.SimpleExpressionFork;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLNode;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("YAML")
@Description("Gets, sets, removes values/nodes etc.. of a cached yaml file" +
		"\n  - Requires the id used/created from the load effect" +
		"\n  - This expression does not save to file" +
		"\n  - Lists accept list variables for input" +
		"\n  - Using 'without string checks' optional is a tiny bit faster but doesn't check/convert strings for numbers or booleans")
@Examples({
		"set yaml value \"test1.test2\" from \"config\" to \"test3\"",
		"set yaml list \"list.name\" from \"config\" to {_list::*}",
		" ",
		"set {_test} to yaml value \"test1.test2\" from \"config\"",
		"broadcast \"%{_test}%\""
})
@Since("1.0.0")
public class ExprYaml<T> extends SimpleExpressionFork<T> {

	static {
		Skript.registerExpression(ExprYaml.class, Object.class, ExpressionType.SIMPLE,
				"[[skript-]y[a]ml] (1¦value|2¦(node|path) list|3¦(node|path)[s with] key[s]|4¦list) %string% (of|in|from) %string% [without string checks]");
		//"[[skript-]y[a]ml] (1¦value|2¦(node|path) list|3¦(node|path)[s with] key[s]|4¦list) %string% (of|in|from) %string% [without string checks] [using %-object% as default]"
	}

	private boolean checks = false;
	private Expression<String> node, file;
	private SkriptNode skriptNode;

	public enum YamlState {
		VALUE, NODES, NODE_KEYS, LIST
	}

	private YamlState state;

	private final ExprYaml<?> source;
	private final Class<T> superType;

	@SuppressWarnings("unchecked")
	public ExprYaml() {
		this(null, (Class<? extends T>) Object.class);
	}

	@SuppressWarnings("unchecked")
	private ExprYaml(ExprYaml<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.node = source.node;
			this.file = source.file;
			this.state = source.state;
			this.checks = source.checks;
			this.skriptNode = source.skriptNode;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprYaml<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return getReturnType(state);
	}

	@SuppressWarnings("unchecked")
	public Class<? extends T> getReturnType(YamlState state) {
		if (state == YamlState.NODES || state == YamlState.NODE_KEYS)
			return (Class<? extends T>) String.class;
		return superType;
	}

	public YamlState getState() {
		return state;
	}

	public String getNode(Event event) {
		return node.getSingle(event);
	}

	public String getId(Event event) {
		return file.getSingle(event);
	}

	@Override
	public boolean isSingle() {
		return state == YamlState.VALUE ? true : false;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "yaml " + state.toString().toLowerCase() + " " + this.node.toString(event, b) + " from " + this.file.toString(event, b) + (!checks ? "" : " without string checks");
	}

	@Override
	@Nullable
	protected T[] get(Event event) {
		return get(event, this.node.getSingle(event), this.state);
	}

	public T[] get(Event event, YamlState state) {
		return get(event, this.node.getSingle(event), state);
	}

	public T[] get(Event event, String path) {
		return get(event, path, this.state);
	}

	@SuppressWarnings("unchecked")
	public T[] get(Event event, String path, YamlState state) {
		final String name = this.file.getSingle(event);

		if (!SkriptYamlUtils.yamlExists(name, skriptNode))
			return null;

		YAMLProcessor config = SkriptYaml.YAML_STORE.get(name);

		if (state == YamlState.VALUE) {
			Object o = config.getProperty(path);
			if (o != null) {
				if (!checks && String.class.isAssignableFrom(o.getClass()))
					o = ChatColor.translateAlternateColorCodes('&', ((String) o));
				try {
					return SkriptYamlUtils.convertToArray(o, (Class<T>) o.getClass());
				} catch (ClassCastException e) {
					return (T[]) Array.newInstance((Class<T>) o.getClass(), 0);
				}
			}
			return null;
		} else if (state == YamlState.NODES) {
			if (path.equals("")) {
				Set<String> rootNodes = config.getMap().keySet();
				return lazyConvert(rootNodes.toArray(new String[rootNodes.size()]));
			}
			YAMLNode node = config.getNode(path);
			if (node == null)
				return null;
			Map<String, Object> nodes = node.getMap();
			List<String> keys = new ArrayList<String>();
			for (String key : nodes.keySet()) {
				keys.add(path + "." + key);
			}
			return lazyConvert(keys.toArray(new String[keys.size()]));
		} else if (state == YamlState.NODE_KEYS) {
			List<String> nodesKeys = config.getKeys(path);
			if (nodesKeys == null)
				return null;
			return lazyConvert(nodesKeys.toArray(new String[nodesKeys.size()]));
		} else if (state == YamlState.LIST) {
			List<Object> items = config.getList(path);
			if (items == null)
				return null;
			try {
				return convertArray(items.toArray(), superType);
			} catch (ClassCastException e) {
				return (T[]) Array.newInstance(superType, 0);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public final static <T> T[] lazyConvert(Object[] original) {
		try {
			return convertArray(original, (Class<T>) String.class);
		} catch (ClassCastException e) {
			return (T[]) Array.newInstance((Class<T>) String.class, 0);
		}
	}

	//This method is found at ch.njol.util.coll.CollectionUtils but is here for backwards compatibility with older Skript versions
	@SuppressWarnings("unchecked")
	public final static <T> T[] convertArray(Object[] original, Class<T> to) throws ClassCastException {
		T[] end = (T[]) Array.newInstance(to, original.length);
		for (int i = 0; i < original.length; i++) {
			T converted = Converters.convert(original[i], to);
			if (converted != null) {
				end[i] = converted;
			} else {
				throw new ClassCastException();
			}
		}
		return end;
	}

	@Override
	public void change(Event event, Object[] delta, Changer.ChangeMode mode) {
		final String name = this.file.getSingle(event);
		final String path = this.node.getSingle(event);

		if (!SkriptYamlUtils.yamlExists(name, skriptNode))
			return;

		YAMLProcessor config = SkriptYaml.YAML_STORE.get(name);

		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			config.removeProperty(path);
			return;
		}

		if (state == YamlState.VALUE) {
			if (mode == ChangeMode.SET)
				config.setProperty(path, StringUtil.parseString(delta[0], checks));
		} else if (state == YamlState.NODE_KEYS) {
			if (mode == ChangeMode.ADD)
				config.setProperty(path + (delta[0] == null ? "" : "." + delta[0]), null);
				//config.addNode(path + (delta[0] == null ? "" : "." + delta[0]));
			else if (mode == ChangeMode.REMOVE)
				config.removeProperty(path + (delta[0] == null ? "" : "." + delta[0]));
				//config.setProperty(path + (delta[0] == null ? "" : "." + delta[0]), null);
		} else if (state == YamlState.LIST) {
			List<Object> objects = config.getList(path);
			if (mode == ChangeMode.ADD) {
				if (objects == null)
					config.setProperty(path, arrayToList(new LinkedList<Object>(), delta));
				else 
					config.setProperty(path, arrayToList(objects, delta));
			} else if (mode == ChangeMode.REMOVE) {
				for (Object o : delta)
					objects.remove(StringUtil.parseString(o, checks));
			} else if (mode == ChangeMode.SET) {
				if (objects == null) {
					config.setProperty(path, arrayToList(new LinkedList<Object>(), delta));
				} else {
					objects.clear();
					config.setProperty(path, arrayToList(objects, delta));
				}
			}
		}
	}

	private List<Object> arrayToList(List<Object> list, Object[] array) {
		for (Object o : array)
			list.add(StringUtil.parseString(o, checks));
		return list;
	}

/*TODO	Test for speed later
	private Object parseString(Object delta) {
		if (matchedPattern == 0 && String.class.isAssignableFrom(delta.getClass())) {
			String s = ((String) delta);
			if (isBoolean(s)) {
				return parseBoolean(s);
			} else if (isLong(s)) {
				return Long.parseLong(s);
			} else if (isDouble(s)) {
				return Double.parseDouble(s);
			} else {
				return s;
			}
		}
		return delta;
	}
	
	public static boolean isBoolean(String s) {
        return ((s != null) && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on") ||
        		s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("off")));
    }
	
	public static boolean parseBoolean(String s) {
        return ((s != null) && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on")));
    }
	
	private static final int NUMBER_MAX_LENGTH = String.valueOf(Long.MAX_VALUE).length();

	public static boolean isLong(String string) {
	    if (string == null || string.isEmpty()) {
	        return false;
	    }
	    if (string.length() >= NUMBER_MAX_LENGTH) {
	        try {
	            Long.parseLong(string);
	        } catch (Exception e) {
	            return false;
	        }
	    } else {
	        int i = 0;
	        if (string.charAt(0) == '-') {
	            if (string.length() > 1) {
	                i++;
	            } else {
	                return false;
	            }
	        }
	        for (; i < string.length(); i++) {
	            if (!Character.isDigit(string.charAt(i))) {
	                return false;
	            }
	        }
	    }
	    return true;
	}

	public static boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
	
	boolean isNumber(String str) {
	    for (int i=0; i<str.length(); i++) {
	        char c = str.charAt(i);
	        if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
	            return false;
	    }

	    return true;
	}

	public static boolean isDouble(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        ++i;
	    }
	    int integerPartSize = 0;
	    int exponentPartSize = -1;
	    while (i < length) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            if (c == '.' && integerPartSize > 0 && exponentPartSize == -1) {
	                exponentPartSize = 0;
	            } else {
	                return false;
	            }
	        } else if (exponentPartSize > -1) {
	            ++exponentPartSize;
	        } else {
	            ++integerPartSize;
	        }
	        ++i;
	    }
	    if ((str.charAt(0) == '0' && i > 1 && exponentPartSize < 1)
	            || exponentPartSize == 0 || (str.charAt(length - 1) == '.')) {
	        return false;
	    }
	    return true;
	}
*/
	@Override
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
			return CollectionUtils.array(Object.class);
		}
		if (state == YamlState.VALUE) {
			if (mode == Changer.ChangeMode.SET)
				return CollectionUtils.array(Object.class);
		} else if (state == YamlState.NODE_KEYS) {
			if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE)
				return CollectionUtils.array(Object[].class);
		} else if (state == YamlState.LIST) {
			if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.SET)
				return CollectionUtils.array(Object[].class);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] e, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		if (parse.mark == 1)
			state = YamlState.VALUE;
		else if (parse.mark == 2)
			state = YamlState.NODES;
		else if (parse.mark == 3)
			state = YamlState.NODE_KEYS;
		else if (parse.mark == 4)
			state = YamlState.LIST;
		node = (Expression<String>) e[0];
		file = (Expression<String>) e[1];
		if (parse.expr.toLowerCase().endsWith(" without string checks"))
			this.checks = true;
		this.skriptNode = new SkriptNode(SkriptLogger.getNode());

		return true;
	}

	@Override
	public boolean isLoopOf(final String s) {
		return state != YamlState.VALUE && (s.equalsIgnoreCase("index") || s.equalsIgnoreCase("value")
				|| s.equalsIgnoreCase("id") || s.equalsIgnoreCase("val") || s.equalsIgnoreCase("list")
				|| s.equalsIgnoreCase("node") || s.equalsIgnoreCase("key") || s.toLowerCase().startsWith("subnodekey"));
	}
}
