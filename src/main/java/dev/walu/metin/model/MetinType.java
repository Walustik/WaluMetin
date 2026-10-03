package dev.walu.metin.model;

import dev.walu.metin.util.ForbiddenBlocks;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*
import java.util.logging.Logger;

public record MetinType(
        String id,
        String name,
        Material material,
        int hp,
        int duration,
        boolean enabled,
        boolean broadcast,
        List<String> breakMessages,
        List<String> respawnMessages,
        List<Reward> hitRewards,
        List<Reward> breakRewards,
        boolean mobSpawnsEnabled,
        List<MobSpawnRule> mobRules,
        List<Reward> mobDrops,
        boolean keepVanillaDrops,
        EnumSet<EffectType> effects
) {

    public boolean isEntity() {
        return material == Material.END_CRYSTAL;
    }

    public static MetinType load(String id, ConfigurationSection section, Logger log) {
        Material material = Material.matchMaterial(section.getString("Type", ""));
        if (material == null || !ForbiddenBlocks.isAllowed(material)) {
            log.warning("[" + id + "] Geçersiz veya yasaklı blok tipi: " + section.getString("Type"));
            return null;
        }

        List<MobSpawnRule> rules = new ArrayList<>();
        ConfigurationSection spawns = section.getConfigurationSection("MobSpawns");
        if (spawns != null) {
            for (String key : spawns.getKeys(false)) {
                int percent;
                try {
                    percent = Integer.parseInt(key.replace("%", "").trim());
                } catch (NumberFormatException ignored) {
                    log.warning("[" + id + "] MobSpawns anahtarı sayı olmalı: " + key);
                    continue;
                }
                if (spawns.isConfigurationSection(key)) {
                    parseRule(percent, spawns.getConfigurationSection(key).getValues(false), rules, log, id);
                } else {
                    for (Map<?, ?> map : spawns.getMapList(key)) parseRule(percent, map, rules, log, id);
                }
            }
        }

        EnumSet<EffectType> effects = EnumSet.noneOf(EffectType.class);
        if (section.getBoolean("Effects.Knockback", false)) effects.add(EffectType.KNOCKBACK);
        if (section.getBoolean("Effects.Fire", false)) effects.add(EffectType.FIRE);
        if (section.getBoolean("Effects.Fatigue", false)) effects.add(EffectType.FATIGUE);

        return new MetinType(
                id,
                section.getString("Name", id),
                material,
                Math.max(1, section.getInt("HP", 100)),
                Math.max(1, section.getInt("Duration", 30)),
                section.getBoolean("Enabled", true),
                section.getBoolean("Broadcast", true),
                lines(section, "BroadcastMessages.Break"),
                lines(section, "BroadcastMessages.Respawn"),
                Reward.parseList(section.getMapList("HitRewards"), log, id + ".HitRewards"),
                Reward.parseList(section.getMapList("BreakRewards"), log, id + ".BreakRewards"),
                section.getBoolean("MobSpawnsEnabled", false),
                rules,
                Reward.parseList(section.getMapList("MobDrops"), log, id + ".MobDrops"),
                section.getBoolean("MobKeepVanillaDrops", false),
                effects
        );
    }

    private static void parseRule(int percent, Map<?, ?> map, List<MobSpawnRule> out, Logger log, String id) {
        Object typeObj = map.get("type");
        if (typeObj == null) {
            log.warning("[" + id + "] MobSpawns." + percent + " içinde 'type' yok.");
            return;
        }
        EntityType type;
        try {
            type = EntityType.valueOf(String.valueOf(typeObj).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warning("[" + id + "] Geçersiz mob türü: " + typeObj);
            return;
        }
        Class<?> entityClass = type.getEntityClass();
        if (type == EntityType.PLAYER || entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass)) {
            log.warning("[" + id + "] Bu varlık mob olarak doğurulamaz: " + type);
            return;
        }
        int amount = Math.max(1, (int) Reward.num(map.get("amount"), 1));
        out.add(new MobSpawnRule(Math.max(1, Math.min(99, percent)), type, amount));
    }

    private static List<String> lines(ConfigurationSection section, String path) {
        Object value = section.get(path);
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) out.add(String.valueOf(item));
            return out;
        }
        if (value instanceof String str && !str.isBlank()) return List.of(str);
        return List.of();
    }
}
