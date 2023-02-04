package me.sashie.skriptyaml;

import org.bukkit.event.Event;

import ch.njol.skript.effects.Delay;

//Forked to provide backwards compatibility with older skript versions ie. dev25
public abstract class DelayFork extends Delay {

	public static void addDelayedEvent(Event event) {
		delayed.add(event);
	}
}