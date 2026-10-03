package dev.walu.metin.manager;

import dev.walu.metin.WaluMetin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public final class PlaceholderManager {

    private final WaluMetin plugin;

    public PlaceholderManager(WaluMetin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) return;
        new WaluExpansion(plugin).register();
    }

    public void unregister() {
        // no-op with soft dependency safety
    }

    public static final class WaluExpansion extends PlaceholderExpansion {

        private final WaluMetin plugin;

        public WaluExpansion(WaluMetin plugin) {
            this.plugin = plugin;
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
        public String onRequest(OfflinePlayer player, String params) {
            if (params.equalsIgnoreCase("kirdigim_tas")) return "0";
            if (params.equalsIgnoreCase("top1")) return "-";
            return "";
        }
    }
}
