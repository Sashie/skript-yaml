package me.sashie.skriptyaml.utils.yaml;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable;

import java.util.Base64;

public class SkriptClass {

	private final String type;
	private final String data;

	public SkriptClass(String type, byte[] data) {
		this.type = type;
		this.data = Base64.getEncoder().encodeToString(data);
	}

	public SkriptClass(Object value) {
		SerializedVariable.Value val = Classes.serialize(value);
		this.type = val.type;
		this.data = Base64.getEncoder().encodeToString(val.data);
	}

	public String getType() {
		return this.type;
	}

	public String getData() {
		return this.data;
	}

	public Object deserialize() {
		return SkriptClass.deserialize(this.type, this.data);
	}

	public static Object deserialize(String type, String data) {
		return Classes.deserialize(type, Base64.getDecoder().decode(data));
	}
}
