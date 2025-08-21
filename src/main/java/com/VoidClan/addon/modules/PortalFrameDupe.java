package com.VoidClan.addon.modules;

import com.VoidClan.addon.Main;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import java.util.stream.StreamSupport;

public class PortalFrameDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("İşlem gecikmesi (tick)")
        .defaultValue(5)
        .min(1)
        .max(50)
        .build()
    );
    
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("silent")
        .description("Slot değiştirmeyi gizle")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<Boolean> onlyShulker = sgGeneral.add(new BoolSetting.Builder()
        .name("only-shulker")
        .description("Sadece shulker box'ları kullan")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maksimum erişim mesafesi")
        .defaultValue(4.5)
        .min(1.0)
        .max(6.0)
        .build()
    );
    
    private int tickCount = 0;
    private int oldSlot = -1;
    private DupeState currentState = DupeState.PLACE_FRAME;
    private BlockPos targetPos = null;
    private ItemFrameEntity targetFrame = null;

    public PortalFrameDupe() {
        super(Main.VOID, "portal-frame-dupe", "Portal frame dupe exploit");
    }

    private enum DupeState {
        PLACE_FRAME,    // Item frame koy
        PLACE_ITEM,     // Shulker/item koy
        LIGHT_PORTAL,   // Portal yak
        BREAK_FRAME     // Frame kır
    }

    @Override
    public void onActivate() {
        if (silent.get()) {
           oldSlot = mc.player.getInventory().getSelectedSlot();
        }
        currentState = DupeState.PLACE_FRAME;
        targetPos = null;
        targetFrame = null;
        tickCount = 0;
    }

    @Override
    public void onDeactivate() {
        if (silent.get() && oldSlot != -1) {
            mc.player.getInventory().setSelectedSlot(oldSlot);
            oldSlot = -1;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (++tickCount < delay.get()) return;
        tickCount = 0;

        switch (currentState) {
            case PLACE_FRAME:
                placeItemFrame();
                break;
            case PLACE_ITEM:
                placeItemInFrame();
                break;
            case LIGHT_PORTAL:
                lightPortal();
                break;
            case BREAK_FRAME:
                breakFrame();
                break;
        }
    }

    private void placeItemFrame() {
        // Item frame bul
        FindItemResult frameResult = InvUtils.findInHotbar(Items.ITEM_FRAME);
        if (!frameResult.found()) {
            error("Item frame bulunamadı!");
            toggle();
            return;
        }

        // Uygun bir duvar bul (portal yakınında)
        BlockPos wallPos = findNearbyWall();
        if (wallPos == null) {
            error("Uygun duvar bulunamadı!");
            return;
        }

        // Item frame koy
        if (silent.get()) {
            InvUtils.swap(frameResult.slot(), false);
        }

        Direction face = Direction.NORTH; // Varsayılan yön
        BlockHitResult hitResult = new BlockHitResult(
            Vec3d.ofCenter(wallPos), face, wallPos, false
        );

        if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult).isAccepted()) {
            targetPos = wallPos;
            currentState = DupeState.PLACE_ITEM;
            info("Item frame yerleştirildi");
        }

        if (silent.get()) {
            mc.player.getInventory().setSelectedSlot(oldSlot);
        }
    }

    private void placeItemInFrame() {
        // Yakındaki item frame'i bul
        targetFrame = findNearbyItemFrame();
        if (targetFrame == null) {
            currentState = DupeState.PLACE_FRAME;
            return;
        }

        // Frame boşsa item koy
        if (targetFrame.getHeldItemStack().isEmpty()) {
            FindItemResult itemResult = findDupeItem();
            if (!itemResult.found()) {
                error("Dupe edilecek item bulunamadı!");
                toggle();
                return;
            }

            if (silent.get()) {
                InvUtils.swap(itemResult.slot(), false);
            }

            // Frame'e item koy
            if (mc.interactionManager.interactEntity(mc.player, targetFrame, Hand.MAIN_HAND).isAccepted()) {
                currentState = DupeState.LIGHT_PORTAL;
                info("Item frame'e item yerleştirildi");
            }

            if (silent.get()) {
                mc.player.getInventory().setSelectedSlot(oldSlot);
            }
        } else {
            currentState = DupeState.LIGHT_PORTAL;
        }
    }

    private void lightPortal() {
        // Flint and steel bul
        FindItemResult flintResult = InvUtils.findInHotbar(Items.FLINT_AND_STEEL);
        if (!flintResult.found()) {
            error("Flint and steel bulunamadı!");
            toggle();
            return;
        }

        // Portal frame bloğu bul
        BlockPos portalFramePos = findNearbyPortalFrame();
        if (portalFramePos == null) {
            error("Portal frame bulunamadı!");
            return;
        }

        if (silent.get()) {
            InvUtils.swap(flintResult.slot(), false);
        }

        // Portal yak
        BlockHitResult hitResult = new BlockHitResult(
            Vec3d.ofCenter(portalFramePos), Direction.UP, portalFramePos, false
        );

        if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult).isAccepted()) {
            currentState = DupeState.BREAK_FRAME;
            info("Portal yakıldı");
        }

        if (silent.get()) {
            mc.player.getInventory().setSelectedSlot(oldSlot);
        }
    }

    private void breakFrame() {
        if (targetFrame == null) {
            targetFrame = findNearbyItemFrame();
        }

        if (targetFrame != null && !targetFrame.getHeldItemStack().isEmpty()) {
            // Frame'i kır
            mc.interactionManager.attackEntity(mc.player, targetFrame);
            info("Frame kırıldı - Dupe tamamlandı!");
            
            // Döngüyü yeniden başlat
            currentState = DupeState.PLACE_FRAME;
            targetFrame = null;
            targetPos = null;
        } else {
            currentState = DupeState.PLACE_FRAME;
        }
    }

    private ItemFrameEntity findNearbyItemFrame() {
        return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
            .filter(e -> e instanceof ItemFrameEntity)
            .map(e -> (ItemFrameEntity) e)
            .filter(f -> mc.player.squaredDistanceTo(f) < range.get() * range.get())
            .findFirst()
            .orElse(null);
    }

    private BlockPos findNearbyWall() {
        BlockPos playerPos = mc.player.getBlockPos();
        
        // Oyuncunun etrafındaki blokları kontrol et
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    
                    // Solid blok mu kontrol et
                    if (!mc.world.getBlockState(pos).isAir() && 
                        mc.world.getBlockState(pos).isOpaque()) {
                        
                        // Yanında boş alan var mı kontrol et
                        for (Direction dir : Direction.values()) {
                            if (dir == Direction.UP || dir == Direction.DOWN) continue;
                            BlockPos adjacentPos = pos.offset(dir);
                            if (mc.world.getBlockState(adjacentPos).isAir()) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findNearbyPortalFrame() {
        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL ||
                        mc.world.getBlockState(pos).getBlock() == Blocks.END_PORTAL_FRAME) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private FindItemResult findDupeItem() {
        if (onlyShulker.get()) {
            // Sadece shulker box'ları ara
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem) {
                    if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                        return new FindItemResult(i, mc.player.getInventory().getStack(i).getCount());
                    }
                }
            }
        } else {
            // İlk uygun item'ı bul (boş slot değil)
            for (int i = 0; i < 9; i++) {
                if (!mc.player.getInventory().getStack(i).isEmpty() &&
                    mc.player.getInventory().getStack(i).getItem() != Items.ITEM_FRAME &&
                    mc.player.getInventory().getStack(i).getItem() != Items.FLINT_AND_STEEL) {
                    return new FindItemResult(i, mc.player.getInventory().getStack(i).getCount());
                }
            }
        }
        return new FindItemResult(0, 0);
    }

    private void info(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§7[§5Portal Dupe§7] §f" + message), false);
        }
    }

    private void error(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§7[§5Portal Dupe§7] §c" + message), false);
        }
    }
}