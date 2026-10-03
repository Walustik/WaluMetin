package com.walustik.walumetin.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renk kodlarını dönüştüren utility sınıfı
 * Hem klasik (&a, &6, &l) hem de Hex (#FF5555) renk kodlarını destekler.
 */
public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {
        // Utility sınıfı - örneğin oluşturulamaz
    }

    /**
     * Metin içindeki renk kodlarını dönüştürür
     *
     * @param input Dönüştürülecek metin
     * @return Renk kodları uygulanmış metin
     */
    public static String colorize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        Matcher matcher = HEX_PATTERN.matcher(input);

        // Hex renk kodlarını dönüştür (#RRGGBB -> §x§R§R§G§G§B§B)
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = toHexLegacy(hex);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);

        // Klasik renk kodlarını dönüştür (&a -> §a)
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    /**
     * Hex renk kodunu Minecraft format'ına dönüştürür
     *
     * @param hex Hex renk (örn: FF5555)
     * @return Minecraft hex format (§x§F§F§5§5§5§5)
     */
    private static String toHexLegacy(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            builder.append('§').append(c);
        }
        return builder.toString();
    }
}
