package dev.walu.metin.manager;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import dev.walu.metin.model.MobSpawnRule;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class MobManager {

    private final WaluMetin plugin;
    private final NamespacedKey mobKey;

    public MobManager(WaluMetin plugin) {
        this.plugin = plugin;
        this.mobKey = new NamespacedKey(plugin, "guardian_of");
    }

    public boolean isGuardian(Entity entity) {
        return entity != null && entity.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING);
    }

    public void handleDeath(EntityDeathEvent event) {
        String typeId = event.getEntity().getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
        if (typeId == null) return;
        MetinType type = plugin.stones().getType(typeId);
        if (type == null) return;
        if (!type.keepVanillaDrops()) event.getDrops().clear();
        Player killer = event.getEntity().getKiller();
        plugin.rewards().give(type.mobDrops(), killer, event.getEntity().getLocation());
    }

    public void handleTarget(EntityTargetEvent event) {
        if (!isGuardian(event.getEntity())) return;
        if (!(event.getTarget() instanceof Player target)) return;
        if (validTarget(target)) return;
        Player closest = nearestTarget(event.getEntity().getLocation(), targetRadius());
        if (closest != null) event.setTarget(closest);
        else event.setCancelled(true);
    }

    public void onDamage(MetinStone stone, MetinType type, Player attacker, boolean enabled) {
        if (type.mobRules().isEmpty()) return;
        double percent = stone.hp * 100.0 / type.hp();
        Set<Integer> crossed = new HashSet<>();
        for (MobSpawnRule rule : type.mobRules()) {
            if (!stone.triggered.contains(rule.percent()) && percent <= rule.percent()) crossed.add(rule.percent());
        }
        if (crossed.isEmpty()) return;
        stone.triggered.addAll(crossed);
        if (!enabled) return;

        World world = plugin.stones().world(stone);
        if (world == null) return;
        Location center = new Location(world, stone.x(), stone.y(), stone.z());
        Player target = validTarget(attacker) ? attacker : nearestTarget(center, targetRadius());

        for (MobSpawnRule rule : type.mobRules()) {
            if (!crossed.contains(rule.percent())) continue;
            for (int i = 0; i < rule.amount(); i++) {
                Entity entity = world.spawnEntity(findSpot(center), rule.type());
                if (entity instanceof LivingEntity living) {
                    living.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, type.id());
                    if (living instanceof Mob mob && target != null) mob.setTarget(target);
                }
            }
        }
    }

    private double targetRadius() {
        return plugin.getConfig().getDouble("Mobs.TargetRadius", 24.0);
    }

    public static boolean validTarget(Player player) {
        if (player == null || player.isDead() || !player.isOnline()) return false;
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    public Player nearestTarget(Location location, double radius) {
        World world = location.getWorld();
        if (world == null) return null;
        Player best = null;
        double bestDistance = radius * radius;
        for (Player player : world.getPlayers()) {
            if (!validTarget(player)) continue;
            double distance = player.getLocation().distanceSquared(location);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    private Location findSpot(Location center) {
        World world = center.getWorld();
        if (world == null) return center.clone().add(0.5, 1.0, 0.5);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 12; i++) {
            int x = center.getBlockX() + random.nextInt(-3, 4);
            int z = center.getBlockZ() + random.nextInt(-3, 4);
            for (int y = center.getBlockY() + 2; y >= center.getBlockY() - 2; y--) {
                Block feet = world.getBlockAt(x, y, z);
                if (feet.isPassable() && feet.getRelative(BlockFace.UP).isPassable() && !feet.getRelative(BlockFace.DOWN).isPassable()) {
                    return feet.getLocation().add(0.5, 0, 0.5);
                }
            }
        }
        return center.clone().add(0.5, 1.0, 0.5);
    }
}
