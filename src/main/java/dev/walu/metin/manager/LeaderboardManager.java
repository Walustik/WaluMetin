package dev.walu.metin.manager;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LeaderboardManager {

    public static final class Entry {
        private final UUID uuid;
        private String name;
        private int count;

        public Entry(UUID uuid, String name, int count) {
            this.uuid = uuid;
            this.name = name;
            this.count = count;
        }

        public UUID uuid() { return uuid; }
        public String name() { return name; }
        public int count() { return count; }
    }

    private final WaluMetin plugin;
    private final File file;
    private final Map<Integer, Map<UUID, Entry>> perStone = new HashMap<>();
    private final Map<UUID, Entry> totals = new HashMap<>();
    private boolean dirty;

    public LeaderboardManager(WaluMetin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "leaderboard.yml");
    }

    public void load() {
        perStone.clear();
        totals.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) return;

        for (String stoneId : config.getConfigurationSection("stones") == null ? java.util.Collections.emptySet() : config.getConfigurationSection("stones").getKeys(false)) {
            int id = Integer.parseInt(stoneId);
            Map<UUID, Entry> map = new HashMap<>();
            var sec = config.getConfigurationSection("stones." + stoneId);
            if (sec == null) continue;
            for (String uuid : sec.getKeys(false)) {
                try {
                    UUID user = UUID.fromString(uuid);
                    map.put(user, new Entry(user, sec.getString(uuid + ".name", "?"), sec.getInt(uuid + ".count", 0)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            perStone.put(id, map);
        }

        var players = config.getConfigurationSection("players");
        if (players != null) {
            for (String uuid : players.getKeys(false)) {
                try {
                    UUID user = UUID.fromString(uuid);
                    totals.put(user, new Entry(user, players.getString(uuid + ".name", "?"), players.getInt(uuid + ".count", 0)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (var entry : perStone.entrySet()) {
            for (Entry stoneEntry : entry.getValue().values()) {
                String base = "stones." + entry.getKey() + "." + stoneEntry.uuid() + ".";
                config.set(base + "name", stoneEntry.name());
                config.set(base + "count", stoneEntry.count());
            }
        }
        for (Entry e : totals.values()) {
            String base = "players." + e.uuid() + ".";
            config.set(base + "name", e.name());
            config.set(base + "count", e.count());
        }
        try {
            config.save(file);
            dirty = false;
        } catch (IOException e) {
            plugin.getLogger().warning("Leaderboard save failed: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (dirty) save();
    }

    public void recordBreak(int stoneId, Player player) {
        Map<UUID, Entry> map = perStone.computeIfAbsent(stoneId, key -> new HashMap<>());
        Entry entry = map.get(player.getUniqueId());
        if (entry == null) {
            entry = new Entry(player.getUniqueId(), player.getName(), 0);
            map.put(player.getUniqueId(), entry);
        }
        entry.name = player.getName();
        entry.count++;

        Entry total = totals.get(player.getUniqueId());
        if (total == null) {
            total = new Entry(player.getUniqueId(), player.getName(), 0);
            totals.put(player.getUniqueId(), total);
        }
        total.name = player.getName();
        total.count++;
        dirty = true;
    }

    public void removeStone(int stoneId) {
        perStone.remove(stoneId);
        dirty = true;
    }

    public List<Entry> top(int stoneId, int limit) {
        Map<UUID, Entry> map = perStone.get(stoneId);
        if (map == null || map.isEmpty()) return List.of();
        return map.values().stream().sorted((a, b) -> Integer.compare(b.count(), a.count())).limit(limit).toList();
    }

    public List<Entry> topGlobal(int limit) {
        return totals.values().stream().sorted((a, b) -> Integer.compare(b.count(), a.count())).limit(limit).toList();
    }

    public int total(UUID uuid) {
        Entry entry = totals.get(uuid);
        return entry == null ? 0 : entry.count();
    }

    public int breaks(int stoneId, UUID uuid) {
        Map<UUID, Entry> map = perStone.get(stoneId);
        if (map == null) return 0;
        Entry entry = map.get(uuid);
        return entry == null ? 0 : entry.count();
    }
}
