package dev.walu.metin.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MetinStone {

    public enum State { ACTIVE, RESPAWNING, DISABLED }

    private final int id;
    private final String typeId;
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public Boolean enabledOverride;
    public Boolean mobsOverride;
    public Boolean broadcastOverride;
    public Boolean hologramOverride;
    public Boolean leaderboardOverride;
    public final Map<EffectType, Boolean> effectOverrides = new EnumMap<>(EffectType.class);

    public State state = State.ACTIVE;
    public int hp;
    public int remaining;
    public final Set<Integer> triggered = new HashSet<>();
    public final Set<Integer> effectTriggered = new HashSet<>();
    public UUID entityId;
    public org.bukkit.scheduler.BukkitTask task;

    public MetinStone(int id, String typeId, String world, int x, int y, int z) {
        this.id = id;
        this.typeId = typeId;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int id() { return id; }
    public String typeId() { return typeId; }
    public String world() { return world; }
    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
}
