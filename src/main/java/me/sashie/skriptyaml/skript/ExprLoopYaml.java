package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.iterator.ArrayIterator;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.skript.ExprYaml.YamlState;
import me.sashie.skriptyaml.skript.loops.AbstractLoopExpression;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.versions.wrapper.AbstractExpressionInitializer;
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
public class ExprLoopYaml extends AbstractLoopExpression<Object> {
	static {
		if (Skript.getVersion().getMajor() >= 2 && Skript.getVersion().getMinor() >= 8) {
			Skript.registerExpression(ExprLoopYaml.class, Object.class, ExpressionType.SIMPLE, "[the] loop-(1¦id|2¦val|3¦list|4¦node|5¦key|6¦subnodekey[s])[-%-*integer%]");
		} else {
			Skript.registerExpression(ExprLoopYaml.class, Object.class, ExpressionType.SIMPLE, "[the] loop-(1¦id|2¦val|3¦list|4¦node|5¦key|6¦subnodekey[s]|7¦iteration)[-%-*integer%]");
		}
	}

	public static enum LoopState {
		ID, VALUE, LIST, NODE, NODE_KEY, SUB_NODE_KEYS, INDEX
	}


	YamlState yamlState;
	LoopState loopState;

	boolean isYamlLoop = false;

	private boolean loopStateListError(String s) {
		SkriptYaml.error("There's no 'loop-" + s + "' in a yaml list " + getNodeMsg());
		return false;
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
	public Class<?> getReturnType() {
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
	public boolean isIntendedLoop(AbstractLoop loop, AbstractExpressionInitializer initializer) {
		if (loop.getLoopedExpression() instanceof ExprYaml) {
			yamlState = ((ExprYaml<?>) loop.getLoopedExpression()).getState();
			if (!yamlState.equals(YamlState.VALUE)) {
				if (initializer.parser().mark == 7) {
					loopState = LoopState.INDEX;
				} else if (initializer.parser().mark == 1) {
					loopState = LoopState.ID;
				} else if (initializer.parser().mark == 2) {
					loopState = LoopState.VALUE;
				} else if (initializer.parser().mark == 3) {
					loopState = LoopState.LIST;
				} else if (initializer.parser().mark == 4) {
					if (yamlState.equals(YamlState.LIST))
						return loopStateListError(s);
					loopState = LoopState.NODE;
				} else if (initializer.parser().mark == 5) {
					if (yamlState.equals(YamlState.LIST))
						return loopStateListError(s);
					loopState = LoopState.NODE_KEY;
				} else if (initializer.parser().mark == 6) {
					if (yamlState.equals(YamlState.LIST))
						return loopStateListError(s);
					loopState = LoopState.SUB_NODE_KEYS;
				}
			}
		}
		SkriptYaml.error("A 'loop-" + s + "' can only be used in a yaml expression loop ie. 'loop yaml node keys \"node\" from \"config\"' " + getNodeMsg());
		return false;
	}

	@Override
	public Class<? extends Expression> getExpressionToLoop() {
		return ExprYaml.class;
	}
}