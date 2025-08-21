package com.VoidClan.addon.hud;

import com.VoidClan.addon.Main;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Identifier;

public class Logo extends HudElement {
    private final Identifier TEXTURE = Identifier.of("VoidClan", "textures/icon.png");
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> size = sgGeneral.add(new IntSetting.Builder()
        .name("Size")
        .description("Logo Size")
        .defaultValue(100)
        .min(1)
        .max(256)
        .sliderMax(256)
        .build()
    );

    public static final HudElementInfo<Logo> INFO = new HudElementInfo<>(
        Main.HUD_GROUP, "Logo", "Renders the logo", Logo::new);

    public Logo() {
        super(INFO);
    }


    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("Test"), 60);
        renderer.texture(TEXTURE, x, y, size.get(), size.get(), Color.WHITE);
        renderer.text(".gg/VoidClan", x + ((double) size.get() / 2) - (renderer.textWidth(".gg/VoidClan") / 2), y + size.get(), Color.WHITE, true);
    }
}
