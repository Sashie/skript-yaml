package me.sashie.skriptyaml.utils.yaml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.util.Vector;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public class SkriptYamlConstructor extends SafeConstructor {

	public SkriptYamlConstructor() {
		this.yamlConstructors.put(new Tag("!skriptclass"), new ConstructSkriptClass());

		this.yamlConstructors.put(new Tag("!vector"), new ConstructVector());
		this.yamlConstructors.put(new Tag("!location"), new ConstructLocation());

		this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
	}

	private class ConstructCustomObject extends ConstructYamlMap {
		@Override
		public Object construct(Node node) {
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
		public void construct2ndStep(Node node, Object object) {
			throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
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

			return new SkriptClass(type, data).deserialize();
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

			return new Location(Bukkit.getServer().getWorld(w), x, y, z, (float) yaw.doubleValue(), (float) pitch.doubleValue());
		}
	}
}
