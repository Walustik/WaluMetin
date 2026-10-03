package dev.walu.metin.manager;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.model.EffectType;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import dev.walu.metin.util.ColorUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class StoneManager {

    private final WaluMetin plugin;
    private final Map<String, MetinType> types = new LinkedHashMap<>();
    private final Map<Integer, MetinStone> stones = new LinkedHashMap<>();
    private final Map<String, MetinStone> byLocation = new HashMap<>();
    private final File dataFile;
    private final NamespacedKey entityKey;
    private int nextId = 1;

    public StoneManager(WaluMetin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "stones.yml");
        this.entityKey = new NamespacedKey(plugin, "stone_entity");
    }

    public void loadTypes() {
        types.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("MetinTaslari");
        if (root == null) {
            plugin.getLogger().warning("config.yml içinde 'MetinTaslari' bölümü yok!");
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) continue;
            MetinType type = MetinType.load(key, section, plugin.getLogger());
            if (type != null) types.put(key.toLowerCase(Locale.ROOT), type);
        }
    }

    public MetinType getType(String key) {
        return key == null ? null : types.get(key.toLowerCase(Locale.ROOT));
    }

    public MetinType type(MetinStone stone) {
        return stone == null ? null : types.get(stone.typeId().toLowerCase(Locale.ROOT));
    }

    public Collection<MetinType> types() { return Collections.unmodifiableCollection(types.values()); }

    public Collection<MetinStone> all() { return Collections.unmodifiableCollection(stones.values()); }

    public boolean hasStones() { return !stones.isEmpty(); }

    public void loadData() {
        stones.clear();
        byLocation.clear();
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        nextId = config.getInt("next-id", 1);
        ConfigurationSection section = config.getConfigurationSection("stones");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection stoneSection = section.getConfigurationSection(key);
            if (stoneSection == null) continue;
            int id;
            try {
                id = Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                continue;
            }
            MetinStone stone = new MetinStone(id, stoneSection.getString("type", "stone"), stoneSection.getString("world", "world"), stoneSection.getInt("x"), stoneSection.getInt("y"), stoneSection.getInt("z"));
            if (stoneSection.contains("enabled")) stone.enabledOverride = stoneSection.getBoolean("enabled");
            if (stoneSection.contains("mobs")) stone.mobsOverride = stoneSection.getBoolean("mobs");
            if (stoneSection.contains("broadcast")) stone.broadcastOverride = stoneSection.getBoolean("broadcast");
            if (stoneSection.contains("hologram")) stone.hologramOverride = stoneSection.getBoolean("hologram");
            if (stoneSection.contains("leaderboard")) stone.leaderboardOverride = stoneSection.getBoolean("leaderboard");
            ConfigurationSection effects = stoneSection.getConfigurationSection("effects");
            if (effects != null) {
                for (EffectType effectType : EffectType.values()) {
                    if (effects.contains(effectType.name())) stone.effectOverrides.put(effectType, effects.getBoolean(effectType.name()));
                }
            }
            register(stone);
            nextId = Math.max(nextId, stone.id() + 1);
        }
    }

    public void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("next-id", nextId);
        for (MetinStone stone : stones.values()) {
            String base = "stones." + stone.id() + ".";
            config.set(base + "type", stone.typeId());
            config.set(base + "world", stone.world());
            config.set(base + "x", stone.x());
            config.set(base + "y", stone.y());
            config.set(base + "z", stone.z());
            if (stone.enabledOverride != null) config.set(base + "enabled", stone.enabledOverride);
            if (stone.mobsOverride != null) config.set(base + "mobs", stone.mobsOverride);
            if (stone.broadcastOverride != null) config.set(base + "broadcast", stone.broadcastOverride);
            if (stone.hologramOverride != null) config.set(base + "hologram", stone.hologramOverride);
            if (stone.leaderboardOverride != null) config.set(base + "leaderboard", stone.leaderboardOverride);
            for (Map.Entry<EffectType, Boolean> entry : stone.effectOverrides.entrySet()) {
                config.set(base + "effects." + entry.getKey().name(), entry.getValue());
            }
        }
        try {
            config.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("stones.yml save failed: " + exception.getMessage());
        }
    }

    private void register(MetinStone stone) {
        stones.put(stone.id(), stone);
        byLocation.put(key(stone.world(), stone.x(), stone.y(), stone.z()), stone);
    }

    private static String key(String world, int x, int y, int z) {
        return world + ":" + x + "," + y + "," + z;
    }

    public MetinStone getAt(Block block) {
        if (block == null) return null;
        return byLocation.get(key(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    public MetinStone byEntity(Entity entity) {
        if (entity == null) return null;
        Integer id = entity.getPersistentDataContainer().get(entityKey, PersistentDataType.INTEGER);
        return id == null ? null : stones.get(id);
    }

    public MetinStone get(int id) { return stones.get(id); }

    public Collection<MetinStone> all() { return Collections.unmodifiableCollection(stones.values()); }

    public World world(MetinStone stone) { return Bukkit.getWorld(stone.world()); }

    public Block block(MetinStone stone) {
        World world = world(stone);
        if (world == null) return null;
        return world.getBlockAt(stone.x(), stone.y(), stone.z());
    }

    public boolean isEnabled(MetinStone stone) {
        if (stone.enabledOverride != null) return stone.enabledOverride;
        MetinType type = type(stone);
        return type != null && type.enabled();
    }

    public boolean isMobs(MetinStone stone) {
        if (stone.mobsOverride != null) return stone.mobsOverride;
        MetinType type = type(stone);
        return type != null && type.mobSpawnsEnabled();
    }

    public boolean isBroadcast(MetinStone stone) {
        if (stone.broadcastOverride != null) return stone.broadcastOverride;
        MetinType type = type(stone);
        return type != null && type.broadcast();
    }

    public boolean isHologram(MetinStone stone) {
        if (stone.hologramOverride != null) return stone.hologramOverride;
        return plugin.getConfig().getBoolean("Hologram.Enabled", true);
    }

    public boolean isLeaderboard(MetinStone stone) {
        if (stone.leaderboardOverride != null) return stone.leaderboardOverride;
        return plugin.getConfig().getBoolean("Leaderboard.Hologram", true);
    }

    public boolean isEffect(MetinStone stone, EffectType effectType) {
        Boolean override = stone.effectOverrides.get(effectType);
        if (override != null) return override;
        MetinType type = type(stone);
        return type != null && type.effects().contains(effectType);
    }

    public void setEnabled(MetinStone stone, boolean enabled) {
        stone.enabledOverride = enabled;
        saveData();
    }

    public void toggleMobs(MetinStone stone) {
        stone.mobsOverride = !isMobs(stone);
        saveData();
    }

    public void toggleBroadcast(MetinStone stone) {
        stone.broadcastOverride = !isBroadcast(stone);
        saveData();
    }

    public void toggleHologram(MetinStone stone) {
        stone.hologramOverride = !isHologram(stone);
        saveData();
        updateHologram(stone);
    }

    public void toggleLeaderboard(MetinStone stone) {
        stone.leaderboardOverride = !isLeaderboard(stone);
        saveData();
        updateHologram(stone);
    }

    public void toggleEffect(MetinStone stone, EffectType effectType) {
        stone.effectOverrides.put(effectType, !isEffect(stone, effectType));
        saveData();
    }

    public void instantRespawn(MetinStone stone) {
        MetinType type = type(stone);
        if (type == null) return;
        stone.state = MetinStone.State.ACTIVE;
        stone.hp = type.hp();
        stone.remaining = 0;
        showSolid(stone, type);
        updateHologram(stone);
    }

    public MetinStone create(Block block, MetinType type) {
        if (block == null || type == null) return null;
        MetinStone stone = new MetinStone(nextId++, type.id(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        register(stone);
        stone.hp = type.hp();
        stone.state = MetinStone.State.ACTIVE;
        showSolid(stone, type);
        updateHologram(stone);
        saveData();
        return stone;
    }

    public void remove(MetinStone stone) {
        if (stone == null) return;
        cancelTask(stone);
        plugin.holograms().remove(stone);
        removeEntity(stone);
        Block block = block(stone);
        if (block != null) block.setType(Material.AIR);
        stones.remove(stone.id());
        byLocation.remove(key(stone.world(), stone.x(), stone.y(), stone.z()));
        plugin.leaderboard().removeStone(stone.id());
        saveData();
    }

    private void cancelTask(MetinStone stone) {
        if (stone.task != null) {
            stone.task.cancel();
            stone.task = null;
        }
    }

    public void activateAll() {
        for (MetinStone stone : stones.values()) {
            MetinType type = type(stone);
            if (type == null) continue;
            stone.state = MetinStone.State.ACTIVE;
            stone.hp = type.hp();
            stone.triggered.clear();
            stone.effectTriggered.clear();
            showSolid(stone, type);
            updateHologram(stone);
        }
    }

    public void refreshAll() {
        for (MetinStone stone : stones.values()) {
            MetinType type = type(stone);
            if (type == null) continue;
            if (stone.state == MetinStone.State.ACTIVE) {
                stone.hp = Math.min(stone.hp, type.hp());
                showSolid(stone, type);
            }
            updateHologram(stone);
        }
    }

    public void shutdown() {
        for (MetinStone stone : stones.values()) {
            cancelTask(stone);
            plugin.holograms().remove(stone);
            removeEntity(stone);
            Block block = block(stone);
            if (block != null) block.setType(Material.AIR);
        }
    }

    public void hit(Player player, MetinStone stone) {
        MetinType type = type(stone);
        if (type == null) return;
        if (stone.state != MetinStone.State.ACTIVE) {
            player.sendActionBar(ColorUtil.component(plugin.language().msg("inactive")));
            return;
        }

        stone.hp--;
        plugin.rewards().give(type.hitRewards(), player, null);

        if (stone.hp <= 0) {
            breakStone(player, stone, type);
            return;
        }

        player.sendActionBar(ColorUtil.component(plugin.language().msg("hit",
                "%stone%", type.name(), "%hp%", String.valueOf(stone.hp), "%max%", String.valueOf(type.hp()))));
        updateHologram(stone);
        plugin.effects().onDamage(stone, type, player);
        plugin.mobs().onDamage(stone, type, player, isMobs(stone));
    }

    private void breakStone(Player player, MetinStone stone, MetinType type) {
        plugin.rewards().give(type.breakRewards(), player, null);
        plugin.leaderboard().recordBreak(stone.id(), player);
        announce(stone, type, true, player.getName());
        startRespawn(stone, type);
    }

    private void startRespawn(MetinStone stone, MetinType type) {
        cancelTask(stone);
        stone.state = MetinStone.State.RESPAWNING;
        stone.remaining = type.duration();
        Block block = block(stone);
        if (block != null) block.setType(Material.BEDROCK);
        updateHologram(stone);

        stone.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (--stone.remaining <= 0) {
                    cancel();
                    stone.task = null;
                    stone.state = MetinStone.State.ACTIVE;
                    stone.hp = type.hp();
                    stone.triggered.clear();
                    stone.effectTriggered.clear();
                    showSolid(stone, type);
                    updateHologram(stone);
                    announce(stone, type, false, "");
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void showSolid(MetinStone stone, MetinType type) {
        Block block = block(stone);
        if (block == null) return;
        if (type.isEntity()) {
            if (block.getType() != Material.AIR) block.setType(Material.AIR);
            ensureEntity(stone);
        } else {
            block.setType(type.material());
        }
    }

    private void ensureEntity(MetinStone stone) {
        World world = world(stone);
        if (world == null || !world.isChunkLoaded(stone.x() >> 4, stone.z() >> 4)) return;
        if (stone.entityId != null) {
            Entity entity = Bukkit.getEntity(stone.entityId);
            if (entity != null && entity.isValid()) return;
        }
        Location location = new Location(world, stone.x() + 0.5, stone.y(), stone.z() + 0.5);
        EnderCrystal crystal = world.spawn(location, EnderCrystal.class, entity -> {
            entity.setShowingBottom(false);
            entity.setPersistent(false);
            entity.getPersistentDataContainer().set(entityKey, PersistentDataType.INTEGER, stone.id());
        });
        stone.entityId = crystal.getUniqueId();
    }

    private void removeEntity(MetinStone stone) {
        if (stone.entityId == null) return;
        Entity entity = Bukkit.getEntity(stone.entityId);
        if (entity != null) entity.remove();
        stone.entityId = null;
    }

    private void announce(MetinStone stone, MetinType type, boolean broken, String actor) {
        if (!isBroadcast(stone)) return;
        List<String> messages = broken ? type.breakMessages() : type.respawnMessages();
        if (messages.isEmpty()) messages = List.of(plugin.language().raw("broadcast-break"));
        String prefix = plugin.language().raw("prefix");
        for (String message : messages) {
            String rendered = message.replace("%prefix%", prefix)
                    .replace("%player%", actor)
                    .replace("%stone%", type.name())
                    .replace("%id%", String.valueOf(stone.id()))
                    .replace("%world%", stone.world())
                    .replace("%x%", String.valueOf(stone.x()))
                    .replace("%y%", String.valueOf(stone.y()))
                    .replace("%z%", String.valueOf(stone.z()));
            Bukkit.broadcastMessage(ColorUtil.color(rendered));
        }
    }

    public void updateHologram(MetinStone stone) {
        if (stone == null) return;
        plugin.holograms().update(stone);
    }

    public void refreshChunk(String worldName, int chunkX, int chunkZ) {
        for (MetinStone stone : stones.values()) {
            if (!stone.world().equals(worldName)) continue;
            if ((stone.x() >> 4) != chunkX || (stone.z() >> 4) != chunkZ) continue;
            MetinType type = type(stone);
            if (type != null && type.isEntity() && stone.state == MetinStone.State.ACTIVE) ensureEntity(stone);
            updateHologram(stone);
        }
    }
}
