package dev.walu.metin.hologram;

import dev.walu.metin.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NativeBackend implements HologramBackend {

    private final Map<String, List<ArmorStand>> map = new HashMap<>();

    @Override
    public void set(String id, Location base, List<String> lines) {
        World world = base.getWorld();
        if (world == null) return;
        int count = lines.size();

        List<ArmorStand> stands = map.get(id);
        boolean valid = stands != null && stands.size() == count;
        if (valid) {
            for (ArmorStand stand : stands) {
                if (!stand.isValid()) {
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {
            remove(id);
            stands = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Location loc = base.clone().add(0, (count - 1 - i) * LINE, 0);
                ArmorStand stand = world.spawn(loc, ArmorStand.class, a -> {
                    a.setInvisible(true);
                    a.setMarker(true);
                    a.setGravity(false);
                    a.setInvulnerable(true);
                    a.setSilent(true);
                    a.setCustomNameVisible(true);
                    a.setPersistent(false);
                });
                stands.add(stand);
            }
            map.put(id, stands);
        }

        for (int i = 0; i < count; i++) {
            ArmorStand stand = stands.get(i);
            double y = base.getY() + (count - 1 - i) * LINE;
            Location location = stand.getLocation();
            location.setY(y);
            stand.teleport(location);
            stand.customName(ColorUtil.component(lines.get(i)));
        }
    }

    @Override
    public void remove(String id) {
        List<ArmorStand> stands = map.remove(id);
        if (stands == null) return;
        for (ArmorStand stand : stands) stand.remove();
    }
}
