package dev.walu.metin;

import dev.walu.metin.command.MetinCommand;
import dev.walu.metin.gui.AdminGUIManager;
import dev.walu.metin.gui.GUIManager;
import dev.walu.metin.listener.MenuListener;
import dev.walu.metin.listener.MobListener;
import dev.walu.metin.listener.StoneListener;
import dev.walu.metin.manager.EffectManager;
import dev.walu.metin.manager.HologramManager;
import dev.walu.metin.manager.LanguageManager;
import dev.walu.metin.manager.LeaderboardManager;
import dev.walu.metin.manager.MobManager;
import dev.walu.metin.manager.PlaceholderManager;
import dev.walu.metin.manager.RewardManager;
import dev.walu.metin.manager.StoneManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class WaluMetin extends JavaPlugin {

    private LanguageManager language;
    private RewardManager rewards;
    private MobManager mobs;
    private EffectManager effects;
    private LeaderboardManager leaderboard;
    private HologramManager holograms;
    private PlaceholderManager placeholders;
    private StoneManager stones;
    private GUIManager gui;
    private AdminGUIManager adminGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        language = new LanguageManager(this);
        rewards = new RewardManager(this);
        mobs = new MobManager(this);
        effects = new EffectManager(this);
        leaderboard = new LeaderboardManager(this);
        holograms = new HologramManager(this);
        placeholders = new PlaceholderManager(this);
        stones = new StoneManager(this);
        gui = new GUIManager(this);
        adminGui = new AdminGUIManager(this);

        language.load();
        holograms.load();
        leaderboard.load();
        stones.loadTypes();
        stones.loadData();

        getServer().getPluginManager().registerEvents(new StoneListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new MobListener(this), this);

        PluginCommand cmd = getCommand("metintas");
        if (cmd != null) {
            MetinCommand handler = new MetinCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getServer().getScheduler().runTask(this, () -> {
            stones.activateAll();
            placeholders.register();
        });
    }

    @Override
    public void onDisable() {
        if (placeholders != null) placeholders.unregister();
        if (stones != null) stones.shutdown();
        if (holograms != null) holograms.removeAll();
        if (leaderboard != null) leaderboard.shutdown();
    }

    public void reloadAll() {
        reloadConfig();
        language.load();
        stones.loadTypes();
        holograms.reload();
        stones.refreshAll();
    }

    public LanguageManager language() { return language; }
    public RewardManager rewards() { return rewards; }
    public MobManager mobs() { return mobs; }
    public EffectManager effects() { return effects; }
    public LeaderboardManager leaderboard() { return leaderboard; }
    public HologramManager holograms() { return holograms; }
    public PlaceholderManager placeholders() { return placeholders; }
    public StoneManager stones() { return stones; }
    public GUIManager gui() { return gui; }
    public AdminGUIManager adminGui() { return adminGui; }
}
