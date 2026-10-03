package dev.walu.metin.manager;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.model.EffectType;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import dev.walu.metin.util.ColorUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class EffectManager {

    private final WaluMetin plugin;

    public EffectManager(WaluMetin plugin) {
        this.plugin = plugin;
    }

    public List<Integer> percents() {
        List<Integer> list = plugin.getConfig().getIntegerList("Effects.Percents");
        return list.isEmpty() ? List.of(75, 50, 25) : list;
    }

    public String percentsText() {
        return percents().stream().map(value -> "%" + value).collect(Collectors.joining(", "));
    }

    public void onDamage(MetinStone stone, MetinType type, Player attacker) {
        double percent = stone.hp * 100.0 / type.hp();
        List<Integer> crossed = new ArrayList<>();
        for (int point : percents()) {
            if (!stone.effectTriggered.contains(point) && percent <= point) crossed.add(point);
        }
        if (crossed.isEmpty()) return;
        stone.effectTriggered.addAll(crossed);

        World world = plugin.stones().world(stone);
        if (world == null) return;
        Location center = new Location(world, stone.x() + 0.5, stone.y() + 0.5, stone.z() + 0.5);
        FileConfiguration config = plugin.getConfig();

        if (plugin.stones().isEffect(stone, EffectType.KNOCKBACK)) {
            double radius = config.getDouble("Effects.Knockback.Radius", 8.0);
            double power = config.getDouble("Effects.Knockback.Power", 1.6);
            double lift = config.getDouble("Effects.Knockback.Lift", 0.9);
            for (Player target : targets(world, center, radius)) {
                Vector direction = target.getLocation().toVector().subtract(center.toVector());
                direction.setY(0);
                if (direction.lengthSquared() < 0.01) {
                    direction = new Vector(ThreadLocalRandom.current().nextDouble(-1.0, 1.0), 0, ThreadLocalRandom.current().nextDouble(-1.0, 1.0));
                    if (direction.lengthSquared() < 0.01) direction = new Vector(1, 0, 0);
                }
                direction.normalize().multiply(power).setY(lift);
                target.setVelocity(direction);
            }
            world.spawnParticle(Particle.CLOUD, center, 40, 0.6, 0.4, 0.6, 0.1);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
        }

        if (plugin.stones().isEffect(stone, EffectType.FIRE)) {
            double radius = config.getDouble("Effects.Fire.Radius", 5.0);
            int ticks = Math.max(1, config.getInt("Effects.Fire.Seconds", 4)) * 20;
            for (Player target : targets(world, center, radius)) {
                target.setFireTicks(Math.max(target.getFireTicks(), ticks));
            }
        }

        if (plugin.stones().isEffect(stone, EffectType.FATIGUE)) {
            double radius = config.getDouble("Effects.Fatigue.Radius", 8.0);
            int ticks = Math.max(1, config.getInt("Effects.Fatigue.Seconds", 8)) * 20;
            int level = Math.max(0, config.getInt("Effects.Fatigue.Level", 1) - 1);
            boolean slow = config.getBoolean("Effects.Fatigue.Slowness", true);
            PotionEffectType fatigue = PotionEffectType.getByKey(NamespacedKey.minecraft("mining_fatigue"));
            PotionEffectType slowness = PotionEffectType.getByKey(NamespacedKey.minecraft("slowness"));
            for (Player target : targets(world, center, radius)) {
                if (fatigue != null) target.addPotionEffect(new PotionEffect(fatigue, ticks, level, false, true, true));
                if (slow && slowness != null) target.addPotionEffect(new PotionEffect(slowness, ticks, 0, false, true, true));
            }
        }
    }

    private List<Player> targets(World world, Location center, double radius) {
        List<Player> out = new ArrayList<>();
        for (Player player : world.getNearbyPlayers(center, radius)) {
            if (player.isDead()) continue;
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) out.add(player);
        }
        return out;
    }
}
