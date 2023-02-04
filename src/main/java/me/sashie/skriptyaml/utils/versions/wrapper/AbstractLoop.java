package me.sashie.skriptyaml.utils.versions.wrapper;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;

public abstract class AbstractLoop {

	protected Object loop;

	public AbstractLoop(Object loop) {
		this.loop = loop;
	}

	public abstract Class<?> getLoopClass();
	public abstract Object getObject();
	public abstract Object getCurrent(Event event);
	public abstract Expression<?> getLoopedExpression();

}