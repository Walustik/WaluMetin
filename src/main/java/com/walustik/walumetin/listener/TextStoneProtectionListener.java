package com.walustik.walumetin.listener;

import com.walustik.walumetin.service.TextStoneService;
import com.walustik.walumetin.util.ColorUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Metin taşlarını korur ve hasar olaylarını yönetir
 */
public class TextStoneProtectionListener implements Listener {

    private final TextStoneService service;

    public TextStoneProtectionListener(TextStoneService service) {
        this.service = service;
    }

    /**
     * Metin taşının kırılmasını engeller (klasik kırma)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (service.isTextStone(event.getBlock())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(
                    ColorUtil.colorize("&cBu metin taşı kırılmaz.")
            );
        }
    }

    /**
     * Metin taşına vurulduğunda tetiklenir (hasar)
     * BlockDamageEvent: Oyuncu bloğu vurduğunda tetiklenir
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent event) {
        service.damageBlock(event.getBlock(), event.getPlayer());
        event.setCancelled(true);
    }

    /**
     * Metin taşı konumuna blok yerleştirilmesini engeller
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (service.isTextStone(event.getBlockPlaced())) {
            event.setCancelled(true);
        }
    }

    /**
     * Patlama olayında metin taşlarını korur
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(service::isTextStone);
    }
}
