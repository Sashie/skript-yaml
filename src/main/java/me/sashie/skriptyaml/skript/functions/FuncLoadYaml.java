package me.sashie.skriptyaml.skript.functions;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.registrations.Classes;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLFormat;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class FuncLoadYaml extends JavaFunction<Boolean> {

	static {
		Functions.registerFunction(new FuncLoadYaml())
			.description("Loads a yaml file.")
			.examples("loadYaml(\"someId\", \"plugins/someThing/someYamlFile.yml\", false)")
			.since("1.3.3");
	}
 
	final static ClassInfo<String> stringClass = Classes.getExactClassInfo(String.class);
	final static ClassInfo<Boolean> booleanClass = Classes.getExactClassInfo(Boolean.class);

	public FuncLoadYaml() {
		super("loadYaml",
				new Parameter[] { 
						new Parameter<>("name", stringClass, true, null),
						new Parameter<>("file", stringClass, true, null),
						new Parameter<>("isRelative", booleanClass, true, null)
					},
				booleanClass, true);
	}

	@Override
	@Nullable
	public Boolean[] execute(FunctionEvent e, Object[][] params) {
		String name = (String) params[0][0];
		String file = (String) params[1][0];
		Boolean isRelative = (Boolean) params[2][0];

		File yamlFile = null;
		String server = new File("").getAbsoluteFile().getAbsolutePath() + File.separator;
		if (isRelative) {
			yamlFile = new File(StringUtil.checkRoot(StringUtil.checkSeparator(file)));
		} else {
			yamlFile = new File(server + StringUtil.checkSeparator(file));
		}

		try {
			if (!yamlFile.exists()) {
				File folder;
				String filePath = yamlFile.getPath();
				int index = filePath.lastIndexOf(File.separator);
				folder = new File(filePath.substring(0, index));
				if (index >= 0 && !folder.exists()) {
					folder.mkdirs();
				}
				yamlFile.createNewFile();
			}
		} catch (IOException error) {
			SkriptYaml.error("[Load Yaml] " + error.getMessage() + " (" + file + ")");
			return new Boolean[] { false };
		}

		YAMLProcessor yaml = new YAMLProcessor(yamlFile, false, YAMLFormat.EXTENDED);

		try {
			yaml.load();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			SkriptYaml.YAML_STORE.put(name, yaml);
		}

		return new Boolean[] { true };
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
