/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package me.sashie.skriptyaml.skript;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SkriptYaml;

public class CondYamlIsLoaded extends Condition {
	//y[a]ml[s] %strings% (is|are) loaded
	//y[a]ml[s] %strings% ((are|is) not|(is|are)n[']t) loaded
	static {
		Skript.registerCondition(CondYamlIsLoaded.class,
				"y[a]ml[s] %strings% (is|are) loaded", 
				"y[a]ml[s] %strings% ((are|is) not|(is|are)n[']t) loaded");
	}
	
	private Expression<String> name;
	
	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		name = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return name.check(e, new Checker<String>() {
			@Override
			public boolean check(final String s) {
				if (null == SkriptYaml.YAML_STORE.get(s))
					return false;
				return true;
			}
		}, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "yaml " + name.toString(e, debug) + (name.isSingle() ? " is " : " are ") + (isNegated() ? "not loaded" : "loaded");
	}
	
}
