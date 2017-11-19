package me.sashie.skriptyaml.skript;

import java.io.IOException;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.AsyncEffect;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.YamlFile;

@Name("Save YAML")
@Description("Saves the current cached YAML elements to file.")
@Examples({
		"save yaml \"config\""
})
@Since("1.0.0")
public class EffSaveYaml extends AsyncEffect {

	static {
		Skript.registerEffect(EffSaveYaml.class, "save [y[a]ml] %string%");
		//Skript.registerEffect(EffSaveYaml.class, "save y[a]ml [file] %string% [to path %-string%]"); This is debatable
	}

	private Expression<String> file;

	@Override
	protected void execute(@Nullable Event event) {
		final String name = this.file.getSingle(event);
		
		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			//SkriptYaml.warn("No yaml by the name '" + name + "' has been loaded");
			return;
		}
		
		YamlFile yaml = SkriptYaml.YAML_STORE.get(name);
		try {
			yaml.file.save(yaml.path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "save yaml file " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		file = (Expression<String>) exprs[0];
		return true;
	}
}
