package me.sashie.skriptyaml.utils.versions.wrapper;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.sections.SecLoop;
import org.bukkit.event.Event;

public class SkriptSecLoop extends AbstractLoop {

	public SkriptSecLoop(Object loop) {
		super(loop);
	}

	@Override
	public Class<?> getLoopClass() {
		Object o = getObject();
		if (o != null) {
			return ((SecLoop) o).getClass();
		}
		return null;
	}

	@Override
	public SecLoop getObject() {
		return (SecLoop) loop;
	}

	@Override
	public Object getCurrent(Event event) {
		Object o = getObject();
		if (o != null) {
			return ((SecLoop) o).getCurrent(event);
		}
		return null;
	}

	@Override
	public Expression<?> getLoopedExpression() {
		Object o = getObject();
		if (o != null) {
			return ((SecLoop) o).getLoopedExpression();
		}
		return null;
	}
}