package me.sashie.skriptyaml.skript;

import java.io.File;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.utils.StringUtil;

@Name("Does YAML Exist")
@Description("Checks if a YAML file exists at a file location."
		+ "\nYou shouldn't have to use this condition since the load yaml effect will create the file if it doesn't exist")
@Examples({
		"yaml file \"plugins/skript-yaml/test.yml\" exists:",
		" ",
		"yaml file \"plugins/skript-yaml/test.yml\" doesn't exist:",
})
@Since("1.3")
public class CondYamlFileExists extends Condition {

	static {
		Skript.registerCondition(CondYamlFileExists.class,
				"[(1¦non[(-| )]relative)] y[a]ml file %string% exists", 
				"[(1¦non[(-| )]relative)] y[a]ml file %string% does(n't| not) exist");
	}

	private Expression<String> file;
	private int mark;

	@Override
	public boolean check(final Event event) {
		return file.check(event, new Checker<String>() {
			@Override
			public boolean check(final String s) {
				final String f = StringUtil.checkSeparator(file.getSingle(event));
				File yamlFile = null;
				if (mark == 1) {
					yamlFile = new File(StringUtil.checkRoot(f));
				} else {
					String server = new File("").getAbsoluteFile().getAbsolutePath();
					yamlFile = new File(server + File.separator + f);
				}
				if (yamlFile.exists())
					return true;
				return false;
			}
		}, isNegated());
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "yaml file " + file.toString(e, debug) + (isNegated() ? " does not exist" : " exists");
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parse) {
		file = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		this.mark = parse.mark;
		return true;
	}
}
