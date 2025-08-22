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

public class TotemMessage extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> totemMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("totem-messages")
        .description("Messages to send when a player uses a totem. Use <n> for the player's name.")
        .defaultValue(List.of("<n> just used a totem!", "Lucky <n> had a totem!", "<n> survived... barely!"))
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in seconds between totem messages to prevent spam.")
        .defaultValue(5)
        .min(0)
        .sliderMax(30)
        .build()
    );

    private final Random random = new Random();
    private long lastMessageTime = 0;

    public TotemMessage() {
        super(Main.CHAT, "totem-message", "Sends a message when a player uses a totem.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 35) {
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
                            System.out.println("EntityStatusS2CPacket fields:");
                            for (java.lang.reflect.Field field : packet.getClass().getDeclaredFields()) {
                                System.out.println("Field: " + field.getName() + " - Type: " + field.getType());
                            }
                            return;
                        }
                    }
                }
                
                if (entity instanceof PlayerEntity player) {
                    sendTotemMessage(player.getName().getString());
                }
            }
        }
    }

    private void sendTotemMessage(String playerName) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime < delay.get() * 1000L) return;
        
        if (totemMessages.get().isEmpty()) return;

        String message = totemMessages.get().get(random.nextInt(totemMessages.get().size()));
        message = message.replace("<n>", playerName);
        
        ChatUtils.sendPlayerMsg(message);
        lastMessageTime = currentTime;
    }
}