package me.sashie.skriptyaml.skript;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.StringUtil;
import me.sashie.skriptyaml.utils.yaml.YAMLProcessor;

@Name("Return All YAML loaded in memory")
@Description("Returns a list of all \"cached\" yaml file IDs.")
@Examples({
		"set {_list::*} to the currently loaded yaml files",
		"broadcast \"%{_list::*}%\"",
		" ",
		"loop the loaded yaml",
		"\tbroadcast loop-value",
		" ",
		"loop the loaded yaml from directory \"plugins\\skript-yaml\"",
		"\tbroadcast loop-value",
		" ",
		"loop the loaded yaml directories",
		"\tbroadcast loop-value"
})
@Since("1.0.0")
public class ExprAllLoadedYaml extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAllLoadedYaml.class, String.class, ExpressionType.SIMPLE,
				"[(the|all [(of the|the)])] [currently] loaded y[a]ml [files] [from (director(y|ies) %-strings%|all directories)]",
				"[(the|all [(of the|the)])] [currently] loaded y[a]ml directories");
	}

	private Expression<String> directory;
	private int matchedPattern;

	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parse) {
		if (matchedPattern == 0)
			directory = (Expression<String>) exprs[0];
		this.matchedPattern = matchedPattern;
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (matchedPattern == 0)
			return "(the|all [of the]) [currently] loaded y[a]ml [files]" + (directory != null ? " from directory " + directory.toString(event, debug) : "");
		else
			return "(the|all [of the]) [currently] loaded y[a]ml directories";
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		if (matchedPattern == 0) {
			if (directory == null) {
				if (SkriptYaml.YAML_STORE.isEmpty())
					return null;
				return SkriptYaml.YAML_STORE.keySet().toArray(new String[SkriptYaml.YAML_STORE.keySet().size()]);
			} else {
				return getYamlFromDirectories(directory.getAll(event));
			}
		} else {
			return getAllDirectories();
		}
	}

	private String[] getYamlFromDirectories(String... directories) {
		List<String> yamlNames = new ArrayList<String>();
		String server = new File("").getAbsoluteFile().getAbsolutePath() + File.separator;
		List<String> filter = new ArrayList<String>();

		for (String d : directories) {
			if (d.startsWith(server))
				filter.add(StringUtil.stripLastSeparator(StringUtil.checkSeparator(d)));
			else
				filter.add(server + StringUtil.stripLastSeparator(StringUtil.checkSeparator(d)));
		}
		for (Iterator<Entry<String, YAMLProcessor>> it = SkriptYaml.YAML_STORE.entrySet().iterator(); it.hasNext();) {
			Entry<String, YAMLProcessor> entry = it.next();
			if (filter.contains(entry.getValue().getParentPath())) {
				String id = entry.getKey();
				if (!yamlNames.contains(id))
					yamlNames.add(id);
			}
		}
		if (yamlNames.isEmpty())
			return null;
		else
			return yamlNames.toArray(new String[yamlNames.size()]);
	}

	private String[] getAllDirectories() {
		List<String> yamlDirectories = new ArrayList<String>();
		for (Iterator<Entry<String, YAMLProcessor>> it = SkriptYaml.YAML_STORE.entrySet().iterator(); it.hasNext();) {
			String path = it.next().getValue().getParentPath();
			if (!yamlDirectories.contains(path))
				yamlDirectories.add(path);
		}
		return yamlDirectories.toArray(new String[yamlDirectories.size()]);
	}
	
	@Override
	public void change(Event event, Object[] delta, Changer.ChangeMode mode) {

	}

	@Override
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		return null;
	}
}
