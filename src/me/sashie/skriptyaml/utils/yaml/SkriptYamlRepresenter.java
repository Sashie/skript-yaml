package me.sashie.skriptyaml.utils.yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Slot;

public class SkriptYamlRepresenter extends Representer {

	private static List<String> representedClasses = new ArrayList<>();

	public SkriptYamlRepresenter() {
		this.nullRepresenter = new Represent() {
			@Override
			public Node representData(Object o) {
				return representScalar(Tag.NULL, "");
			}
		};

		this.representers.put(SkriptClass.class, new RepresentSkriptClass());
		this.representers.put(Vector.class, new RepresentVector());
		this.representers.put(Location.class, new RepresentLocation());
		this.representers.put(ItemType.class, new RepresentItemType());
		this.representers.put(Slot.class, new RepresentSlot());

		this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());

		for (Class<?> c : representers.keySet()) {
			if (c != null)
				representedClasses.add(c.getSimpleName());
		}
	}

	public static boolean contains(Object object) {
		return representedClasses.contains(object.getClass().getSimpleName());
	}

	private class RepresentConfigurationSerializable extends RepresentMap {
		@Override
		public Node representData(Object data) {
			return representConfigurationSerializable(data);
		}
	}

	private class RepresentSkriptClass extends RepresentMap {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			SkriptClass skriptClass = (SkriptClass) data;
			out.put("type", skriptClass.getType());
			out.put("data", skriptClass.getData());
			return representMapping(new Tag("!skriptclass"), out, null);
		}
	}

	private class RepresentVector implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Double> out = new LinkedHashMap<String, Double>();
			Vector vec = (Vector) data;
			out.put("x", vec.getX());
			out.put("y", vec.getY());
			out.put("z", vec.getZ());
			return representMapping(new Tag("!vector"), out, null);
		}
	}

	private class RepresentLocation implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			Location loc = (Location) data;
			out.put("world", loc.getWorld().getName());
			out.put("x", loc.getX());
			out.put("y", loc.getY());
			out.put("z", loc.getZ());
			out.put("yaw", (double) loc.getYaw());
			out.put("pitch", (double) loc.getPitch());
			return representMapping(new Tag("!location"), out, null);
		}
	}

	private class RepresentItemType extends RepresentMap {
		@Override
		public Node representData(Object data) {
			ItemStack item = null;
			return representConfigurationSerializable(((ItemType) data).addTo(item));
		}
	}

	private class RepresentSlot extends RepresentMap {
		@Override
		public Node representData(Object data) {
			return representConfigurationSerializable(((Slot) data).getItem());
		}
	}

	private Node representConfigurationSerializable(Object data) {
		ConfigurationSerializable serializable = (ConfigurationSerializable) data;
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
				ConfigurationSerialization.getAlias(serializable.getClass()));
		values.putAll(serializable.serialize());

		return super.representData(values);
	}
}
