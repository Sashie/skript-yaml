package me.sashie.skriptyaml.utils.versions;

import ch.njol.skript.effects.Delay;
import org.bukkit.event.Event;

import java.lang.reflect.Field;
import java.util.Set;

public class V2_7 extends V2_6 {

	private Field delayedField;

	public V2_7() {
		try {
			delayedField = Delay.class.getDeclaredField("DELAYED");
			delayedField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
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
}
