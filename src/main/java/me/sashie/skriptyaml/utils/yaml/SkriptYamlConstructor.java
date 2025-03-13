package me.sashie.skriptyaml.utils.yaml;

import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.api.ConstructedClass;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkriptYamlConstructor extends SafeConstructor {

	public SkriptYamlConstructor() {
		super(new LoaderOptions());
		this.yamlConstructors.put(new Tag("!skriptclass"), new ConstructSkriptClass());

		this.yamlConstructors.put(new Tag("!vector"), new ConstructVector());
		this.yamlConstructors.put(new Tag("!location"), new ConstructLocation());
		this.yamlConstructors.put(new Tag("!blockdata"), new ConstructBlockData());
		
		this.yamlConstructors.put(new Tag("!skriptdate"), new ConstructSkriptDate());
		//this.yamlConstructors.put(Tag.TIMESTAMP, new ConstructSkriptDate());
		this.yamlConstructors.put(new Tag("!skripttime"), new ConstructSkriptTime());
		this.yamlConstructors.put(new Tag("!skripttimespan"), new ConstructSkriptTimespan());
		this.yamlConstructors.put(new Tag("!skriptcolor"), new ConstructSkriptColor());
		this.yamlConstructors.put(new Tag("!skriptweather"), new ConstructSkriptWeather());

		this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());

	}
	
	public void register(String tag, ConstructedClass<?> cc) {
		this.yamlConstructors.put(new Tag("!" + tag), cc);
	}

	public Map<Object, Object> constructMap(MappingNode node) {
		return constructMapping(node);
	}

	private class ConstructCustomObject extends ConstructYamlMap {

		@Nullable
		@Override
		public Object construct(@NotNull Node node) {
			if (node.isTwoStepsConstruction()) {
				throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
			}

			Map<?, ?> raw = (Map<?, ?>) super.construct(node);

			if (raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
				Map<String, Object> typed = new LinkedHashMap<String, Object>(raw.size());
				for (Map.Entry<?, ?> entry : raw.entrySet()) {
					typed.put(entry.getKey().toString(), entry.getValue());
				}

				try {
					return ConfigurationSerialization.deserializeObject(typed);
				} catch (IllegalArgumentException ex) {
					throw new YAMLException("Could not deserialize object", ex);
				}
			}

			return raw;
		}

		@Override
		public void construct2ndStep(@NotNull Node node, @NotNull Object object) {
			throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
		}
	}

	private class ConstructVector extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			final Map<Object, Object> values = constructMapping((MappingNode) node);

			Double x = (Double) values.get("x");
			Double y = (Double) values.get("y");
			Double z = (Double) values.get("z");

			if (x == null || y == null || z == null)
				return null;

			return new Vector(x, y, z);
		}
	}

	private class ConstructLocation extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			final Map<Object, Object> values = constructMapping((MappingNode) node);

			String w = (String) values.get("world");
			Double x = (Double) values.get("x");
			Double y = (Double) values.get("y");
			Double z = (Double) values.get("z");
			Double yaw = (Double) values.get("yaw");
			Double pitch = (Double) values.get("pitch");

			if (w == null | x == null || y == null || z == null || yaw == null || pitch == null)
				return null;

			World world = Bukkit.getServer().getWorld(w);
			if (world == null) return null;

			return new Location(world, x, y, z, (float) yaw.doubleValue(),
					(float) pitch.doubleValue());
		}
	}

	private class ConstructBlockData extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			final Map<Object, Object> values = constructMapping((MappingNode) node);

			String data = (String) values.get("blockData");

			if (data == null)
				return null;

			return Bukkit.createBlockData(data);
		}
	}

	private class ConstructSkriptClass extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			final Map<Object, Object> values = constructMapping((MappingNode) node);
			String type = (String) values.get("type");
			String data = (String) values.get("data");
			if (type == null || data == null)
				return null;

			return SkriptClass.deserialize(type, data);
		}
	}

	private final static Pattern TIMESTAMP_REGEXP = Pattern.compile(
			"^([0-9][0-9][0-9][0-9])-([0-9][0-9]?)-([0-9][0-9]?)(?:(?:[Tt]|[ \t]+)([0-9][0-9]?):([0-9][0-9]):([0-9][0-9])(?:\\.([0-9]*))?(?:[ \t]*(?:Z|([-+][0-9][0-9]?)(?::([0-9][0-9])?)?))?)?$");
	private final static Pattern YMD_REGEXP = Pattern.compile("^([0-9][0-9][0-9][0-9])-([0-9][0-9]?)-([0-9][0-9]?)$");

	public class ConstructSkriptDate extends AbstractConstruct {
		private Calendar calendar;

		public Calendar getCalendar() {
			return calendar;
		}

		@Override
		public Object construct(Node node) {
			ScalarNode scalar = (ScalarNode) node;
			String nodeValue = scalar.getValue();
			Matcher match = YMD_REGEXP.matcher(nodeValue);
			if (match.matches()) {
				String year_s = match.group(1);
				String month_s = match.group(2);
				String day_s = match.group(3);
				calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.clear();
				calendar.set(Calendar.YEAR, Integer.parseInt(year_s));
				// Java's months are zero-based...
				calendar.set(Calendar.MONTH, Integer.parseInt(month_s) - 1); // x
				calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_s));
				return new Date(calendar.getTime().getTime());
			} else {
				match = TIMESTAMP_REGEXP.matcher(nodeValue);
				if (!match.matches()) {
					throw new YAMLException("Unexpected timestamp: " + nodeValue);
				}
				String year_s = match.group(1);
				String month_s = match.group(2);
				String day_s = match.group(3);
				String hour_s = match.group(4);
				String min_s = match.group(5);
				// seconds and milliseconds
				String seconds = match.group(6);
				String millis = match.group(7);
				if (millis != null) {
					seconds = seconds + "." + millis;
				}
				double fractions = Double.parseDouble(seconds);
				int sec_s = (int) Math.round(Math.floor(fractions));
				int usec = (int) Math.round((fractions - sec_s) * 1000);
				// timezone
				String timezoneh_s = match.group(8);
				String timezonem_s = match.group(9);
				TimeZone timeZone;
				if (timezoneh_s != null) {
					String time = timezonem_s != null ? ":" + timezonem_s : "00";
					timeZone = TimeZone.getTimeZone("GMT" + timezoneh_s + time);
				} else {
					// no time zone provided
					timeZone = TimeZone.getTimeZone("UTC");
				}
				calendar = Calendar.getInstance(timeZone);
				calendar.set(Calendar.YEAR, Integer.parseInt(year_s));
				// Java's months are zero-based...
				calendar.set(Calendar.MONTH, Integer.parseInt(month_s) - 1);
				calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_s));
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour_s));
				calendar.set(Calendar.MINUTE, Integer.parseInt(min_s));
				calendar.set(Calendar.SECOND, sec_s);
				calendar.set(Calendar.MILLISECOND, usec);
				
				return new Date(calendar.getTime().getTime());
			}
		}
	}

	public class ConstructSkriptTime extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			ScalarNode scalar = (ScalarNode) node;
			String nodeValue = scalar.getValue();
			return Time.parse(nodeValue);
		}
	}

	public class ConstructSkriptTimespan extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			ScalarNode scalar = (ScalarNode) node;
			String nodeValue = scalar.getValue();
			return Timespan.parse(nodeValue);
		}
	}

	public class ConstructSkriptColor extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			ScalarNode scalar = (ScalarNode) node;
			String nodeValue = scalar.getValue();
			return SkriptYaml.getInstance().getSkriptAdapter().colorFromName(nodeValue);
		}
	}

	public class ConstructSkriptWeather extends AbstractConstruct {
		@Override
		public Object construct(Node node) {
			ScalarNode scalar = (ScalarNode) node;
			String nodeValue = scalar.getValue();
			return WeatherType.parse(nodeValue);
		}
	}

}
