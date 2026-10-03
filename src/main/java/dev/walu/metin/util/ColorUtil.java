package dev.walu.metin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX = Pattern.compile("&?#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    public static String color(String text) {
        if (text == null) return "";
        Matcher m = HEX.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            StringBuilder r = new StringBuilder("§x");
            for (char c : m.group(1).toCharArray()) {
                r.append('§').append(c);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(r.toString()));
        }
        m.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static Component component(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(color(text));
    }
}
