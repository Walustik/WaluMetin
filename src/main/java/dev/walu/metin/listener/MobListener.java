package dev.walu.metin.listener;

import dev.walu.metin.WaluMetin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public final class MobListener implements Listener {

    private final WaluMetin plugin;

    public MobListener(WaluMetin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        plugin.mobs().handleDeath(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTarget(EntityTargetEvent event) {
        plugin.mobs().handleTarget(event);
    }
}
