package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import me.sashie.skriptyaml.SimpleExpressionFork;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.skript.ExprYaml.YamlState;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.versions.wrapper.AbstractLoop;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;

@Name("Yaml Loop")
@Description("The currently looped value of a yaml expression.")
@Examples({"",
		"loop yaml node keys \"node\" from \"config\":",
		"	message yaml value loop-node from loop-id",
		"loop yaml node list \"node\" from \"config\":",
		"	message yaml value loop-node from loop-id"})
@Since("1.3")
public class ExprLoopYaml extends SimpleExpressionFork<Object> {
	private static boolean IS_2_8 = false;
	static {
		if (Skript.getVersion().getMajor() >= 2 && Skript.getVersion().getMinor() >= 8) {
			Skript.registerExpression(ExprLoopYaml.class, Object.class, ExpressionType.SIMPLE, "[the] loop-(1¦id|2¦val|3¦list|4¦node|5¦key|6¦subnodekey[s])[-%-*integer%]");
			IS_2_8 = true;
		} else {
			Skript.registerExpression(ExprLoopYaml.class, Object.class, ExpressionType.SIMPLE, "[the] loop-(1¦id|2¦val|3¦list|4¦node|5¦key|6¦subnodekey[s]|7¦iteration)[-%-*integer%]");
		}
	}

	public static enum LoopState {
		ID, VALUE, LIST, NODE, NODE_KEY, SUB_NODE_KEYS, INDEX
	}

	private String name;
	private Expression<Integer> number;

	private AbstractLoop loop;

	YamlState yamlState;
	LoopState loopState;

	boolean isYamlLoop = false;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		name = parser.expr;
		number = (Expression<Integer>) vars[0];

		String s = name.split("-")[1];

		int i = -1;
		if (number != null) {
			i = ((Literal<Integer>) number).getSingle().intValue();
		}

		AbstractLoop loop = SkriptYaml.getInstance().getSkriptAdapter().getLoop(i, s);

		if (loop == null) {
			SkriptYaml.error("There are multiple loops that match loop-" + s + ". Use loop-" + s + "-1/2/3/etc. to specify which loop's value you want. " + getNodeMsg());
			return false;
		}

		if (loop != null && loop.getObject() == null) {
			SkriptYaml.error("There's no loop that matches 'loop-" + s + "' " + getNodeMsg());
			return false;
		}

		if (loop.getLoopedExpression() instanceof ExprYaml) {
			yamlState = ((ExprYaml<?>) loop.getLoopedExpression()).getState();
			if (!yamlState.equals(YamlState.VALUE)) {
				if (parser.mark == 7) {
					loopState = LoopState.INDEX;
				} else if (parser.mark == 1) {
					loopState = LoopState.ID;
				} else if (parser.mark == 2) {
					loopState = LoopState.VALUE;
				} else if (parser.mark == 3) {
					loopState = LoopState.LIST;
				} else if (parser.mark == 4) {
					if (yamlState.equals(YamlState.LIST))
						return loopStateListError(s);
					loopState = LoopState.NODE;
				} else if (parser.mark == 5) {
					if (yamlState.equals(YamlState.LIST))
						return loopStateListError(s);
					loopState = LoopState.NODE_KEY;
				} else if (parser.mark == 6) {
					if (yamlState.equals(YamlState.LIST))
						return loopStateListError(s);
					loopState = LoopState.SUB_NODE_KEYS;
				}
			}
			isYamlLoop = true;
		} else {
			SkriptYaml.error("A 'loop-" + s + "' can only be used in a yaml expression loop ie. 'loop yaml node keys \"node\" from \"config\"' " + getNodeMsg());
			return false;
		}

		this.loop = loop;
		return true;
	}

	private boolean loopStateListError(String s) {
		SkriptYaml.error("There's no 'loop-" + s + "' in a yaml list " + getNodeMsg());
		return false;
	}

	private String getNodeMsg() {
		ch.njol.skript.config.Node n = SkriptLogger.getNode();
		if (n == null) {
			return "";
		}
		return "[script: " + n.getConfig().getFileName() + ", line: " + n.getLine() + " : '" + n.save().trim() + "']";
	}

	@Override
	public boolean isSingle() {
		if (loopState == LoopState.VALUE && yamlState == YamlState.LIST)
			return true;
		return yamlState == YamlState.VALUE;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	protected <R> ConvertedExpression<Object, ? extends R> getConvertedExpr(final Class<R>... to) {
		if (isYamlLoop && loopState != LoopState.INDEX) {
			Class<R> superType = (Class<R>) Utils.getSuperType(to);
			Class<?> converterClass = SkriptYaml.getInstance().getSkriptAdapter().getConverterClass();

			Object converter = Proxy.newProxyInstance(
					converterClass.getClassLoader(),
					new Class<?>[]{converterClass},
					new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) {
							if (method.getName().equals("convert") && args != null && args.length == 1) {
								Object o = args[0];
								return SkriptYaml.getInstance().getSkriptAdapter().convert(o, to);
							}
							return null;
						}
					});
			return SkriptYaml.getInstance().getSkriptAdapter().getConvertedExpr(this, superType, converter);
		} else {
			return super.getConvertedExpr(to);
		}
	}

	@Override
	public Class<? extends Object> getReturnType() {
		if (loopState == LoopState.INDEX)
			return Number.class;
		else if (loopState == LoopState.ID || loopState == LoopState.NODE_KEY || loopState == LoopState.NODE)
			return String.class;
		return ((ExprYaml<?>) loop.getLoopedExpression()).getReturnType(yamlState);
	}

	@Override
	@Nullable
	protected Object[] get(final Event e) {
		if (isYamlLoop) {
			final Object current = loop.getCurrent(e);
			ExprYaml<?> yamlExpr = ((ExprYaml<?>) loop.getLoopedExpression());
			if (current == null)
				return null;

			switch (loopState) {
				case INDEX:
					return new Number[] {getIndex()};
				case ID:
					return new String[] {yamlExpr.getId(e)};
				case VALUE:
					if (yamlState.equals(YamlState.LIST))
						return new Object[] {current};
					String n = getCurrentNode(current, yamlExpr.getNode(e));
					if (n == null)
						return null;
					return yamlExpr.get(e, n, YamlState.VALUE);
				case LIST:
					String n2 = getCurrentNode(current, yamlExpr.getNode(e));
					if (n2 == null)
						return null;
					return yamlExpr.get(e, n2, YamlState.LIST);
				case NODE:
					if (yamlState.equals(YamlState.NODE_KEYS))
						return new String[] {StringUtil.addLastNodeSeperator(yamlExpr.getNode(e)) + current};
					else if (yamlState.equals(YamlState.NODES))
						return new String[] {current.toString()};
				case NODE_KEY:
					if (yamlState.equals(YamlState.NODE_KEYS))
						return new String[] {current.toString()};
					else if (yamlState.equals(YamlState.NODES))
						return new String[] {StringUtil.stripBeforeLastNode(current.toString())};
				case SUB_NODE_KEYS:
					String n3 = getCurrentNode(current, yamlExpr.getNode(e));
					if (n3 == null)
						return null;
					Object[] objects = yamlExpr.get(e, n3);
					if (objects == null)
						return null;
					return objects;
				default:
					break;
			}
		}
		return null;
	}

	private String getCurrentNode(Object current, String node) {
		String key = null;
		if (yamlState.equals(YamlState.NODE_KEYS))
			key = StringUtil.addLastNodeSeperator(node) + current;
		else if (yamlState.equals(YamlState.NODES))
			key = current.toString();
		return key;
	}

	@SuppressWarnings("unchecked")
	public Number getIndex() {
		try {
			Field currentIterField = loop.getLoopClass().getDeclaredField("currentIter");
			currentIterField.setAccessible(true);
			Field indexField = ArrayIterator.class.getDeclaredField("index");
			indexField.setAccessible(true);
			for (Iterator<?> entry : ((Map<Event, Iterator<?>>) currentIterField.get(loop.getObject())).values()) {
				return ((int) indexField.get(entry));
			}
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {	//TODO
		if (e == null)
			return name;
		if (isYamlLoop) {
			final Object current = loop.getCurrent(e);
			Object[] objects = ((ExprYaml<?>) loop.getLoopedExpression()).get(e);

			if (current == null || objects == null)
				return Classes.getDebugMessage(null);

			return loopState == LoopState.INDEX ? "\"" + getIndex() + "\"" : Classes.getDebugMessage(current);
		}
		return Classes.getDebugMessage(loop.getCurrent(e));
	}
}