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
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Delete YAML")
@Description("Deletes a YAML file and optionally removes it from memory.\n" +
		"  - If you want to keep the file loaded in memory, use the 'and keep loaded in memory' option")
@Examples({
		"delete yaml \"config\"",
		"delete yaml \"config\" and keep loaded in memory"
})
@Since("1.1.5")
public class EffDeleteYaml extends Effect {

	static {
		Skript.registerEffect(EffDeleteYaml.class, "delete y[a]ml %strings% [and keep loaded in memory]");
	}

	private Expression<Object> file;
	private boolean keepLoaded;

	@Override
	protected void execute(@Nullable Event event) {
		for (Object name : this.file.getAll(event)) {
			YAMLProcessor yaml = SkriptYaml.YAML_STORE.get(name);
			if (yaml == null)
				continue;
			yaml.getFile().delete();
			if (!keepLoaded)
				SkriptYaml.YAML_STORE.remove(name);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "delete yaml " + this.file.toString(event, b) + (keepLoaded ? " and keep loaded in memory" : "");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		file = (Expression<Object>) exprs[0];
		keepLoaded = parser.expr.toLowerCase().endsWith("keep loaded in memory");
		return true;
	}
}
