package com.VoidClan.addon.hud;

import com.VoidClan.addon.Main;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ImageHud extends HudElement {
    public static final HudElementInfo<ImageHud> INFO = new HudElementInfo<>(Main.HUD_GROUP, "image", "Displays a custom image on HUD", ImageHud::new);

    // Önceden tanımlanmış resim listesi (kolay seçim için)
    public enum PresetImages {
        LOGO("1.png", "1"),
        ICON("icon.png", "VoidClan Icon"),
        VoidChan("Void-chan.png", "VoidChan"),
        BANNER("2.png", "2"),
        AVATAR("3.png", "3"),
        SWORD("4.png", "4"),
        SHIELD("5.png", "5"),
        CROWN("6.png", "6"),
        STAR("7.png", "7"),
        imo1("imo1.png", "imo1"),
        imo2("imo2.png", "imo2"),
        imo3("imo3.png", "imo3"),
        imo4("imo4.png", "imo4"),
        imo5("imo5.png", "imo5"),
        imo6("imo6.png", "imo6"),
        imo7("imo7.png", "imo7"),
        imo8("imo8.png", "imo8"),
        imo9("imo9.png", "imo9"),
        imo10("imo10.png", "imo10"),
        duck("duck.jpg", "duck"),
        bear("bear.jpg", "bear"),
        kuromi("kuromi.jpg", "kuromi"),
        CUSTOM("", "Custom File");

        private final String fileName;
        private final String displayName;

        PresetImages(String fileName, String displayName) {
            this.fileName = fileName;
            this.displayName = displayName;
        }

        public String getFileName() {
            return fileName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final SettingGroup sgImage = settings.createGroup("Image Selection");
    private final SettingGroup sgAppearance = settings.createGroup("Appearance");
    private final SettingGroup sgEffects = settings.createGroup("Effects");

    // Resim seçimi ayarları
    private final Setting<PresetImages> presetImage = sgImage.add(new EnumSetting.Builder<PresetImages>()
        .name("preset-image")
        .description("Hazır resim seçeneklerinden birini seç")
        .defaultValue(PresetImages.LOGO)
        .build()
    );

    private final Setting<String> customImageName = sgImage.add(new StringSetting.Builder()
        .name("custom-image")
        .description("Özel resim dosya adı (assets/images/ klasöründen)")
        .defaultValue("logo.png")
        .visible(() -> presetImage.get() == PresetImages.CUSTOM)
        .build()
    );

    private final Setting<Boolean> autoRefresh = sgImage.add(new BoolSetting.Builder()
        .name("auto-refresh")
        .description("Dosya değişikliklerini otomatik algıla")
        .defaultValue(true)
        .build()
    );

    // Görünüm ayarları
    private final Setting<Double> imageScale = sgAppearance.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Resim boyutu ölçeği")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .sliderRange(0.1, 3.0)
        .build()
    );

    private final Setting<Integer> imageOpacity = sgAppearance.add(new IntSetting.Builder()
        .name("opacity")
        .description("Resim şeffaflığı")
        .defaultValue(255)
        .min(0)
        .max(255)
        .sliderRange(0, 255)
        .build()
    );

    private final Setting<Boolean> maintainAspectRatio = sgAppearance.add(new BoolSetting.Builder()
        .name("maintain-aspect")
        .description("Orijinal en-boy oranını koru")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> maxWidth = sgAppearance.add(new IntSetting.Builder()
        .name("max-width")
        .description("Maksimum genişlik (piksel)")
        .defaultValue(256)
        .min(16)
        .max(512)
        .sliderRange(16, 512)
        .visible(() -> !maintainAspectRatio.get())
        .build()
    );

    private final Setting<Integer> maxHeight = sgAppearance.add(new IntSetting.Builder()
        .name("max-height")
        .description("Maksimum yükseklik (piksel)")
        .defaultValue(256)
        .min(16)
        .max(512)
        .sliderRange(16, 512)
        .visible(() -> !maintainAspectRatio.get())
        .build()
    );

    // Efekt ayarları
    private final Setting<Boolean> rainbow = sgEffects.add(new BoolSetting.Builder()
        .name("rainbow")
        .description("Rainbow efekti uygula")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> rainbowSpeed = sgEffects.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("Rainbow efekt hızı")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .sliderRange(0.1, 3.0)
        .visible(rainbow::get)
        .build()
    );

    private final Setting<SettingColor> tintColor = sgEffects.add(new ColorSetting.Builder()
        .name("tint-color")
        .description("Resme renk tonu uygula")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> !rainbow.get())
        .build()
    );

    private final Setting<Boolean> pulse = sgEffects.add(new BoolSetting.Builder()
        .name("pulse")
        .description("Nabız efekti")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> pulseSpeed = sgEffects.add(new DoubleSetting.Builder()
        .name("pulse-speed")
        .description("Nabız efekt hızı")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .sliderRange(0.1, 3.0)
        .visible(pulse::get)
        .build()
    );

    private final Setting<Boolean> showBackground = sgAppearance.add(new BoolSetting.Builder()
        .name("background")
        .description("Arka plan göster")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgAppearance.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Arka plan rengi")
        .defaultValue(new SettingColor(0, 0, 0, 100))
        .visible(showBackground::get)
        .build()
    );

    // Texture ve boyut bilgileri
    private Identifier textureId;
    private int imageWidth = 64;
    private int imageHeight = 64;
    private boolean textureLoaded = false;
    private String lastImageName = "";
    private PresetImages lastPreset = null;
    private long lastFileCheck = 0;
    
    // Mevcut dosyalar cache'i
    private Set<String> availableImages = new HashSet<>();
    private long lastDirScan = 0;

    public ImageHud() {
        super(INFO);
        scanImageDirectory();
    }

    @Override
    public void render(HudRenderer renderer) {
        // Otomatik yenileme kontrolü
        if (autoRefresh.get() && System.currentTimeMillis() - lastFileCheck > 2000) {
            checkForChanges();
            lastFileCheck = System.currentTimeMillis();
        }

        // Resim değiştiyse yeniden yükle
        String currentImageName = getCurrentImageName();
        if (!currentImageName.equals(lastImageName) || presetImage.get() != lastPreset) {
            loadImage();
            lastImageName = currentImageName;
            lastPreset = presetImage.get();
        }

        if (!textureLoaded || textureId == null) {
            // Fallback: Resim yüklenemezse bilgi göster
            String errorMsg = "Image: " + getCurrentImageName() + " (Not Found)";
            renderer.text(errorMsg, x, y, Color.RED, true);
            setSize(renderer.textWidth(errorMsg), renderer.textHeight());
            return;
        }

        // Boyut hesapla
        double scale = imageScale.get();
        if (pulse.get()) {
            double pulseValue = Math.sin(System.currentTimeMillis() / 1000.0 * pulseSpeed.get()) * 0.1 + 1.0;
            scale *= pulseValue;
        }

        int width, height;
        if (maintainAspectRatio.get()) {
            width = (int) (imageWidth * scale);
            height = (int) (imageHeight * scale);
        } else {
            width = (int) (maxWidth.get() * scale);
            height = (int) (maxHeight.get() * scale);
        }

        // Arka plan çiz
        if (showBackground.get()) {
            renderer.quad(x, y, width, height, backgroundColor.get());
        }

        // Renk hesapla
        Color color;
        if (rainbow.get()) {
            double time = System.currentTimeMillis() / 1000.0 * rainbowSpeed.get();
            color = Color.fromHsv((int)((time * 60) % 360), 1, 1);
            color.a = imageOpacity.get();
        } else {
            SettingColor settingColor = tintColor.get();
            color = new Color(settingColor.r, settingColor.g, settingColor.b, imageOpacity.get());
        }

        // Resmi çiz
        renderer.texture(textureId, x, y, width, height, color);

        // HUD boyutunu ayarla
        setSize(width, height);
    }

    private String getCurrentImageName() {
        if (presetImage.get() == PresetImages.CUSTOM) {
            return customImageName.get();
        }
        return presetImage.get().getFileName();
    }

    private void loadImage() {
        try {
            String imageName = getCurrentImageName();
            if (imageName.isEmpty()) {
                error("Resim adı boş!");
                textureLoaded = false;
                return;
            }

            // Addon'un resource klasöründen resim yükle
            String resourcePath = "/assets/images/" + imageName;
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            
            if (inputStream == null) {
                error("Resim dosyası bulunamadı: " + imageName);
                textureLoaded = false;
                showAvailableImages();
                return;
            }

            try (inputStream) {
                NativeImage nativeImage = NativeImage.read(inputStream);
                
                // Boyut bilgilerini al
                imageWidth = nativeImage.getWidth();
                imageHeight = nativeImage.getHeight();
                
                // Texture oluştur
                NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> "imagehud", nativeImage);
                
                // Eski texture'ı temizle
                if (textureId != null) {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
                }
                
                // Yeni texture'ı register et
                String textureName = "hud_image_" + imageName.replaceAll("\\.", "_").replaceAll("/", "_");
                textureId = Identifier.of("voidclan", textureName);
                MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
                
                textureLoaded = true;
                info("Resim yüklendi: " + imageName + " (" + imageWidth + "x" + imageHeight + ")");
                
            }
        } catch (IOException e) {
            error("Resim yüklenirken hata: " + e.getMessage());
            textureLoaded = false;
        }
    }

    private void checkForChanges() {
        // Dizini yeniden tara
        if (System.currentTimeMillis() - lastDirScan > 10000) { // 10 saniyede bir
            scanImageDirectory();
            lastDirScan = System.currentTimeMillis();
        }
    }

    private void scanImageDirectory() {
        availableImages.clear();
        try {
            // Yaygın resim formatlarını kontrol et
            String[] commonImages = {
                "1.png", "icon.png", "2.png", "3.png",
                "4.png", "5.png", "6.png", "7.png",
                "imo1.png", "imo2.png", "imo3.png", "imo4.png",
                "imo5.png", "imo6.png", "imo7.png" , "imo7.png",
                "imo8.png" , "imo9.png", "imo10.png" , "Void-chan.png",
                "duck.jpg", "bear.jpg", "kuromi.jpg"
            };

            for (String imageName : commonImages) {
                InputStream stream = getClass().getResourceAsStream("/assets/images/" + imageName);
                if (stream != null) {
                    availableImages.add(imageName);
                    try {
                        stream.close();
                    } catch (IOException ignored) {}
                }
            }
        } catch (Exception e) {
            error("Dizin tarama hatası: " + e.getMessage());
        }
    }

    private void showAvailableImages() {
        if (!availableImages.isEmpty()) {
            info("Mevcut resimler: " + String.join(", ", availableImages));
        } else {
            info("Hiç resim bulunamadı. Lütfen /assets/images/ klasörüne resim ekleyin.");
        }
    }

    @Override
    public void tick(HudRenderer renderer) {
        super.tick(renderer);
        
        // İlk kez yükleniyorsa resmi yükle
        if (!textureLoaded && textureId == null) {
            loadImage();
        }
    }

    // Temizlik için
    public void cleanup() {
        if (textureId != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
            textureId = null;
        }
        textureLoaded = false;
    }

    // Utility metodları
    public void refreshImage() {
        loadImage();
    }

    public void listImages() {
        scanImageDirectory();
        showAvailableImages();
    }

    private static void info(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                net.minecraft.text.Text.literal("§7[§bImage HUD§7] §a" + message), false
            );
        }
    }

    private static void error(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                net.minecraft.text.Text.literal("§7[§bImage HUD§7] §c" + message), false
            );
        }
    }
}