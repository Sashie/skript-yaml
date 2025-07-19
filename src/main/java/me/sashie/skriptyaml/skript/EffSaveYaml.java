package me.sashie.skriptyaml.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Save YAML")
@Description("Saves the current cached YAML elements to file." +
		"\n\t - Using the `[with an indentation of %-number%]` option allows you to save the file with a different amount of spacing between 1 and 10" +
		"\n\t - Option to remove extra lines between nodes")
@Examples({
		"save yaml \"config\"",
		"save yaml \"config\" with an indentation of 2"
})
@Since("1.0.0")
public class EffSaveYaml extends Effect {

	static {
		Skript.registerEffect(EffSaveYaml.class,
				"save y[a]ml %strings% [with an indentation of %-number%] [(1¦[and] with(out| no) extra lines between nodes)]");
	}

	private Expression<String> file;
	private Expression<Number> yamlIndent;
	private int mark;
	private SkriptNode skriptNode;

	@Override
	protected void execute(@Nullable Event event) {
		for (String name : this.file.getAll(event)) {
			YAMLProcessor yaml = SkriptYaml.YAML_STORE.get(name);
			if (yaml == null)
				continue;
			if (yamlIndent != null)
				yaml.setIndent(this.yamlIndent.getSingle(event).intValue());
			try {
				yaml.save(this.mark == 1 ? false : true);
			} catch (NullPointerException ex) {
				SkriptYaml.warn("The yaml '" + name + "' hasnt been populated yet " + skriptNode.toString());
				ex.printStackTrace();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "save yaml " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		this.file = (Expression<String>) exprs[0];
		this.yamlIndent = (Expression<Number>) exprs[1];
		this.mark = parse.mark;
		this.skriptNode = new SkriptNode(SkriptLogger.getNode());
		return true;
	}
}