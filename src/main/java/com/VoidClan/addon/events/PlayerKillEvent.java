package com.VoidClan.addon.events;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerKillEvent {
    public final PlayerEntity killer;
    public final PlayerEntity killed;
    public final long timeOfKill;
    public final long lastHitTime;

    public PlayerKillEvent(PlayerEntity killer, PlayerEntity killed, long timeOfKill, long lastHitTime) {
        this.killer = killer;
        this.killed = killed;
        this.timeOfKill = timeOfKill;
        this.lastHitTime = lastHitTime;
    }
}
