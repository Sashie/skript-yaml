package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Delete YAML")
@Description("Deletes a YAML file and removes it from memory.")
@Examples({
		"delete yaml \"config\""
})
@Since("1.1.5")
public class EffDeleteYaml extends Effect {

	static {
		Skript.registerEffect(EffDeleteYaml.class, "delete [y[a]ml] %strings%");	//TODO add option to keep loaded in memory
	}

	private Expression<String> file;

	@Override
	protected void execute(@Nullable Event event) {
		for (String name : this.file.getAll(event)) {
			if (!SkriptYaml.YAML_STORE.containsKey(name))
				continue;
			SkriptYaml.YAML_STORE.get(name).getFile().delete();
			SkriptYaml.YAML_STORE.remove(name);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "delete yaml " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		file = (Expression<String>) exprs[0];
		return true;
	}
}
