package me.sashie.skriptyaml.utils.versions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.utils.versions.wrapper.SkriptLoop;
import org.bukkit.event.Event;

import java.lang.reflect.Field;
import java.util.List;

public class V2_4 implements SkriptAdapter {

	private Field hasDelayBeforeField, currentLoopsField;

	public V2_4() {
		try {
			hasDelayBeforeField = ScriptLoader.class.getDeclaredField("hasDelayBefore");
			currentLoopsField = ScriptLoader.class.getDeclaredField("currentLoops");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		hasDelayBeforeField.setAccessible(true);
		currentLoopsField.setAccessible(true);
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
	public String getColorName(Object color) {
		return ((SkriptColor) color).getName();
	}

	@Override
	public ConvertedExpression getConvertedExpr(Expression expr, Class superType, Converter converter) {
		return new ConvertedExpression<>(expr, superType,
				new ConverterInfo<>(Object.class, superType, converter, 1));
	}

	@SuppressWarnings("unchecked")
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
		return ScriptLoader.isCurrentEvent(event);
	}

	@Override
	public SkriptLoop getLoop(int i, String input) {
		return V2_3.getLoop(i, input, currentLoops());
	}
}
