package me.sashie.skriptyaml.utils.versions;

import java.util.List;

import org.bukkit.event.Event;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.util.Kleenean;

/**
 * Certain classes, field names or, method signatures have changed between Skript v2.3 and v2.4 and v2.6
 * <p>
 * This interface handles the different methods required by skript-yaml or any addon that chooses to fork this(please give credit)
 * 
 * @version 1.3.3
 */
public interface SkriptAdapter {

	<T> Class<T> getColorClass();

	Object colorFromName(String name);

	String getColorName(Object color);

	ConvertedExpression getConvertedExpr(Expression expr, Class superType, Converter converter);
	
	List<Loop> currentLoops();
	
	Kleenean hasDelayBefore();
	
	boolean isCurrentEvent(Class<? extends Event> event);
}
