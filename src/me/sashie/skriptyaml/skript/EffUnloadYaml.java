package me.sashie.skriptyaml.skript;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

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
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("Unload YAML")
@Description("Unloads one or more YAML files or directories full of YAML files from memory.")
@Examples({
	"unload yaml \"config\"",
	" ",
	"unload yaml directory \"plugins\\skript-yaml\""
})
@Since("1.0.0")
public class EffUnloadYaml extends Effect {

	static {
		Skript.registerEffect(EffUnloadYaml.class, "unload [y[a]ml] [(1¦director(y|ies))] %strings%");
	}

	private Expression<String> file;
	private int mark;

	@Override
	protected void execute(@Nullable Event event) {
		String server = null;
		if (mark == 1)
			 server = new File("").getAbsoluteFile().getAbsolutePath() + File.separator;
		for (String name : this.file.getAll(event)) {
			if (mark == 1) {
				for (Iterator<Entry<String, YAMLProcessor>> it = SkriptYaml.YAML_STORE.entrySet().iterator(); it.hasNext();) {
					String path = it.next().getValue().getParentPath();
					if (path.equals(server + StringUtil.removeFirst(server, name))) 
						it.remove();
				}
			} else {
				if (!SkriptYaml.YAML_STORE.containsKey(name)) 
					continue;
				SkriptYaml.YAML_STORE.remove(name);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "unload yaml" + (mark == 1 ? "director(y|ies) " : " ") + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		file = (Expression<String>) exprs[0];
		mark = parser.mark;
		return true;
	}
}
