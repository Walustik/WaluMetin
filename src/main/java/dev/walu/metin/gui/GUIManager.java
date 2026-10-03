package dev.walu.metin.gui;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.manager.LanguageManager;
import dev.walu.metin.manager.StoneManager;
import dev.walu.metin.model.EffectType;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import dev.walu.metin.util.ColorUtil;
import dev.walu.metin.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class GUIManager {

    private static final int SLOT_STONE = 10;
    private static final int SLOT_MOBS = 11;
    private static final int SLOT_HOLOGRAM = 12;
    private static final int SLOT_INFO = 13;
    private static final int SLOT_LEADERBOARD = 14;
    private static final int SLOT_BROADCAST = 15;
    private static final int SLOT_RESPAWN = 16;
    private static final int SLOT_EFFECTS = 22;

    private static final int E_KNOCKBACK = 11;
    private static final int E_FIRE = 13;
    private static final int E_FATIGUE = 15;
    private static final int E_BACK = 22;

    private final WaluMetin plugin;

    public GUIManager(WaluMetin plugin) {
        this.plugin = plugin;
    }

    public void open(Player p, MetinStone stone) {
        LanguageManager lang = plugin.language();
        if (plugin.stones().type(stone) == null) {
            lang.send(p, "type-not-found", "%type%", stone.typeId());
            return;
        }
        MenuHolder holder = new MenuHolder(MenuHolder.Type.STONE, stone.id(), 0);
        Inventory inv = Bukkit.createInventory(holder, 36, ColorUtil.component(lang.msg("gui-title", "%id%", String.valueOf(stone.id()))));
        holder.setInventory(inv);
        render(inv, stone);
        p.openInventory(inv);
    }

    public void render(Inventory inv, MetinStone stone) {
        StoneManager sm = plugin.stones();
        LanguageManager lang = plugin.language();
        MetinType type = sm.type(stone);
        if (type == null) return;

        fill(inv);

        inv.setItem(SLOT_STONE, toggle(sm.isEnabled(stone) ? Material.LIME_DYE : Material.GRAY_DYE, "gui-toggle-stone", sm.isEnabled(stone)));
        inv.setItem(SLOT_MOBS, toggle(sm.isMobs(stone) ? Material.ZOMBIE_HEAD : Material.SKELETON_SKULL, "gui-toggle-mobs", sm.isMobs(stone)));
        inv.setItem(SLOT_HOLOGRAM, toggle(sm.isHologram(stone) ? Material.NAME_TAG : Material.PAPER, "gui-toggle-hologram", sm.isHologram(stone)));
        inv.setItem(SLOT_LEADERBOARD, toggle(sm.isLeaderboard(stone) ? Material.GOLD_INGOT : Material.COAL, "gui-toggle-leaderboard", sm.isLeaderboard(stone)));
        inv.setItem(SLOT_BROADCAST, toggle(sm.isBroadcast(stone) ? Material.BELL : Material.NOTE_BLOCK, "gui-toggle-broadcast", sm.isBroadcast(stone)));
        inv.setItem(SLOT_RESPAWN, ItemUtil.make(Material.CLOCK, lang.msg("gui-respawn"), List.of(lang.msg("gui-click-respawn"))));
        inv.setItem(SLOT_EFFECTS, ItemUtil.make(Material.BLAZE_POWDER, lang.msg("gui-effects"), List.of(lang.msg("effects-hint", "%percents%", plugin.effects().percentsText()), lang.msg("gui-effects-lore"))));

        inv.setItem(SLOT_INFO, ItemUtil.make(type.material(), lang.msg("gui-info"), lang.lines("gui-info-lore",
                "%id%", String.valueOf(stone.id()), "%stone%", ColorUtil.color(type.name()), "%hp%", String.valueOf(stone.hp), "%max%", String.valueOf(type.hp()), "%duration%", String.valueOf(type.duration()), "%state%", lang.stateName(stone.state))));
    }

    public void openEffects(Player p, MetinStone stone) {
        Inventory inv = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.STONE_EFFECTS, stone.id(), 0), 27,
                ColorUtil.component(plugin.language().msg("gui-effects-title", "%id%", String.valueOf(stone.id()))));
        renderEffects(inv, stone);
        p.openInventory(inv);
    }

    public void renderEffects(Inventory inv, MetinStone stone) {
        StoneManager sm = plugin.stones();
        LanguageManager lang = plugin.language();
        fill(inv);
        inv.setItem(E_KNOCKBACK, effectItem(Material.FEATHER, "effect-knockback", sm.isEffect(stone, EffectType.KNOCKBACK)));
        inv.setItem(E_FIRE, effectItem(Material.BLAZE_POWDER, "effect-fire", sm.isEffect(stone, EffectType.FIRE)));
        inv.setItem(E_FATIGUE, effectItem(Material.COBWEB, "effect-fatigue", sm.isEffect(stone, EffectType.FATIGUE)));
        inv.setItem(E_BACK, ItemUtil.make(Material.ARROW, lang.msg("back"), null));
    }

    private ItemStack effectItem(Material material, String key, boolean enabled) {
        LanguageManager lang = plugin.language();
        return ItemUtil.make(enabled ? material : Material.GRAY_DYE, lang.msg(key), List.of(
                lang.msg("gui-status", "%status%", lang.msg(enabled ? "on" : "off")),
                lang.msg("effects-hint", "%percents%", plugin.effects().percentsText()),
                lang.msg("gui-click")));
    }

    private void fill(Inventory inv) {
        ItemStack filler = ItemUtil.make(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
    }

    private ItemStack toggle(Material material, String nameKey, boolean enabled) {
        LanguageManager lang = plugin.language();
        return ItemUtil.make(material, lang.msg(nameKey), List.of(
                lang.msg("gui-status", "%status%", lang.msg(enabled ? "on" : "off")),
                lang.msg("gui-click")));
    }

    public void click(Player p, MenuHolder holder, int slot) {
        if (!p.hasPermission("walumetin.gui")) {
            p.closeInventory();
            return;
        }
        MetinStone stone = plugin.stones().get(holder.stoneId());
        if (stone == null) {
            p.closeInventory();
            return;
        }

        if (holder.type() == MenuHolder.Type.STONE_EFFECTS) {
            switch (slot) {
                case E_KNOCKBACK -> plugin.stones().toggleEffect(stone, EffectType.KNOCKBACK);
                case E_FIRE -> plugin.stones().toggleEffect(stone, EffectType.FIRE);
                case E_FATIGUE -> plugin.stones().toggleEffect(stone, EffectType.FATIGUE);
                case E_BACK -> Bukkit.getScheduler().runTask(plugin, () -> open(p, stone));
                default -> {}
            }
            renderEffects(holder.getInventory(), stone);
            return;
        }

        switch (slot) {
            case SLOT_STONE -> plugin.stones().setEnabled(stone, !plugin.stones().isEnabled(stone));
            case SLOT_MOBS -> plugin.stones().toggleMobs(stone);
            case SLOT_HOLOGRAM -> plugin.stones().toggleHologram(stone);
            case SLOT_LEADERBOARD -> plugin.stones().toggleLeaderboard(stone);
            case SLOT_BROADCAST -> plugin.stones().toggleBroadcast(stone);
            case SLOT_RESPAWN -> {
                plugin.stones().instantRespawn(stone);
                plugin.language().send(p, "instant-done");
            }
            case SLOT_EFFECTS -> Bukkit.getScheduler().runTask(plugin, () -> openEffects(p, stone));
            default -> {}
        }
        render(holder.getInventory(), stone);
    }
}
