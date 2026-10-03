package dev.walu.metin.hologram;

import dev.walu.metin.WaluMetin;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HolographicBackend implements HologramBackend {

    private final HolographicDisplaysAPI api;
    private final Map<String, Hologram> map = new HashMap<>();

    public HolographicBackend(WaluMetin plugin) {
        this.api = HolographicDisplaysAPI.get(plugin);
    }

    @Override
    public void set(String id, Location base, List<String> lines) {
        Hologram hologram = map.get(id);
        if (hologram == null || hologram.isDeleted()) {
            hologram = api.createHologram(base.clone().add(0, (lines.size() - 1) * LINE, 0));
            map.put(id, hologram);
        }
        hologram.getLines().clear();
        for (String line : lines) { hologram.getLines().appendText(line); }
    }

    @Override
    public void remove(String id) {
        Hologram hologram = map.remove(id);
        if (hologram != null && !hologram.isDeleted()) hologram.delete();
    }
}
