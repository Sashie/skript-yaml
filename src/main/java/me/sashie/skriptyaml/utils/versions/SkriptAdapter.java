package me.sashie.skriptyaml.utils.versions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.utils.versions.wrapper.AbstractLoop;
import org.bukkit.event.Event;

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

	long getTime(Date date);

	ConvertedExpression getConvertedExpr(Expression expr, Class superType, Object converter);

	Class<?> getConverterClass();

	<R> R convert(Object object, Class<? extends R>[] to);

	<R> R convert(Object object, Class<? extends R> to);

	Object currentLoops();

	Kleenean hasDelayBefore();

	boolean isCurrentEvent(Class<? extends Event> event);

	AbstractLoop getLoop(int i, String input);

	void addDelayedEvent(Event event);

}
