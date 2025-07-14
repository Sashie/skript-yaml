package me.sashie.skriptyaml.skript.loops;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import me.sashie.skriptyaml.SimpleExpressionFork;
import me.sashie.skriptyaml.SkriptYaml;
import me.sashie.skriptyaml.utils.versions.wrapper.AbstractExpressionInitializer;
import me.sashie.skriptyaml.utils.versions.wrapper.AbstractLoop;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public abstract class AbstractLoopExpression<T> extends SimpleExpressionFork<T> {
    boolean itsIntendedLoop = false;
    protected AbstractLoop loop;
    protected String name, s;
    protected Class<? extends SimpleExpression<?>> clsTo;
    protected Expression<Integer> number;

    @SuppressWarnings("unchecked")
    protected static <T> T[] callGetMethod(Expression<T> expression, Event event) {
        try {
            Method getMethod = expression.getClass().getSuperclass().getDeclaredMethod("get", Event.class);
            getMethod.setAccessible(true);
            return (T[]) getMethod.invoke(expression, event);
        } catch (Exception ex) {
            throw Skript.exception(ex, "Failed to get value from expression %s", expression.toString(event, false));
        }
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        if (e == null)
            return name;
        if (itsIntendedLoop) {
            final Object current = loop.getCurrent(e);
            Object[] objects = callGetMethod(loop.getLoopedExpression(), e);

            if (current == null || objects == null)
                return Classes.getDebugMessage(null);
            return Classes.getDebugMessage(current);
        }
        return Classes.getDebugMessage(loop.getCurrent(e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
        name = parser.expr;
        number = (Expression<Integer>) vars[0];
        s = name.split("-")[1];

        int i = -1;
        if (number != null) {
            i = ((Literal<Integer>) number).getSingle();
        }

        if (! Expression.class.isAssignableFrom(this.getExpressionToLoop()) || ! SimpleExpressionFork.class.isAssignableFrom(this.getExpressionToLoop())) {
            return false;
        }

        AbstractLoop loop = SkriptYaml.getInstance().getSkriptAdapter().getLoop(i, s, (Class<? extends SimpleExpressionFork<?>>) this.getExpressionToLoop());

        if (loop == null) {
            SkriptYaml.error("There are multiple loops that match loop-" + s + ". Use loop-" + s + "-1/2/3/etc. to specify which loop's value you want. " + getNodeMsg());
            return false;
        }

        if (loop.getObject() == null) {
            SkriptYaml.error("There's no loop that matches 'loop-" + s + "' " + getNodeMsg());
            return false;
        }

        var initializer = new AbstractExpressionInitializer(vars, matchedPattern, isDelayed, parser);
        if (! this.isIntendedLoop(loop, initializer)) {
            return false;
        };

        this.loop = loop;
        return true;
    }

    public abstract boolean isIntendedLoop(AbstractLoop loop, final AbstractExpressionInitializer initializer);

    @SuppressWarnings("rawtypes")
    public abstract Class<? extends Expression> getExpressionToLoop();

    protected String getNodeMsg() {
        ch.njol.skript.config.Node n = SkriptLogger.getNode();
        if (n == null) {
            return "";
        }
        return "[script: " + n.getConfig().getFileName() + ", line: " + n.getLine() + " : '" + n.save().trim() + "']";
    }
}
