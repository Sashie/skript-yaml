package me.sashie.skriptyaml.utils.versions.wrapper;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SkriptLoop extends AbstractLoop {

	static Class loopClass;
	static Method getCurrent, getLoopedExpression;


	static {
		try {
			loopClass = Class.forName("ch.njol.skript.lang.Loop");
			getCurrent = loopClass.getDeclaredMethod("getCurrent", Event.class);
			getLoopedExpression = loopClass.getDeclaredMethod("getLoopedExpression");
			//getDeclaredField("hasDelayBefore");
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public SkriptLoop(Object loop) {
		super(loop);
	}

	@Override
	public Class<?> getLoopClass() {
		Object o = getObject();
		if (o != null) {
			return o.getClass();
		}
		return null;
	}

	@Override
	public Object getObject() {
		return loop;
	}

	@Override
	public Object getCurrent(Event event) {
		Object o = getObject();
		if (o != null) {
			try {
				return getCurrent.invoke(o, event);//(Loop) o).getCurrent(event);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public Expression<?> getLoopedExpression() {
		Object o = getObject();
		return getLoopedExpression(o);
	}

	static public Expression<?> getLoopedExpression(Object o) {
		if (o != null) {
			try {
				return (Expression<?>) getLoopedExpression.invoke(o);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
