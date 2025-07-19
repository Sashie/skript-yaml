package me.sashie.skriptyaml.skript.functions;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import me.sashie.skriptyaml.utils.yaml.YAMLAPI;

import javax.annotation.Nullable;

public class FuncLoadYaml extends JavaFunction<Boolean> {

	static {
		Functions.registerFunction(new FuncLoadYaml())
			.description("Loads a yaml file. Returns true is the load was successful.",
					" - 1st parameter: (string) The id you would like to use",
					" - 2nd parameter: (string) The file path",
					" - 3rd parameter: (boolean) Whether you want to use a non relative path ie. c:/somefolder rather than relative to the server ")
			.examples("loadYaml(\"someId\", \"plugins/someThing/someYamlFile.yml\")",
					"loadYaml(\"someId\", \"plugins/someThing/someYamlFile.yml\", true)")
			.since("1.3.3");
	}

	public FuncLoadYaml() {
		super("loadYaml",
				new Parameter[] { 
						new Parameter<>("name", Classes.getExactClassInfo(String.class), true, null),
						new Parameter<>("file", Classes.getExactClassInfo(String.class), true, null),
						new Parameter<>("isNonRelative", Classes.getExactClassInfo(Boolean.class), true, new SimpleLiteral<Boolean>(false, true))
					},
				Classes.getExactClassInfo(Boolean.class), true);
	}

	@Override
	@Nullable
	public Boolean[] execute(FunctionEvent<?> e, Object[][] params) {
		String name = (String) params[0][0];
		String file = (String) params[1][0];
		Boolean isNonRelative = (Boolean) params[2][0];

		boolean loaded = YAMLAPI.load(name, file, isNonRelative);
		return new Boolean[] { loaded };
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
