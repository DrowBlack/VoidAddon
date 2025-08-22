package com.VoidClan.addon;

import com.VoidClan.addon.hud.ImageHud;
import com.VoidClan.addon.modules.*;
import com.VoidClan.addon.events.*;
import com.VoidClan.addon.services.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class Main extends MeteorAddon {
    public static KillDetectionService killDetectionService;
    public static final Category VOID = new Category("Void Addon");
    public static final Category CHAT = new Category("Void Bypass");
    public static final Category Misc = new Category("Void Misc");
    public static final HudGroup HUD_GROUP = new HudGroup("Void");
    public static final ArrayList<String> delayedMessages = new ArrayList<>();
    

    @Override
    public void onInitialize() {
        killDetectionService = new KillDetectionService();
        MeteorClient.EVENT_BUS.subscribe(killDetectionService);

        // Modules
        Modules.get().add(new FrameDupe());
        Modules.get().add(new AutoFrameDupe());
        Modules.get().add(new PortalFrameDupe());

        //Chat Bypass
        Modules.get().add(new ChatBypass());
        Modules.get().add(new LinkBypass());
        
        // AutoEz
        Modules.get().add(new AutoEz());

	Modules.get().add(new PingSpoof());

        Hud.get().register(ImageHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(VOID);
        Modules.registerCategory(CHAT);
        Modules.registerCategory(Misc);
    }

    @Override
    public String getPackage() {
        return "com.VoidClan.addon";
    }
}