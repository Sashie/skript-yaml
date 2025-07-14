package me.sashie.skriptyaml.utils.versions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.skript.ExprYaml;
import me.sashie.skriptyaml.utils.versions.wrapper.AbstractLoop;
import me.sashie.skriptyaml.utils.versions.wrapper.SkriptLoop;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class V2_3 implements SkriptAdapter {

	private Field byNameField, hasDelayBeforeField, currentLoopsField, delayedField;
	private Class<?> converterClass;
	private Method convertMethod, convertMethod2, isCurrentEventMethod, getTimeStampMethod;

	public V2_3() {
		try {
			if (Skript.fieldExists(getColorClass(), "BY_NAME")) 
				byNameField = getColorClass().getDeclaredField("BY_NAME");
			else 
				byNameField = getColorClass().getDeclaredField("byName");
			hasDelayBeforeField = ScriptLoader.class.getDeclaredField("hasDelayBefore");
			currentLoopsField = ScriptLoader.class.getDeclaredField("currentLoops");
			converterClass = Class.forName("ch.njol.skript.classes.Converter");
			Class<?> convertersClass = Class.forName("ch.njol.skript.registrations.Converters");
			convertMethod = convertersClass.getMethod("convert", Object.class, Class[].class);
			convertMethod2 = convertersClass.getMethod("convert", Object.class, Class.class);
			delayedField = Delay.class.getDeclaredField("delayed");
			Class<?> skriptLoaderClass = Class.forName("ch.njol.skript.ScriptLoader");
			isCurrentEventMethod = skriptLoaderClass.getMethod("isCurrentEvent", Event.class);
			Class<?> dateClass = Class.forName("ch.njol.skript.util.Date");
			getTimeStampMethod = dateClass.getMethod("getTimestamp");
		} catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		byNameField.setAccessible(true);
		hasDelayBeforeField.setAccessible(true);
		currentLoopsField.setAccessible(true);
		delayedField.setAccessible(true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<T> getColorClass() {
		return (Class<T>) Color.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Color colorFromName(String name) {
		try {
			return (Color) ((HashMap<String, Object>) byNameField.get(null)).get(name);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getColorName(Object color) {
		return ((Color) color).toString();
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
	public ConvertedExpression getConvertedExpr(Expression expr, Class superType, Object converter) {
		try {
			Constructor<ConvertedExpression> conExCon = ConvertedExpression.class.getConstructor(Expression.class, superType.getClass(), converterClass);
			return conExCon.newInstance(expr, superType, converter);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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
	public List<?> currentLoops() {
		try {
			return (List<?>) currentLoopsField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Kleenean hasDelayBefore() {
		try {
			return (Kleenean) hasDelayBeforeField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isCurrentEvent(Class<? extends Event> event) {
		try {
			return (boolean) isCurrentEventMethod.invoke(null, event);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return false;
		}
	}
	
	@Override
	public SkriptLoop getLoop(int i, String input) {
		return getLoop(i, input, currentLoops(), ExprYaml.class);
	}

	@Override
	public AbstractLoop getLoop(int i, String input, Class<? extends Expression<?>> loopedExpression) {
		return getLoop(i, input, currentLoops(), loopedExpression);
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

	public static SkriptLoop getLoop(int i, String input, List<?> currentLoops, Class<?> cls ) {
		int j = 1;
		Object loop = null;
		for (final Object l : currentLoops) {
			if (SkriptLoop.getLoopedExpression(l).getClass().isAssignableFrom(cls)) {
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

		return new SkriptLoop(loop);
	}
}
