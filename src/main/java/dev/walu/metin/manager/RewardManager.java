package dev.walu.metin.manager;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class RewardManager {

    private final WaluMetin plugin;

    public RewardManager(WaluMetin plugin) {
        this.plugin = plugin;
    }

    public void give(List<Reward> rewards, Player player, Location dropAt) {
        if (rewards == null || rewards.isEmpty()) return;
        for (Reward reward : rewards) {
            if (!reward.roll()) continue;
            if (reward.isCommand()) {
                if (player == null) continue;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.command().replace("%player%", player.getName()));
                continue;
            }

            if (reward.item() == null) continue;
            ItemStack stack = new ItemStack(reward.item(), reward.amount());
            if (player != null) {
                Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
                for (ItemStack left : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            } else if (dropAt != null && dropAt.getWorld() != null) {
                dropAt.getWorld().dropItemNaturally(dropAt, stack);
            }
        }
    }
}
