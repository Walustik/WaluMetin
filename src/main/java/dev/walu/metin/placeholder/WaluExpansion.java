package dev.walu.metin.placeholder;

import dev.walu.metin.WaluMetin;
import dev.walu.metin.manager.LeaderboardManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Locale;

public final class WaluExpansion extends PlaceholderExpansion {

    private final WaluMetin plugin;

    public WaluExpansion(WaluMetin plugin) {
        this.plugin = plugin;
    }

    public static WaluExpansion create(WaluMetin plugin) {
        WaluExpansion expansion = new WaluExpansion(plugin);
        expansion.register(); 
        return expansion;
    }

    @Override
    public String getIdentifier() {
        return "walumetin";
    }

    @Override
    public String getAuthor() {
        return "Walu";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        LeaderboardManager leaderboard = plugin.leaderboard();
        String lower = params.toLowerCase(Locale.ROOT);

        if (lower.equals("kirdigim_tas")) return player == null ? "0" : String.valueOf(leaderboard.total(player.getUniqueId()));
        if (lower.equals("top1")) {
            List<LeaderboardManager.Entry> entries = leaderboard.topGlobal(1);
            return entries.isEmpty() ? "-" : entries.get(0).name();
        }
        if (lower.equals("top1_count")) {
            List<LeaderboardManager.Entry> entries = leaderboard.topGlobal(1);
            return entries.isEmpty() ? "0" : String.valueOf(entries.get(0).count());
        }
        if (lower.equals("total_stones")) return String.valueOf(plugin.stones().all().size());
        return "";
    }
}
