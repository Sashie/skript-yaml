package me.sashie.skriptyaml.utils.versions;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.skript.ExprYaml;
import me.sashie.skriptyaml.utils.versions.wrapper.SkriptSecLoop;
import org.bukkit.event.Event;

import java.util.List;

public class V2_6 implements SkriptAdapter {

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

	@SuppressWarnings("unchecked")
	@Override
	public ConvertedExpression getConvertedExpr(Expression expr, Class superType, Converter converter) {
		return new ConvertedExpression<>(expr, superType,
				new ConverterInfo<>(Object.class, superType, converter, 1));
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
}
