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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ImageHud extends HudElement {
    public static final HudElementInfo<ImageHud> INFO = new HudElementInfo<>(Main.HUD_GROUP, "image", "Displays an image from URL on HUD", ImageHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAppearance = settings.createGroup("Appearance");
    private final SettingGroup sgEffects = settings.createGroup("Effects");

    private final Setting<String> imageUrl = sgGeneral.add(new StringSetting.Builder()
        .name("image-url")
        .description("URL of image to display (.png, .jpg, .jpeg, .gif)")
        .defaultValue("https://example.com/image.png")
        .build());

    private final Setting<Double> imageScale = sgAppearance.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Image scale")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .sliderRange(0.1, 3.0)
        .build());

    private final Setting<Integer> imageOpacity = sgAppearance.add(new IntSetting.Builder()
        .name("opacity")
        .description("Image opacity")
        .defaultValue(255)
        .min(0)
        .max(255)
        .sliderRange(0, 255)
        .build());

    private final Setting<Boolean> maintainAspectRatio = sgAppearance.add(new BoolSetting.Builder()
        .name("maintain-aspect")
        .description("Maintain original aspect ratio")
        .defaultValue(true)
        .build());

    private final Setting<Integer> maxWidth = sgAppearance.add(new IntSetting.Builder()
        .name("max-width")
        .description("Maximum width (pixels)")
        .defaultValue(256)
        .min(16)
        .max(512)
        .sliderRange(16, 512)
        .visible(() -> !maintainAspectRatio.get())
        .build());

    private final Setting<Integer> maxHeight = sgAppearance.add(new IntSetting.Builder()
        .name("max-height")
        .description("Maximum height (pixels)")
        .defaultValue(256)
        .min(16)
        .max(512)
        .sliderRange(16, 512)
        .visible(() -> !maintainAspectRatio.get())
        .build());

    private final Setting<Boolean> rainbow = sgEffects.add(new BoolSetting.Builder()
        .name("rainbow")
        .description("Apply rainbow effect")
        .defaultValue(false)
        .build());

    private final Setting<Double> rainbowSpeed = sgEffects.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("Rainbow effect speed")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .sliderRange(0.1, 3.0)
        .visible(rainbow::get)
        .build());

    private final Setting<SettingColor> tintColor = sgEffects.add(new ColorSetting.Builder()
        .name("tint-color")
        .description("Apply color tint to image")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> !rainbow.get())
        .build());

    private final Setting<Boolean> pulse = sgEffects.add(new BoolSetting.Builder()
        .name("pulse")
        .description("Pulse effect")
        .defaultValue(false)
        .build());

    private final Setting<Double> pulseSpeed = sgEffects.add(new DoubleSetting.Builder()
        .name("pulse-speed")
        .description("Pulse effect speed")
        .defaultValue(1.0)
        .min(0.1)
        .max(5.0)
        .sliderRange(0.1, 3.0)
        .visible(pulse::get)
        .build());

    private final Setting<Boolean> showBackground = sgAppearance.add(new BoolSetting.Builder()
        .name("background")
        .description("Show background")
        .defaultValue(false)
        .build());

    private final Setting<SettingColor> backgroundColor = sgAppearance.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Background color")
        .defaultValue(new SettingColor(0, 0, 0, 100))
        .visible(showBackground::get)
        .build());

    private Identifier textureId;
    private int imageWidth = 64;
    private int imageHeight = 64;
    private boolean textureLoaded = false;
    private String lastImageUrl = "";
    private CompletableFuture<Void> imageLoadingTask;
    private boolean showError = false;
    private String errorMessage = "";

    private static final Pattern SUPPORTED_FORMATS = Pattern.compile(".*\\.(png|jpg|jpeg|gif)(\\?.*)?$", Pattern.CASE_INSENSITIVE);

    public ImageHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        String currentUrl = imageUrl.get();
        if (!currentUrl.equals(lastImageUrl)) {
            loadImageFromUrl(currentUrl);
            lastImageUrl = currentUrl;
            showError = false;
        }

        if (!textureLoaded || textureId == null) {
            if (showError) {
                String errorText = "Image: " + errorMessage;
                renderer.text(errorText, x, y, Color.RED, true);
                setSize(renderer.textWidth(errorText), renderer.textHeight());
            } else {
                String loadingMsg = "Loading image...";
                renderer.text(loadingMsg, x, y, Color.WHITE, true);
                setSize(renderer.textWidth(loadingMsg), renderer.textHeight());
            }
            return;
        }

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

        if (showBackground.get()) {
            renderer.quad(x, y, width, height, backgroundColor.get());
        }

        Color color;
        if (rainbow.get()) {
            double time = System.currentTimeMillis() / 1000.0 * rainbowSpeed.get();
            color = Color.fromHsv((int)((time * 60) % 360), 1, 1);
            color.a = imageOpacity.get();
        } else {
            SettingColor settingColor = tintColor.get();
            color = new Color(settingColor.r, settingColor.g, settingColor.b, imageOpacity.get());
        }

        renderer.texture(textureId, x, y, width, height, color);
        setSize(width, height);
    }

    private void loadImageFromUrl(String url) {
        if (imageLoadingTask != null && !imageLoadingTask.isDone()) {
            imageLoadingTask.cancel(true);
        }

        if (textureId != null) {
            try {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
            } catch (Exception e) {
            }
            textureId = null;
        }
        textureLoaded = false;
        showError = false;

        if (url == null || url.trim().isEmpty()) {
            showError = true;
            errorMessage = "URL is empty";
            return;
        }

        url = url.trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            showError = true;
            errorMessage = "Invalid URL (must start with http:// or https://)";
            return;
        }

        if (!SUPPORTED_FORMATS.matcher(url).matches()) {
            showError = true;
            errorMessage = "Unsupported image format (use .png, .jpg, .jpeg, or .gif)";
            return;
        }

        final String finalUrl = url;
        imageLoadingTask = CompletableFuture.runAsync(() -> {
            try {
                URL urlObj = new URL(finalUrl);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                connection.setRequestProperty("Accept", "image/png,image/jpeg,image/gif,image/*;q=0.9,*/*;q=0.8");
                connection.setInstanceFollowRedirects(true);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP " + responseCode + " " + connection.getResponseMessage());
                }

                String contentType = connection.getContentType();
                if (contentType != null && !contentType.toLowerCase().startsWith("image/")) {
                    throw new IOException("Not an image: " + contentType);
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] data = new byte[8192];
                    int bytesRead;
                    int totalBytes = 0;
                    int maxSize = 10 * 1024 * 1024;
                    
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        totalBytes += bytesRead;
                        if (totalBytes > maxSize) {
                            throw new IOException("Image too large (max 10MB)");
                        }
                        buffer.write(data, 0, bytesRead);
                    }
                }
                byte[] imageData = buffer.toByteArray();

                if (imageData.length == 0) {
                    throw new IOException("Empty image data");
                }

                BufferedImage bufferedImage;
                try (ByteArrayInputStream imageStream = new ByteArrayInputStream(imageData)) {
                    bufferedImage = ImageIO.read(imageStream);
                }

                if (bufferedImage == null) {
                    throw new IOException("Failed to decode image - unsupported format or corrupted file");
                }

                if (bufferedImage.getWidth() > 2048 || bufferedImage.getHeight() > 2048) {
                    throw new IOException("Image too large (max 2048x2048)");
                }

                NativeImage nativeImage = convertToNativeImage(bufferedImage);

                MinecraftClient.getInstance().execute(() -> {
                    try {
                        if (nativeImage == null) {
                            showError = true;
                            errorMessage = "Failed to convert image";
                            return;
                        }

                        imageWidth = nativeImage.getWidth();
                        imageHeight = nativeImage.getHeight();

                        NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> "imagehud", nativeImage);

                        String textureName = "hud_image_" + Math.abs(finalUrl.hashCode()) + "_" + System.currentTimeMillis();
                        textureId = Identifier.of("voidclan", textureName);
                        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);

                        textureLoaded = true;
                        showError = false;
                    } catch (Exception e) {
                        showError = true;
                        errorMessage = "Texture creation failed: " + e.getMessage();
                        e.printStackTrace();
                        
                        if (nativeImage != null) {
                            try {
                                nativeImage.close();
                            } catch (Exception ignored) {}
                        }
                    }
                });
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    showError = true;
                    errorMessage = "Load failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                });
            }
        });
    }

    private NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;
        
        try {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            
            NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    
                    int alpha = (rgb >> 24) & 0xFF;
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    int abgr = (alpha << 24) | (blue << 16) | (green << 8) | red;
                    
                    nativeImage.setColor(x, y, abgr);
                }
            }
            
            return nativeImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void remove() {
        if (textureId != null) {
            try {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
            } catch (Exception e) {
            }
            textureId = null;
        }
        textureLoaded = false;
        
        if (imageLoadingTask != null && !imageLoadingTask.isDone()) {
            imageLoadingTask.cancel(true);
        }
    }
}