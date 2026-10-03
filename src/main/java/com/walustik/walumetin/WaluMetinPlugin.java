package com.walustik.walumetin;

import com.walustik.walumetin.command.TextStoneCommand;
import com.walustik.walumetin.config.TextStoneConfigManager;
import com.walustik.walumetin.listener.TextStoneProtectionListener;
import com.walustik.walumetin.service.TextStoneService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * WaluMetin Plugin - Ana sınıf
 * Dinamik metin taşı sistemi (Aşama 1 / Beta)
 *
 * @author Walustik
 * @version 1.0.0
 */
public final class WaluMetinPlugin extends JavaPlugin {

    private TextStoneConfigManager configManager;
    private TextStoneService textStoneService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Config yöneticisini başlat
        configManager = new TextStoneConfigManager(this);
        configManager.reload();

        // Metin taşı servisini başlat
        textStoneService = new TextStoneService(this, configManager);
        textStoneService.startTickTask();

        // Komut ve event listener'ları kaydet
        getCommand("metintas").setExecutor(new TextStoneCommand(textStoneService));
        getServer().getPluginManager().registerEvents(new TextStoneProtectionListener(textStoneService), this);

        getLogger().info("WaluMetin plugin etkin (Sürüm: " + getDescription().getVersion() + ")");
    }

    @Override
    public void onDisable() {
        if (textStoneService != null) {
            textStoneService.removeAll();
            textStoneService.stopTickTask();
        }
        getLogger().info("WaluMetin plugin kapatıldı.");
    }
}
