package dev.walu.metin.model;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public record Reward(Material item, int amount, String command, double chance) {

    public boolean isCommand() { return command != null; }

    public boolean roll() {
        return ThreadLocalRandom.current().nextDouble() * 100.0 < chance;
    }

    public static List<Reward> parseList(List<Map<?, ?>> raw, Logger log, String context) {
        List<Reward> out = new ArrayList<>();
        if (raw == null) return out;
        for (Map<?, ?> map : raw) {
            double chance = Math.max(0.0, Math.min(100.0, num(map.get("chance"), 100.0)));
            Object command = map.get("command");
            if (command != null) {
                String value = String.valueOf(command).trim();
                if (value.startsWith("/")) value = value.substring(1).trim();
                if (!value.isEmpty()) out.add(new Reward(null, 0, value, chance));
                continue;
            }
            Object item = map.get("item");
            if (item == null) {
                if (log != null) log.warning("[" + context + "] 'item' veya 'command' eksik, atlandı.");
                continue;
            }
            Material material = Material.matchMaterial(String.valueOf(item));
            if (material == null || material.isAir() || !material.isItem()) {
                if (log != null) log.warning("[" + context + "] Geçersiz eşya: " + item);
                continue;
            }
            int amount = (int) Math.max(1, num(map.get("amount"), 1));
            out.add(new Reward(material, amount, null, chance));
        }
        return out;
    }

    public static double num(Object value, double fallback) {
        if (value instanceof Number number) return number.doubleValue();
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }
}
