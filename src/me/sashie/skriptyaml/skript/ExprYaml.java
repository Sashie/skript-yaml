package me.sashie.skriptyaml.skript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.sashie.skriptyaml.SkriptYaml;
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
public class ExprYaml extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprYaml.class, Object.class, ExpressionType.SIMPLE,
				"[[skript-]y[a]ml] (1¦value|2¦(node|path)[s]|3¦(node|path)[s with] keys|4¦list) %string% (of|in|from) %string% [without string checks]");
	}

	private boolean checks = false;
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
		return "yaml " + state.toString().toLowerCase() + " " + this.node.toString(event, b) + " from " + this.file.toString(event, b) + (!checks ? "" : " without string checks");
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		final String name = this.file.getSingle(event);
		final String path = this.node.getSingle(event);

		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			SkriptYaml.warn("No yaml by the name '" + name + "' has been loaded");
			return null;
		}

		YAMLProcessor config = SkriptYaml.YAML_STORE.get(name);

		//if (!config.getAllKeys().contains(path)) {
		//	return null;
		//}
		
		if (state == States.VALUE) {
			Object o = config.getProperty(path);
			if (o != null)
				return CollectionUtils.array(o);
			return null;
		} else if (state == States.NODES) {
			if (path.equals("")) {
				Set<String> rootNodes = config.getMap().keySet();
				return rootNodes.toArray(new String[rootNodes.size()]);
			}
			Map<String, YAMLNode> nodes = config.getNodes(path); 
			if (nodes == null)
				return null;
			return nodes.keySet().toArray(new String[nodes.size()]);
		} else if (state == States.NODES_KEYS) {
			List<String> nodesKeys = config.getKeys(path);
			if (nodesKeys == null)
				return null;
			return nodesKeys.toArray(new String[nodesKeys.size()]);
		} else if (state == States.LIST) {
			List<?> items = config.getList(path);
			if (items == null)
				return null;
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
		
		YAMLProcessor config = SkriptYaml.YAML_STORE.get(name);
		
		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			config.removeProperty(path);
			return;
		}
		
		//TODO add possible warning if setting a value or list with the same path
		if (state == States.VALUE) {
			if (mode == ChangeMode.SET) 
				config.setProperty(path, parseString(delta[0]));
		} else if (state == States.NODES_KEYS) {
			if (mode == ChangeMode.ADD)
				config.addNode(path);
			else if (mode == ChangeMode.REMOVE)
				config.setProperty(path + "." + (delta[0] == null ? "" : delta[0]), null);
		} else if (state == States.LIST) {
			ArrayList<Object> objects = (ArrayList<Object>) config.getList(path);
			if (mode == ChangeMode.ADD) {
				if (objects == null)
					config.setProperty(path, arrayToList(new ArrayList<Object>(), delta));
				else
					arrayToList(objects, delta);
			} else if (mode == ChangeMode.REMOVE) {
				for (Object o : delta)
					objects.remove(parseString(o));
			} else if (mode == ChangeMode.SET) {
				if (objects == null) {
					config.setProperty(path, arrayToList(new ArrayList<Object>(), delta));
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
		if (!checks && String.class.isAssignableFrom(delta.getClass())) {
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
		if (parse.expr.toLowerCase().endsWith(" without string checks"))
			this.checks = true;
		return true;
	}
}
