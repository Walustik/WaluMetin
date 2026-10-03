package dev.walu.metin.gui;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.manager.LanguageManager;
import dev.walu.metin.model.MetinStone;
import dev.walu.metin.model.MetinType;
import dev.walu.metin.util.ColorUtil;
import dev.walu.metin.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AdminGUIManager {

    private static final int PER_PAGE = 45;

    private final WaluMetin plugin;
    private final NamespacedKey placeKey;
    private final NamespacedKey idKey;
    private final Set<UUID> deleteMode = new HashSet<>();

    public AdminGUIManager(WaluMetin plugin) {
        this.plugin = plugin;
        this.placeKey = new NamespacedKey(plugin, "place_stone");
        this.idKey = new NamespacedKey(plugin, "stone_id");
    }

    public NamespacedKey placeKey() { return placeKey; }

    public boolean isDeleteMode(Player p) { return deleteMode.contains(p.getUniqueId()); }

    public void clear(Player p) { deleteMode.remove(p.getUniqueId()); }

    private void toggleDelete(Player p) {
        if (!deleteMode.remove(p.getUniqueId())) {
            deleteMode.add(p.getUniqueId());
            plugin.language().send(p, "delete-on");
        } else {
            plugin.language().send(p, "delete-off");
        }
    }

    public ItemStack createPlaceItem(MetinType type) {
        LanguageManager lang = plugin.language();
        ItemStack item = ItemUtil.make(type.material(), ColorUtil.color(type.name()),
                lang.lines("place-item-lore",
                        "%hp%", String.valueOf(type.hp()),
                        "%duration%", String.valueOf(type.duration()),
                        "%name%", type.name()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(placeKey, PersistentDataType.STRING, type.id());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.ADMIN_MAIN, 0, 0), 27,
                ColorUtil.component(plugin.language().msg("admin-title")));
        fill(inv);
        inv.setItem(11, ItemUtil.make(Material.NETHER_STAR, plugin.language().msg("admin-get"), List.of(plugin.language().msg("gui-click"))));
        boolean del = isDeleteMode(p);
        inv.setItem(13, ItemUtil.make(del ? Material.TNT : Material.BARRIER, plugin.language().msg("admin-delete"), List.of(
                plugin.language().msg("gui-status", "%status%", plugin.language().msg(del ? "on" : "off")),
                plugin.language().msg("gui-click"))));
        inv.setItem(15, ItemUtil.make(Material.COMPASS, plugin.language().msg("admin-list"), List.of(plugin.language().msg("gui-click"))));
        p.openInventory(inv);
    }

    public void openSelect(Player p) {
        Inventory inv = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.ADMIN_SELECT, 0, 0), 54,
                ColorUtil.component(plugin.language().msg("select-title")));
        int slot = 0;
        for (MetinType type : plugin.stones().types()) {
            if (slot >= PER_PAGE) break;
            inv.setItem(slot++, createPlaceItem(type));
        }
        inv.setItem(49, ItemUtil.make(Material.ARROW, plugin.language().msg("back"), null));
        p.openInventory(inv);
    }

    public void openList(Player p, int page) {
        List<MetinStone> all = new ArrayList<>(plugin.stones().all());
        int maxPage = Math.max(0, (all.size() - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));
        Inventory inv = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.ADMIN_LIST, 0, page), 54,
                ColorUtil.component(plugin.language().msg("list-title", "%page%", String.valueOf(page + 1))));
        int from = page * PER_PAGE;
        for (int i = from; i < Math.min(all.size(), from + PER_PAGE); i++) {
            MetinStone stone = all.get(i);
            MetinType type = plugin.stones().type(stone);
            Material icon = type != null ? type.material() : Material.BARRIER;
            String label = "&e#" + stone.id() + " &7- " + (type != null ? type.name() : stone.typeId());
            ItemStack item = ItemUtil.make(icon, ColorUtil.color(label), plugin.language().lines("list-lore",
                    "%world%", stone.world(), "%x%", String.valueOf(stone.x()), "%y%", String.valueOf(stone.y()),
                    "%z%", String.valueOf(stone.z()), "%state%", plugin.language().stateName(stone.state())));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(idKey, PersistentDataType.INTEGER, stone.id());
                item.setItemMeta(meta);
            }
            inv.setItem(i - from, item);
        }
        if (page > 0) inv.setItem(45, ItemUtil.make(Material.ARROW, plugin.language().msg("page-prev"), null));
        inv.setItem(49, ItemUtil.make(Material.BARRIER, plugin.language().msg("back"), null));
        if (page < maxPage) inv.setItem(53, ItemUtil.make(Material.ARROW, plugin.language().msg("page-next"), null));
        p.openInventory(inv);
    }

    private void fill(Inventory inv) {
        ItemStack filler = ItemUtil.make(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
    }

    public void click(Player p, MenuHolder holder, InventoryClickEvent e) {
        if (!p.hasPermission("walumetin.admin")) {
            p.closeInventory();
            return;
        }
        int slot = e.getRawSlot();

        switch (holder.type()) {
            case ADMIN_MAIN -> {
                if (slot == 11) Bukkit.getScheduler().runTask(plugin, () -> openSelect(p));
                else if (slot == 13) {
                    toggleDelete(p);
                    Bukkit.getScheduler().runTask(plugin, () -> openMain(p));
                } else if (slot == 15) Bukkit.getScheduler().runTask(plugin, () -> openList(p, 0));
            }
            case ADMIN_SELECT -> {
                if (slot == 49) {
                    Bukkit.getScheduler().runTask(plugin, () -> openMain(p));
                    return;
                }
                ItemStack it = e.getCurrentItem();
                if (it == null || !it.hasItemMeta()) return;
                String id = it.getItemMeta().getPersistentDataContainer().get(placeKey, PersistentDataType.STRING);
                MetinType type = plugin.stones().getType(id);
                if (type == null) return;
                p.getInventory().addItem(createPlaceItem(type));
                plugin.language().send(p, "given-item", "%stone%", type.name());
                p.closeInventory();
            }
            case ADMIN_LIST -> {
                if (slot == 49) {
                    Bukkit.getScheduler().runTask(plugin, () -> openMain(p));
                    return;
                }
                if (slot == 45 && holder.page() > 0) {
                    Bukkit.getScheduler().runTask(plugin, () -> openList(p, holder.page() - 1));
                    return;
                }
                if (slot == 53) {
                    Bukkit.getScheduler().runTask(plugin, () -> openList(p, holder.page() + 1));
                    return;
                }
                ItemStack it = e.getCurrentItem();
                if (it == null || !it.hasItemMeta()) return;
                Integer id = it.getItemMeta().getPersistentDataContainer().get(this.idKey, PersistentDataType.INTEGER);
                if (id == null) return;
                MetinStone stone = plugin.stones().get(id);
                if (stone == null) {
                    plugin.language().send(p, "stone-not-found");
                    return;
                }
                if (e.getClick().isRightClick()) {
                    plugin.gui().open(p, stone);
                } else {
                    p.teleport(new org.bukkit.Location(Bukkit.getWorld(stone.world()), stone.x() + 0.5, stone.y() + 1.0, stone.z() + 0.5));
                    plugin.language().send(p, "teleported", "%id%", String.valueOf(stone.id()));
                    p.closeInventory();
                }
            }
            default -> {}
        }
    }
}
