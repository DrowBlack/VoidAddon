package com.VoidClan.addon.modules;

import com.VoidClan.addon.Main;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;

import java.util.HashSet;

public class PingSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General
    private final Setting<Integer> ping = sgGeneral.add(new IntSetting.Builder()
        .name("ping")
        .description("Adds delay (in ms) to your ping.")
        .defaultValue(250)
        .min(1)
        .sliderMin(100)
        .sliderMax(1000)
        .build()
    );

    // Variables
    private final Object2LongMap<KeepAliveC2SPacket> packets = new Object2LongOpenHashMap<>();

    // Constructor
    public PingSpoof() {
        super(Main.Misc, "ping-spoof", "Artificially increases your ping by delaying KeepAlive packets.");
    }

    @Override
    public void onActivate() {
        packets.clear();
    }

    @Override
    public void onDeactivate() {
        if (!packets.isEmpty()) {
            for (KeepAliveC2SPacket packet : new HashSet<>(packets.keySet())) {
                if (packets.getLong(packet) + ping.get() <= System.currentTimeMillis()) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
            }
            packets.clear();
        }
    }

    // Cancel outgoing KeepAlive and queue it
    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (event.packet instanceof KeepAliveC2SPacket packet) {
            if (packets.containsKey(packet)) {
                packets.removeLong(packet);
                return;
            }

            packets.put(packet, System.currentTimeMillis());
            event.cancel(); // prevent immediate sending
        }
    }

    // Release delayed packets
    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        for (KeepAliveC2SPacket packet : new HashSet<>(packets.keySet())) {
            if (packets.getLong(packet) + ping.get() <= System.currentTimeMillis()) {
                mc.getNetworkHandler().sendPacket(packet);
                packets.removeLong(packet);
                break;
            }
        }
    }
}
