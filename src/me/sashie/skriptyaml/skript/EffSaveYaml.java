package me.sashie.skriptyaml.skript;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("Save YAML")
@Description("Saves the current cached YAML elements to file." +
		"\n\t - Option to remove extra lines between nodes")
@Examples({
		"save yaml \"config\""
})
@Since("1.0.0")
public class EffSaveYaml extends Effect {

	static {
		Skript.registerEffect(EffSaveYaml.class, "save [y[a]ml] %string% [(1¦without extra lines between nodes)]");
	}

	private Expression<String> file;
	private int mark;

	@Override
	protected void execute(@Nullable Event event) {
		final String name = this.file.getSingle(event);
		
		if (!SkriptYaml.YAML_STORE.containsKey(name)) {
			//SkriptYaml.warn("No yaml by the name '" + name + "' has been loaded");
			return;
		}
		
		YAMLProcessor yaml = SkriptYaml.YAML_STORE.get(name);
		yaml.save(this.mark == 1 ? false : true);
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "save yaml " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		file = (Expression<String>) exprs[0];
		this.mark = parse.mark;
		return true;
	}
}
