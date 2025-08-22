package com.VoidClan.addon.modules;

import com.VoidClan.addon.Main;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import com.VoidClan.addon.events.PlayerKillEvent;

import java.util.List;
import java.util.Random;

public class AutoEz extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("How many ticks to wait before sending the GG message.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    private final Setting<Integer> hitValiditySeconds = sgGeneral.add(new IntSetting.Builder()
        .name("hit-validity-seconds")
        .description("Time window for attributing a kill after hitting a player.")
        .defaultValue(5)
        .min(1)
        .sliderMax(30)
        .onChanged(this::onSettingChange)
        .build()
    );

    private final Setting<Boolean> randomMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Random Message")
        .description("Choose a random message from the list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> privateMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Private Message")
        .description("Send the GG message as a private /msg to the killed player.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> ggMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("GG Messages")
        .description("Messages to send after a kill.")
        .defaultValue(List.of("Ez!", "Good fight!", "Well played!", "Nice one!"))
        .build()
    );

    private final Random random = new Random();
    private int timer = 0;
    private boolean shouldSend = false;
    private int messageIndex = 0;
    private String lastKilledPlayer = "";

    public AutoEz() {
        super(Main.VOID, "Auto Ez", "Sends a message after a kill.");
    }

    @Override
    public void onActivate() {
        timer = 0;
        shouldSend = false;
        messageIndex = 0;
        onSettingChange(hitValiditySeconds.get());
    }

    private void onSettingChange(Integer newValue) {
        if (Main.killDetectionService != null) {
            Main.killDetectionService.setHitValiditySeconds(newValue);
        }
    }

    @EventHandler
    private void onPlayerKill(PlayerKillEvent event) {
        if (mc.player != null && event.killer.equals(mc.player)) {
            triggerGG(event.killed.getName().getString());
        }
    }

    public void triggerGG(String playerName) {
        shouldSend = true;
        timer = 0;
        lastKilledPlayer = playerName;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!shouldSend) return;
        timer++;
        if (timer >= tickDelay.get()) {
            sendGGMessage();
            shouldSend = false;
        }
    }

    private void sendGGMessage() {
        if (ggMessages.get().isEmpty()) return;

        String message;
        if (randomMessage.get()) {
            message = ggMessages.get().get(random.nextInt(ggMessages.get().size()));
        } else {
            message = ggMessages.get().get(messageIndex);
            messageIndex = (messageIndex + 1) % ggMessages.get().size();
        }

        if (privateMessage.get() && !lastKilledPlayer.isEmpty()) {
            ChatUtils.sendPlayerMsg("/msg " + lastKilledPlayer + " " + message);
        } else {
            ChatUtils.sendPlayerMsg(message);
        }
    }
}
