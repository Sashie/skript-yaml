package me.sashie.skriptyaml.skript;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.debug.SkriptNode;
import me.sashie.skriptyaml.utils.SkriptYamlUtils;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("YAML Comments/header")
@Description("Gets, sets, deletes comments or the header of a cached yaml file" +
		"\n  - Headers don't contain '#' so add it yourself if you want it" +
		"\n  - Comments can only be at root level ie. 'root' not 'root.something'" +
		"\n  - Both comment and header expressions can be set to multiple elements" +
		"\n  - This expression does not save to file" +
		"\n  - Option to have an extra line or not depending if you use comment or header" +
		"\n  - Any 'extra lines' are removed when deleting comments/headers")
@Examples({
		"set the comments of yaml node \"test\" from \"config\" to \"First line\" and \"Second line\"",
		"delete the comments of yaml node \"test\" from \"config\"",
		" ",
		"set {_header::*} to \"First line\" and \"Second line\"",
		"set the comments at the top of \"config\" to {_header::*}",
		"delete the comments at the top of \"config\"",
		" ",
		"set the header of \"config\" to {_header::*}"
})
@Since("1.1.0")
public class ExprYamlComments extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprYamlComments.class, Object.class, ExpressionType.SIMPLE,
				"[the] comment[s] (of|from) [y[a]ml] node[s] %strings% (of|in|from) %string% [(1¦with [an] extra line)]",
				"[the] (comment[s] (at|on) [the] top of |header (of|from)) %string% [(1¦with [an] extra line)]");
	}

	private Expression<String> paths, file;
	private int mark;
	private SkriptNode skriptNode;

	private static enum States {
		COMMENT, HEADER
	}

	private States state;

	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return state.toString().toLowerCase() + (state == States.COMMENT ? " for path\node " + this.paths.toString(event, b) : "") + " from yaml " + this.file.toString(event, b);
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		final String name = this.file.getSingle(event);

		if (!SkriptYamlUtils.yamlExists(name, skriptNode))
			return null;

		YAMLProcessor config = SkriptYaml.YAML_STORE.get(name);

		String s = null;
		if (state == States.COMMENT) {
			final String path = this.paths.getSingle(event);
			s = config.getComment(path, skriptNode);
		} else if (state == States.HEADER)
			s = config.getHeader(skriptNode);
		if (s == null)
			return null;
		List<String> list = Arrays.asList(s.split("\\r?\\n"));
		return list.toArray(new String[list.size()]);
	}

	@Override
	public void change(Event event, Object[] delta, Changer.ChangeMode mode) {
		final String name = this.file.getSingle(event);
		String[] paths = null;

		if (!SkriptYamlUtils.yamlExists(name, skriptNode))
			return;

		YAMLProcessor config = SkriptYaml.YAML_STORE.get(name);

		if (state == States.COMMENT) {
			paths = this.paths.getAll(event);
			if (mode == ChangeMode.SET) {
				String[] comments = new String[delta.length];
				for (String p : paths) {
					if (!p.contains(".")) {
						if (config.getMap().containsKey(p))
							config.setComment(p, this.mark == 1 ? true : false, toStringArray(delta, comments));
						else
							SkriptYaml.warn("'" + p + "' is not a valid path in '" + name + "' " + skriptNode.toString());
					} else {
						SkriptYaml.warn("Comments can only be added to root paths not '" + p + "' in '" + name + "' " + skriptNode.toString());
					}
				}
			} else if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
				for (String p : paths) {
					if (config.getMap().containsKey(p)) {
						String n = null;
						config.setComment(p, false, n);
					}
				}
			}
		} else if (state == States.HEADER) {
			if (mode == ChangeMode.SET) {
				config.setHeader(toStringArray(delta, new String[delta.length]));
				config.setExtraHeaderLine(this.mark == 1 ? true : false);
			} else if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
				String n = null;
				config.setHeader(n);
				config.setExtraHeaderLine(false);
			}
		}
	}

	private String[] toStringArray(Object[] input, String[] output) {
		for (int i = 0; i < input.length; i++)
			output[i] = (String) input[i];
		return output;
	}

	@Override
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.SET) {
			return CollectionUtils.array(Object[].class);	
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] e, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		if (matchedPattern == 0) {
			state = States.COMMENT;
			paths = (Expression<String>) e[0];
			file = (Expression<String>) e[1];
		} else {
			state = States.HEADER;
			file = (Expression<String>) e[0];
		}
		this.mark = parse.mark;
		this.skriptNode = new SkriptNode(SkriptLogger.getNode());
		return true;
	}
}
