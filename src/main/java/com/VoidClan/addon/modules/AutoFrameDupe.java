package com.VoidClan.addon.modules;

import com.VoidClan.addon.Main;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoFrameDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> shulkersOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("shulkers-only")
        .description("shulkers only")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Range")
        .defaultValue(5)
        .min(0)
        .max(6)
        .sliderMax(6)
        .build()
    );
    
    private final Setting<Integer> turns = sgGeneral.add(new IntSetting.Builder()
        .name("turns")
        .description("Turn Amount")
        .defaultValue(1)
        .min(0)
        .max(5)
        .sliderMax(5)
        .build()
    );
    
    private final Setting<Integer> ticks = sgGeneral.add(new IntSetting.Builder()
        .name("ticks")
        .description("Tick Count")
        .defaultValue(10)
        .min(1)
        .max(20)
        .sliderMax(20)
        .build()
    );
    
    private int timeoutTicks = 0;

    public AutoFrameDupe() {
        super(Main.VOID, "auto-frame-dupe", "AutoDupe");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemFrameEntity frame) {
                double distance = mc.player.distanceTo(frame);
                
                if (distance <= range.get()) {
                    if (timeoutTicks >= ticks.get()) {
                        ItemStack heldItem = frame.getHeldItemStack();
                        
                        // Boş çerçeveye item koy
                        if (heldItem.isEmpty()) {
                            if (shouldDupeItem()) {
                                mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                            }
                        }
                        // Dolu çerçeveyi işle
                        else if (!heldItem.isEmpty()) {
                            // Çerçeveyi döndür
                            for (int i = 0; i < turns.get(); i++) {
                                mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                            }
                            // Çerçeveyi kır
                            mc.player.attack(frame);
                            mc.interactionManager.attackEntity(mc.player, frame);
                            timeoutTicks = 0;
                        }
                    }
                    timeoutTicks++;
                }
            }
        }
    }
    
    private boolean shouldDupeItem() {
        if (!shulkersOnly.get()) return true;
        
        ItemStack mainHand = mc.player.getMainHandStack();
        if (mainHand.isEmpty()) return false;
        
        String itemName = mainHand.getItem().toString().toLowerCase();
        return itemName.contains("shulker");
    }
}