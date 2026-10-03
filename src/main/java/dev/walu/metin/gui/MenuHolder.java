package dev.walu.metin.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {

    public enum Type { STONE, STONE_EFFECTS, ADMIN_MAIN, ADMIN_SELECT, ADMIN_LIST }

    private final Type type;
    private final int stoneId;
    private final int page;
    private Inventory inventory;

    public MenuHolder(Type type, int stoneId, int page) {
        this.type = type;
        this.stoneId = stoneId;
        this.page = page;
    }

    public Type type() { return type; }
    public int stoneId() { return stoneId; }
    public int page() { return page; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    @Override
    public Inventory getInventory() { return inventory; }
}
