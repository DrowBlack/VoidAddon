package com.VoidClan.addon.modules;

import com.VoidClan.addon.Main;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class FrameDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    // Items.DIAMOND_SWORD yerine string ile item bulma
    private final Setting<String> itemName = sgGeneral.add(new StringSetting.Builder()
        .name("item-name")
        .description("Dupe edilecek item adı (diamond_sword, netherite_ingot vb.)")
        .defaultValue("diamond_sword")
        .build()
    );
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("İşlem gecikmesi (tick)")
        .defaultValue(5)
        .min(1)
        .max(20)
        .build()
    );
    
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Frame arama menzili")
        .defaultValue(4.0)
        .min(1.0)
        .max(6.0)
        .build()
    );
    
    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Otomatik item değiştirme")
        .defaultValue(true)
        .build()
    );
    
    private int tickCounter = 0;
    private boolean isProcessing = false;
    
    public FrameDupe() {
        super(Main.VOID, "frame-dupe", "Frame dupe modülü");
    }
    
    @Override
    public void onActivate() {
        tickCounter = 0;
        isProcessing = false;
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        tickCounter++;
        if (tickCounter < delay.get()) return;
        tickCounter = 0;
        
        if (isProcessing) {
            isProcessing = false;
            return;
        }
        
        // İtem bul (string ile)
        FindItemResult dupeItemResult = findItemByName(itemName.get());
        if (!dupeItemResult.found()) {
            error("Item bulunamadı: " + itemName.get());
            return;
        }
        
        // Frame bul
        ItemFrameEntity frame = findNearestFrame();
        if (frame == null) {
            return;
        }
        
        // Dupe işlemi
        if (performDupe(frame, dupeItemResult)) {
            isProcessing = true;
        }
    }
    
    private FindItemResult findItemByName(String name) {
        // Inventory'de item ara
        return InvUtils.find(itemStack -> {
            if (itemStack.isEmpty()) return false;
            String stackName = itemStack.getItem().toString().toLowerCase();
            return stackName.contains(name.toLowerCase());
        });
    }
    
    private ItemFrameEntity findNearestFrame() {
        if (mc.player == null || mc.world == null) return null;
        
        Vec3d playerPos = mc.player.getPos();
        double rangeValue = range.get();
        
        Box searchBox = new Box(
            playerPos.x - rangeValue, playerPos.y - rangeValue, playerPos.z - rangeValue,
            playerPos.x + rangeValue, playerPos.y + rangeValue, playerPos.z + rangeValue
        );
        
        try {
            List<ItemFrameEntity> frames = mc.world.getEntitiesByClass(
                ItemFrameEntity.class, 
                searchBox, 
                frame -> frame != null && frame.isAlive()
            );
            
            if (frames.isEmpty()) return null;
            
            // En yakını bul
            ItemFrameEntity nearest = null;
            double minDistance = Double.MAX_VALUE;
            
            for (ItemFrameEntity frame : frames) {
                double distance = playerPos.distanceTo(frame.getPos());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = frame;
                }
            }
            
            return nearest;
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean performDupe(ItemFrameEntity frame, FindItemResult itemResult) {
        try {
            // Item'e geç
            if (autoSwitch.get()) {
                if (itemResult.isHotbar()) {
                    InvUtils.swap(itemResult.slot(), false);
                } else {
                    InvUtils.move().from(itemResult.slot()).toHotbar(mc.player.getInventory().getSelectedSlot());
                }
            }
            
            // Frame boşsa item koy
            if (frame.getHeldItemStack().isEmpty()) {
                mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                return true;
            } 
            // Frame doluysa kır
            else {
                mc.interactionManager.attackEntity(mc.player, frame);
                return true;
            }
        } catch (Exception e) {
            error("Dupe hatası: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getInfoString() {
        if (isProcessing) {
            return "İşlem yapılıyor...";
        }
        return "Hazır";
    }
}