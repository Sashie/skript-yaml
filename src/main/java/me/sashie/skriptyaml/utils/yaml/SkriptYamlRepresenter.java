package me.sashie.skriptyaml.utils.yaml;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.*;
import ch.njol.skript.util.slot.Slot;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.api.RepresentedClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.BaseRepresenter;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SkriptYamlRepresenter extends Representer {

	private static Method representMappingMethod;
	private static Method representScalarMethod;

	static {		
		if (SkriptYaml.getInstance().getServerVersion() <= 12) {
			try {
				Class<?> baseRepresenterClass = BaseRepresenter.class;//Class.forName("org.yaml.snakeyaml.representer.BaseRepresenter");
				//representMapping(Tag tag, Map<?, ?> mapping, Boolean flowStyle)
				representMappingMethod = baseRepresenterClass.getDeclaredMethod("representMapping", Tag.class, Map.class, Boolean.class);
				representMappingMethod.setAccessible(true);
				representScalarMethod = baseRepresenterClass.getDeclaredMethod("representScalar", Tag.class, String.class, Character.class);
				representScalarMethod.setAccessible(true);
			} catch (SecurityException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	private static List<String> representedClasses = new ArrayList<>();

	public SkriptYamlRepresenter() {
		super(new DumperOptions());
		this.nullRepresenter = new Represent() {
			@Override
			public Node representData(Object o) {
				return representScalar(Tag.NULL, "");
			}
		};

		this.representers.put(String.class, new RepresentString());

		this.representers.put(SkriptClass.class, new RepresentSkriptClass());
		this.representers.put(ItemType.class, new RepresentSkriptItemType());
		this.representers.put(Slot.class, new RepresentSkriptSlot());
		this.representers.put(Date.class, new RepresentSkriptDate());
		this.representers.put(Time.class, new RepresentSkriptTime());
		this.representers.put(Timespan.class, new RepresentSkriptTimespan());
		this.representers.put(SkriptYaml.getInstance().getSkriptAdapter().getColorClass(), new RepresentSkriptColor());
		this.representers.put(WeatherType.class, new RepresentSkriptWeather());

		this.representers.put(Vector.class, new RepresentVector());
		this.representers.put(Location.class, new RepresentLocation());
		this.representers.put(BlockData.class, new RepresentBlockData());

		this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
		this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());

		for (Class<?> c : representers.keySet()) {
			if (c != null) {
				String name = c.getSimpleName();
				if (!representedClasses.contains(name))
					representedClasses.add(name);
			}
		}
	}

	public void register(String tag, Class<?> c, RepresentedClass<?> rc) {
		rc.tag = tag;
		this.representers.put(c, rc);
		String name = c.getSimpleName();
		if (!representedClasses.contains(name))
			representedClasses.add(name);
	}

	public static boolean contains(Object object) {
		if (object == null)
			return false;
		return representedClasses.contains(object.getClass().getSimpleName());
	}

	public boolean contains(Class<?> c) {
		return representedClasses.contains(c.getSimpleName());
	}

	//** bypassing snakeyamls RepresentString class **/
	private class RepresentString implements Represent {
		public Node representData(Object data) {
			return representScalar(data);
		}
	}

	private Node representScalar(Object data) {
		if (data instanceof String && data.toString().contains("&")) {	//fixing a bug with color codes not working sometimes
			if (SkriptYaml.getInstance().getServerVersion() >= 13) {
				return representScalar(Tag.STR, data.toString(), DumperOptions.ScalarStyle.DOUBLE_QUOTED);
			} else {
				Node node = null;
				try {
					node = (Node) representScalarMethod.invoke(this, Tag.STR, data.toString(), '"');
				} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
				return node;
			}
		} else {
			return representScalar(Tag.STR, data.toString());
		}
	}

	private class RepresentConfigurationSection extends RepresentMap {
		@NotNull
		@Override
		public Node representData(@NotNull Object data) {
			return super.representData(((ConfigurationSection) data).getValues(false));
		}
	}

	private class RepresentConfigurationSerializable extends RepresentMap {
		@NotNull
		@Override
		public Node representData(@NotNull Object data) {
			return representConfigurationSerializable(data);
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

	private class RepresentVector implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Double> out = new LinkedHashMap<String, Double>();
			Vector vec = (Vector) data;
			out.put("x", vec.getX());
			out.put("y", vec.getY());
			out.put("z", vec.getZ());
			return representMapping(new Tag("!vector"), out);
		}
	}

	private class RepresentLocation implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			Location loc = (Location) data;
			World world = loc.getWorld();
			if (world == null) return null;

			out.put("world", world.getName());
			out.put("x", loc.getX());
			out.put("y", loc.getY());
			out.put("z", loc.getZ());
			out.put("yaw", (double) loc.getYaw());
			out.put("pitch", (double) loc.getPitch());
			return representMapping(new Tag("!location"), out);
		}
	}

	private class RepresentBlockData implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			String blockData = ((BlockData) data).getAsString();

			out.put("blockData", blockData);
			return representMapping(new Tag("!blockdata"), out);
		}
	}

	private class RepresentSkriptClass extends RepresentMap {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			SkriptClass skriptClass = (SkriptClass) data;
			out.put("type", skriptClass.getType());
			out.put("data", skriptClass.getData());
			return representMapping(new Tag("!skriptclass"), out);
		}
	}

	/*
	 * To make things backwards compatible and prevent NoSuchMethod exceptions.
	 * (spigot updated snakeyaml in 1.13.2)
	 */
	@SuppressWarnings({ "unchecked" })
	public <T> T representMapping(Tag tag, Map<?, ?> mapping) {
		if (SkriptYaml.getInstance().getServerVersion() >= 13) {
			return (T) representMapping(tag, mapping, FlowStyle.BLOCK);
		} else {
			T node = null;
			try {
				node = (T) representMappingMethod.invoke(this, tag, mapping, null);
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return (T) node;
		}
	}
	
	private class RepresentSkriptItemType extends RepresentMap {
		@Override
		public Node representData(Object data) {
			ItemStack item = null;
			return representConfigurationSerializable(((ItemType) data).addTo(item));
		}
	}

	private class RepresentSkriptSlot extends RepresentMap {
		@Override
		public Node representData(Object data) {
			return representConfigurationSerializable(((Slot) data).getItem());
		}
	}

	/* TODO eventually add support for different slot types
	private class RepresentInventorySlot extends RepresentMap {
		@Override
		public Node representData(Object data) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
			InventorySlot slot = (InventorySlot) data;
			out.put("index", slot.getIndex());
			out.put("item", slot.getItem());
			return representMapping(new Tag("!skriptclass"), out, null);
		}
	}
	*/

	private class RepresentSkriptDate implements Represent {
		@Override
		public Node representData(Object data) {
			Calendar calendar = Calendar.getInstance(getTimeZone() == null ? TimeZone.getTimeZone("UTC") : timeZone);
			calendar.setTime(new java.util.Date(SkriptYaml.getInstance().getSkriptAdapter().getTime((Date) data)));

			int years = calendar.get(Calendar.YEAR);
			int months = calendar.get(Calendar.MONTH) + 1; // 0..12
			int days = calendar.get(Calendar.DAY_OF_MONTH); // 1..31
			int hour24 = calendar.get(Calendar.HOUR_OF_DAY); // 0..24
			int minutes = calendar.get(Calendar.MINUTE); // 0..59
			int seconds = calendar.get(Calendar.SECOND); // 0..59
			int millis = calendar.get(Calendar.MILLISECOND);
			StringBuilder buffer = new StringBuilder(String.valueOf(years));
			while (buffer.length() < 4) {
				// ancient years
				buffer.insert(0, "0");
			}
			buffer.append("-");
			if (months < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(months));
			buffer.append("-");
			if (days < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(days));
			buffer.append("T");
			if (hour24 < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(hour24));
			buffer.append(":");
			if (minutes < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(minutes));
			buffer.append(":");
			if (seconds < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(seconds));
			if (millis > 0) {
				if (millis < 10) {
					buffer.append(".00");
				} else if (millis < 100) {
					buffer.append(".0");
				} else {
					buffer.append(".");
				}
				buffer.append(String.valueOf(millis));
			}

			// Get the offset from GMT taking DST into account
			int gmtOffset = calendar.getTimeZone().getOffset(calendar.get(Calendar.ERA), calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
					calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.MILLISECOND));
			if (gmtOffset == 0) {
				buffer.append('Z');
			} else {
				if (gmtOffset < 0) {
					buffer.append('-');
					gmtOffset *= -1;
				} else {
					buffer.append('+');
				}
				int minutesOffset = gmtOffset / (60 * 1000);
				int hoursOffset = minutesOffset / 60;
				int partOfHour = minutesOffset % 60;

				if (hoursOffset < 10) {
					buffer.append('0');
				}
				buffer.append(hoursOffset);
				buffer.append(':');
				if (partOfHour < 10) {
					buffer.append('0');
				}
				buffer.append(partOfHour);
			}

			return representScalar(new Tag("!skriptdate"), buffer.toString());
		}
	}

	private class RepresentSkriptTime implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skripttime"), ((Time) data).toString());
		}
	}

	private class RepresentSkriptTimespan implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skripttimespan"), ((Timespan) data).toString());
		}
	}

	private class RepresentSkriptColor implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skriptcolor"), SkriptYaml.getInstance().getSkriptAdapter().getColorName(data));
		}
	}

	private class RepresentSkriptWeather implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skriptweather"), ((WeatherType) data).toString().toLowerCase());
		}
	}
}
