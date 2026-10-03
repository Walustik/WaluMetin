package dev.walu.metin.listener;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.gui.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class MenuListener implements Listener {

    private final WaluMetin plugin;

    public MenuListener(WaluMetin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);

        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (holder.type() == MenuHolder.Type.STONE || holder.type() == MenuHolder.Type.STONE_EFFECTS) {
            plugin.gui().click(player, holder, event.getRawSlot());
        } else {
            plugin.adminGui().click(player, holder, event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }
}
