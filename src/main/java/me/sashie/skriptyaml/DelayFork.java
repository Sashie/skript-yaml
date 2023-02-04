package me.sashie.skriptyaml;

import ch.njol.skript.effects.Delay;
import org.bukkit.event.Event;

//Forked to provide backwards compatibility with older skript versions ie. dev25
public abstract class DelayFork extends Delay {

	public static void addDelayedEvent(Event event) {
		delayed.add(event);
	}
}