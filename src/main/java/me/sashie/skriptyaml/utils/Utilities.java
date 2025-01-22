package me.sashie.skriptyaml.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;

public class Utilities {

    public static final String PLUGIN_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Skript" + ChatColor.WHITE + "-" + ChatColor.BLUE + "Yaml" + ChatColor.DARK_GRAY + "]" + ChatColor.RESET;
    private static final boolean SKRIPT_EXISTS = Bukkit.getPluginManager().getPlugin("Skript") != null;
    private static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile("<#([A-Fa-f\\d]){6}>");

    /**
     * Method to log a coloured message to the console.
     * <p>
     * This method is copied from SkBee.
     * <a href="https://github.com/ShaneBeee/SkBee/blob/f6f85e3d9d9da0cd772b58e59fc288f7d1ec21f8/src/main/java/com/shanebeestudios/skbee/api/util/Util.java#L55">log(format, objects)</a>
     * </p>
     *
     * @param format  The format for the log.
     * @param objects The arguments for the log.
     * @author ShaneBee
     */

    public static void log(String format, Object... objects) {
        String log = String.format(format, objects);
        Bukkit.getConsoleSender().sendMessage(getColouredString(PLUGIN_PREFIX + " " + log));
    }

    /**
     * Method to get a coloured string from a string.
     * <p>
     * This method is copied from SkBee.
     * <a href="https://github.com/ShaneBeee/SkBee/blob/f6f85e3d9d9da0cd772b58e59fc288f7d1ec21f8/src/main/java/com/shanebeestudios/skbee/api/util/Util.java#L35">getColString(string)</a>
     * </p>
     *
     * @param string The string to convert to it's coloured version.
     * @return The coloured string.
     * @author ShaneBee
     */

    @SuppressWarnings("deprecation") // Paper deprecation
    public static String getColouredString(String string) {
        Matcher matcher = HEX_PATTERN.matcher(string);
        if (SKRIPT_EXISTS) {
            while (matcher.find()) {
                ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
                String before = string.substring(0, matcher.start());
                String after = string.substring(matcher.end());
                string = before + hexColor + after;
                matcher = HEX_PATTERN.matcher(string);
            }
        } else {
            string = HEX_PATTERN.matcher(string).replaceAll("");
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}