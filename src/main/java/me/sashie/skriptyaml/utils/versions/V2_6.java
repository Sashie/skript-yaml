package me.sashie.skriptyaml.utils.versions;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.skript.ExprYaml;
import me.sashie.skriptyaml.utils.versions.wrapper.SkriptSecLoop;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class V2_6 implements SkriptAdapter {

	private Field delayedField;
	private Class<?> converterClass, converterInfoClass;
	private Method convertMethod, convertMethod2, getTimeStampMethod;

	public V2_6() {
		try {
			converterClass = Class.forName("ch.njol.skript.classes.Converter");
			converterInfoClass = Class.forName("ch.njol.skript.classes.Converter.ConverterInfo");
			Class<?> convertersClass = Class.forName("ch.njol.skript.registrations.Converters");
			convertMethod = convertersClass.getMethod("convert", Object.class, Class[].class);
			convertMethod2 = convertersClass.getMethod("convert", Object.class, Class.class);
			delayedField = Delay.class.getDeclaredField("delayed");
			delayedField.setAccessible(true);
			Class<?> dateClass = Class.forName("ch.njol.skript.util.Date");
			getTimeStampMethod = dateClass.getMethod("getTimestamp");
		} catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<T> getColorClass() {
		return (Class<T>) SkriptColor.class;
	}

	@Override
	public SkriptColor colorFromName(String name) {
		return SkriptColor.fromName(name);
	}

	@Override
	public long getTime(Date date) {
		try {
			return (long) getTimeStampMethod.invoke(date);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return 0L;
		}
	}

	@Override
	public String getColorName(Object color) {
		return ((SkriptColor) color).getName();
	}

	@Override
	public ConvertedExpression getConvertedExpr(Expression expr, Class superType, Object converter) {
		try {
			Constructor<ConvertedExpression> conConvertedExpression = ConvertedExpression.class.getConstructor(Expression.class, superType.getClass(), converterInfoClass);
			Constructor<?> converterInfoConstructor = converterInfoClass.getConstructor(Object.class, superType.getClass(), converterClass, Integer.class);

			return conConvertedExpression.newInstance(expr, superType, converterInfoConstructor.newInstance(Object.class, superType, converter, 1));
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
				 IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<?> getConverterClass() {
		return converterClass;
	}

	@Override
	public <R> R convert(Object object, Class<? extends R>[] to) {
		try {
			return (R) convertMethod.invoke(null, object, to);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public <R> R convert(Object object, Class<? extends R> to) {
		try {
			return (R) convertMethod2.invoke(null, object, to);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public List<SecLoop> currentLoops() {
		return ParserInstance.get().getCurrentSections(SecLoop.class);
	}
	
	@Override
	public Kleenean hasDelayBefore() {
		return ParserInstance.get().getHasDelayBefore();
	}

	@Override
	public boolean isCurrentEvent(Class<? extends Event> event) {
		return ParserInstance.get().isCurrentEvent(event);
	}

	@Override
	public SkriptSecLoop getLoop(int i, String input) {
		int j = 1;
		SecLoop loop = null;
		for (SecLoop l : currentLoops()) {
			if (l.getLoopedExpression() instanceof ExprYaml) {
				if (j < i) {
					j++;
					continue;
				}
				if (loop != null) {
					return null;
				}
				loop = l;
				if (j == i)
					break;
			}
		}

		return new SkriptSecLoop(loop);
	}

	@Override
	public void addDelayedEvent(Event event) {
		try {
			Set<Event> delayed = (Set<Event>) delayedField.get(null);
			delayed.add(event);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		}

	}
}
