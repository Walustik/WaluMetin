package com.walustik.walumetin.service;

import com.walustik.walumetin.WaluMetinPlugin;
import com.walustik.walumetin.config.TextStoneConfigManager;
import com.walustik.walumetin.config.TextStoneDefinition;
import com.walustik.walumetin.model.TextStoneState;
import com.walustik.walumetin.util.ColorUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Metin taşı sistemi mantığını yönetir
 * Oluşturma, hasar, yenileme, hologram güncellemesi
 */
public class TextStoneService {

    private final WaluMetinPlugin plugin;
    private final TextStoneConfigManager configManager;
    private final Map<String, TextStoneState> textStones = new HashMap<>();

    private BukkitTask tickTask;

    public TextStoneService(WaluMetinPlugin plugin, TextStoneConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Yenileme (respawn) kontrolü için tick task'ı başlatır
     * Saniyede bir çalışır
     */
    public void startTickTask() {
        if (tickTask != null) {
            tickTask.cancel();
        }

        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    /**
     * Tick task'ı durdurur
     */
    public void stopTickTask() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Her saniye çalışan işlemler
     * Yenileniyor olan taşları kontrol eder
     */
    private void tick() {
        long now = System.currentTimeMillis();

        for (TextStoneState state : new ArrayList<>(textStones.values())) {
            if (!state.isRespawning()) {
                continue;
            }

            // Yenileme süresi doldu mu kontrol et
            if (now >= state.getRespawnAtMillis()) {
                restoreState(state);
            } else {
                // Hologramı "Yenileniyor..." olarak göster
                ArmorStand hologram = state.getHologram();
                if (hologram != null && !hologram.isDead()) {
                    hologram.setCustomName(ColorUtil.colorize("&7Yenileniyor..."));
                }
            }
        }
    }

    /**
     * Bloğun metin taşı olup olmadığını kontrol eder
     *
     * @param block Kontrol edilecek blok
     * @return Metin taşıysa true
     */
    public boolean isTextStone(Block block) {
        if (block == null) {
            return false;
        }
        return textStones.containsKey(locationKey(block.getLocation()));
    }

    /**
     * Blokun metin taşı durumunu döndürür
     *
     * @param block Blok
     * @return TextStoneState veya null
     */
    public TextStoneState getTextStone(Block block) {
        if (block == null) {
            return null;
        }
        return textStones.get(locationKey(block.getLocation()));
    }

    /**
     * Metin taşı olarak kullanılamayan blok türlerini kontrol eder
     *
     * @param material Blok türü
     * @return Kullanılamıyorsa true
     */
    public boolean isBlockedCreationMaterial(Material material) {
        if (material == null) {
            return true;
        }

        return switch (material) {
            // Arayüzü olan bloklar
            case CRAFTING_TABLE, ANVIL, CHEST, FURNACE, FLOWER_POT,
                 // Yok edilebilir dekoratif bloklar
                 GRASS_BLOCK, TALL_GRASS, DANDELION, POPPY, BLUE_ORCHID,
                 ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP,
                 PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY,
                 WITHER_ROSE, SUNFLOWER, LILAC, ROSE_BUSH, PEONY -> true;
            default -> false;
        };
    }

    /**
     * Oyuncunun baktığı bloğu metin taşına dönüştürür
     *
     * @param player Oyuncu
     * @param key Metin taşı türü
     * @return Başarılıysa true
     */
    public boolean createFromTarget(Player player, String key) {
        TextStoneDefinition definition = configManager.getDefinition(key);

        if (definition == null) {
            player.sendMessage(
                    ColorUtil.colorize("&cBöyle bir metin taşı tipi yok: &e" + key)
            );
            return false;
        }

        if (isBlockedCreationMaterial(definition.material())) {
            player.sendMessage(
                    ColorUtil.colorize("&cBu blok tipi metin taşı olarak kullanılamaz.")
            );
            return false;
        }

        Block target = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);

        if (target == null) {
            player.sendMessage(
                    ColorUtil.colorize("&cBaktığınız blok bulunamadı.")
            );
            return false;
        }

        return createTextStone(target.getLocation(), definition);
    }

    /**
     * Belirtilen konuma metin taşı oluşturur
     *
     * @param location Konum
     * @param key Metin taşı türü
     * @return Başarılıysa true
     */
    public boolean createAtLocation(Location location, String key) {
        TextStoneDefinition definition = configManager.getDefinition(key);

        if (definition == null) {
            return false;
        }

        if (isBlockedCreationMaterial(definition.material())) {
            return false;
        }

        return createTextStone(location, definition);
    }

    /**
     * Konuma metin taşı oluşturur (iç metod)
     *
     * @param location Konum
     * @param definition Metin taşı tanımı
     * @return Başarılıysa true
     */
    private boolean createTextStone(Location location, TextStoneDefinition definition) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        Block block = location.getBlock();

        if (textStones.containsKey(locationKey(location))) {
            return false;
        }

        if (isBlockedCreationMaterial(block.getType())) {
            return false;
        }

        // Bloğu değiştirilecek türe ayarla
        block.setType(definition.material(), false);

        // Metin taşı durumunu oluştur ve ekle
        TextStoneState state = new TextStoneState(location.clone(), definition);
        textStones.put(state.locationKey(), state);

        // Hologramı güncelle
        updateHologram(state);

        return true;
    }

    /**
     * Metin taşına hasar verir
     *
     * @param block Blok
     * @param player Hasar veren oyuncu
     */
    public void damageBlock(Block block, Player player) {
        if (block == null) {
            return;
        }

        TextStoneState state = getTextStone(block);

        if (state == null) {
            return;
        }

        // Yenileniyor ise hasar verme
        if (state.isRespawning()) {
            return;
        }

        // Hasar ver
        state.damage(1);

        // Metin taşı kırıldı
        if (state.getHp() <= 0) {
            state.setRespawning(true);
            state.setRespawnAtMillis(
                    System.currentTimeMillis() + (state.getDefinition().durationSeconds() * 1000L)
            );
            block.setType(Material.BEDROCK, false);

            player.sendMessage(
                    ColorUtil.colorize("&cMetin taşı kırıldı! &7Yenileniyor...")
            );
            player.sendActionBar(
                    ColorUtil.colorize("&cKırıldı! &7Yenileniyor...")
            );
            updateHologram(state);

            plugin.getLogger().info(
                    "Metin taşı kırıldı: " + state.locationKey()
            );
            return;
        }

        // Eylem çubuğunda can durumunu göster
        player.sendActionBar(
                ColorUtil.colorize(
                        "&7" + state.getDefinition().displayName() + " &8["
                                + state.getHp() + "/" + state.getMaxHp() + " HP]"
                )
        );
        plugin.getLogger().info(
                "Metin taşı hasar aldı: " + state.locationKey() + " -> "
                        + state.getHp() + "/" + state.getMaxHp()
        );

        // Hologramı güncelle
        updateHologram(state);
    }

    /**
     * Metin taşını orijinal durumuna geri yükler (yenileme)
     *
     * @param state Metin taşı durumu
     */
    private void restoreState(TextStoneState state) {
        state.setRespawning(false);
        state.setHp(state.getMaxHp());

        Block block = state.getLocation().getBlock();
        block.setType(state.getDefinition().material(), false);

        plugin.getLogger().info(
                "Metin taşı yenilendi: " + state.locationKey()
        );

        updateHologram(state);
    }

    /**
     * Metin taşı üzerindeki hologramı günceller
     *
     * @param state Metin taşı durumu
     */
    private void updateHologram(TextStoneState state) {
        // Eski hologramı kaldır
        if (state.getHologram() != null && state.getHologram().isValid()) {
            state.getHologram().remove();
        }

        // Blok üzerinde 1.2 blok kadar yukarı konumlandır
        Location loc = state.getLocation().clone().add(0.5, 1.2, 0.5);

        // Yeni ArmorStand oluştur (hologram)
        ArmorStand armorStand = state.getLocation().getWorld().spawn(
                loc,
                ArmorStand.class,
                entity -> {
                    entity.setVisible(false);              // Görünmez
                    entity.setMarker(true);                // Çarpışmasız
                    entity.setGravity(false);              // Yerçekimi yok
                    entity.setCustomNameVisible(true);     // İsim görünür
                    entity.setInvulnerable(true);          // Hasar almaz
                    entity.setBasePlate(false);            // Baz yoktur
                    entity.setCanPickupItems(false);       // Eşya almaz
                    entity.setRemoveWhenFarAway(false);    // Uzaktan kaldırma yok
                    entity.setSilent(true);                // Sessiz
                    entity.setPersistent(false);           // Kalıcı değil

                    // İsim ayarla
                    if (state.isRespawning()) {
                        entity.setCustomName(ColorUtil.colorize("&7Yenileniyor..."));
                    } else {
                        String display = state.getDefinition().displayName() + " "
                                + ColorUtil.colorize("&7[" + state.getHp() + "/"
                                + state.getMaxHp() + " HP]");
                        entity.setCustomName(display);
                    }
                }
        );

        state.setHologram(armorStand);
    }

    /**
     * Tüm metin taşlarını kaldırır (plugin kapatılıyor)
     */
    public void removeAll() {
        for (TextStoneState state : new ArrayList<>(textStones.values())) {
            if (state.getHologram() != null && state.getHologram().isValid()) {
                state.getHologram().remove();
            }
            textStones.remove(state.locationKey());
        }
    }

    /**
     * Konum bazlı bir anahtar oluşturur
     *
     * @param location Konum
     * @return Konum anahtarı
     */
    private String locationKey(Location location) {
        return location.getWorld().getName()
                + ":" + location.getBlockX()
                + ":" + location.getBlockY()
                + ":" + location.getBlockZ();
    }
}
