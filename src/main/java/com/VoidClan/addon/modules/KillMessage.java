package com.VoidClan.addon.modules;

import com.VoidClan.addon.Main;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.List;
import java.util.Random;

public class KillMessage extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> killMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("kill-messages")
        .description("Messages to send when you kill a player. Use <name> for the player's name.")
        .defaultValue(List.of("Good fight <name>!", "<name> got rekt!", "EZ <name>"))
        .build()
    );

    private final Random random = new Random();

    public KillMessage() {
        super(Main.CHAT, "kill-message", "Sends a message when you kill a player.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 3) {
                Entity entity = null;
                try {
                    java.lang.reflect.Field entityField = packet.getClass().getDeclaredField("entity");
                    entityField.setAccessible(true);
                    entity = (Entity) entityField.get(packet);
                } catch (Exception e1) {
                    try {
                        java.lang.reflect.Field entityIdField = packet.getClass().getDeclaredField("entityId");
                        entityIdField.setAccessible(true);
                        int entityId = entityIdField.getInt(packet);
                        entity = mc.world.getEntityById(entityId);
                    } catch (Exception e2) {
                        try {
                            java.lang.reflect.Field idField = packet.getClass().getDeclaredField("id");
                            idField.setAccessible(true);
                            int entityId = idField.getInt(packet);
                            entity = mc.world.getEntityById(entityId);
                        } catch (Exception e3) {
                            for (PlayerEntity player : mc.world.getPlayers()) {
                                if (player.getHealth() <= 0 && !player.getUuid().equals(mc.player.getUuid())) {
                                    sendKillMessage(player.getName().getString());
                                    break;
                                }
                            }
                            return;
                        }
                    }
                }
                
                if (entity instanceof PlayerEntity player && !player.getUuid().equals(mc.player.getUuid())) {
                    sendKillMessage(player.getName().getString());
                }
            }
        }
    }

    private void sendKillMessage(String playerName) {
        if (killMessages.get().isEmpty()) return;

        String message = killMessages.get().get(random.nextInt(killMessages.get().size()));
        message = message.replace("<name>", playerName);
        
        ChatUtils.sendPlayerMsg(message);
    }
}