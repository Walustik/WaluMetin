package dev.walu.metin.model;

import org.bukkit.entity.EntityType;

public record MobSpawnRule(int percent, EntityType type, int amount) {}
