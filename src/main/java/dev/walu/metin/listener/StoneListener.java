package dev.walu.metin.listener;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.manager.StoneManager;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class StoneListener implements Listener {

    private final WaluMetin plugin;
    private final StoneManager stones;

    public StoneListener(WaluMetin plugin) {
        this.plugin = plugin;
        this.stones = plugin.stones();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        if (action == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().hasItemMeta()) {
            String typeId = event.getItem().getItemMeta().getPersistentDataContainer().get(plugin.adminGui().placeKey(), PersistentDataType.STRING);
            if (typeId != null) {
                MetinType type = stones.getType(typeId);
                if (type != null && type.isEntity()) {
                    event.setCancelled(true);
                    placeEntityStone(player, block.getRelative(event.getBlockFace()), type);
                    return;
                }
            }
        }

        MetinStone stone = stones.getAt(block);
        if (stone == null) return;

        if (plugin.adminGui().isDeleteMode(player) && player.hasPermission("walumetin.admin")) {
            event.setCancelled(true);
            deleteStone(player, stone);
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);
        stones.hit(player, stone);
    }

    private void placeEntityStone(Player player, Block spot, MetinType type) {
        if (!player.hasPermission("walumetin.admin")) return;
        if (!spot.isPassable()) {
            plugin.language().send(player, "invalid-position");
            return;
        }
        if (stones.getAt(spot) != null) {
            plugin.language().send(player, "already-stone");
            return;
        }
        MetinStone stone = stones.create(spot, type);
        plugin.language().send(player, "stone-created", "%stone%", type.name(), "%id%", String.valueOf(stone.id()));
    }

    private void deleteStone(Player player, MetinStone stone) {
        int id = stone.id();
        stones.remove(stone);
        plugin.language().send(player, "stone-removed", "%id%", String.valueOf(id));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (stones.getAt(event.getBlock()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(BlockDamageEvent event) {
        if (stones.getAt(event.getBlock()) != null) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        String typeId = item.getItemMeta().getPersistentDataContainer().get(plugin.adminGui().placeKey(), PersistentDataType.STRING);
        if (typeId == null) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("walumetin.admin")) {
            event.setCancelled(true);
            return;
        }
        MetinType type = stones.getType(typeId);
        if (type == null) {
            event.setCancelled(true);
            plugin.language().send(player, "type-not-found", "%type%", typeId);
            return;
        }
        MetinStone stone = stones.create(event.getBlockPlaced(), type);
        plugin.language().send(player, "stone-created", "%stone%", type.name(), "%id%", String.valueOf(stone.id()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        MetinStone stone = stones.byEntity(event.getEntity());
        if (stone == null) return;
        event.setCancelled(true);
        if (event instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof Player player) {
            if (plugin.adminGui().isDeleteMode(player) && player.hasPermission("walumetin.admin")) {
                deleteStone(player, stone);
                return;
            }
            stones.hit(player, stone);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        MetinStone stone = stones.byEntity(event.getRightClicked());
        if (stone == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (plugin.adminGui().isDeleteMode(player) && player.hasPermission("walumetin.admin")) deleteStone(player, stone);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> stones.getAt(block) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> stones.getAt(block) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (stones.getAt(block) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (stones.getAt(block) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent event) {
        if (stones.getAt(event.getBlock()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFade(BlockFadeEvent event) {
        if (stones.getAt(event.getBlock()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (stones.getAt(event.getBlock()) != null) event.setCancelled(true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!stones.hasStones()) return;
        Bukkit.getScheduler().runTask(plugin, () -> stones.refreshChunk(event.getWorld().getName(), event.getChunk().getX(), event.getChunk().getZ()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.adminGui().clear(event.getPlayer());
    }
}
