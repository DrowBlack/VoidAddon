package com.VoidClan.addon.modules;
import com.VoidClan.addon.Main;

import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("bypass-mode")
            .description("Which Mode Will Used")
            .defaultValue(Mode.Unicode)
            .build()
    );
    
    private final Setting<Boolean> randomMode = sgGeneral.add(new BoolSetting.Builder()
            .name("random-mode")
            .description("Random Bypass Character")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Integer> intensity = sgGeneral.add(new IntSetting.Builder()
            .name("intensity")
            .description("Bypass yoğunluğu (1-10)")
            .defaultValue(3)
            .min(1)
            .max(10)
            .build()
    );

    private final Random random = new Random();
    private final Map<Character, String[]> unicodeMap = new HashMap<>();
    private final Map<Character, String[]> invisibleMap = new HashMap<>();
    
    public ChatBypass() {
        super(Main.CHAT, "chat-bypass", "Bypass Advertise Protection");
        initializeMaps();
    }
    
    private void initializeMaps() {
        unicodeMap.put('a', new String[]{"а", "ɑ", "α", "а̀", "ā", "ă", "ą", "ä", "å", "à", "á", "â", "ã"});
        unicodeMap.put('e', new String[]{"е", "ë", "ê", "é", "è", "ē", "ĕ", "ė", "ę", "ě", "ҽ", "е̇"});
        unicodeMap.put('o', new String[]{"о", "ο", "ö", "ô", "ó", "ò", "õ", "ō", "ŏ", "ő", "ø", "о̀"});
        unicodeMap.put('i', new String[]{"і", "ι", "ï", "î", "í", "ì", "ĩ", "ī", "ĭ", "į", "ı", "і̇"});
        unicodeMap.put('u', new String[]{"υ", "ü", "û", "ú", "ù", "ũ", "ū", "ŭ", "ů", "ű", "ų", "ụ"});
        unicodeMap.put('c', new String[]{"с", "ĉ", "ċ", "č", "ç", "ć", "с̌", "ҫ"});
        unicodeMap.put('p', new String[]{"р", "ρ", "р̌", "р̣"});
        unicodeMap.put('h', new String[]{"һ", "ħ", "ĥ", "ḥ", "ḩ", "ḫ", "ḧ"});
        unicodeMap.put('x', new String[]{"х", "χ", "ҳ", "х̣"});
        unicodeMap.put('y', new String[]{"у", "ỳ", "ý", "ŷ", "ÿ", "ȳ", "ỹ", "ȝ"});
        unicodeMap.put('k', new String[]{"κ", "ķ", "ḱ", "ḳ", "ḵ", "ĸ"});
        unicodeMap.put('n', new String[]{"п", "ň", "ñ", "ń", "ņ", "ň", "ŋ", "ṇ", "ṅ", "ṉ"});
        unicodeMap.put('m', new String[]{"м", "ɱ", "ḿ", "ṁ", "ṃ"});
        unicodeMap.put('t', new String[]{"т", "ť", "ţ", "ṫ", "ṭ", "ṯ", "ṱ", "ŧ"});
        unicodeMap.put('r', new String[]{"г", "ρ", "ř", "ŕ", "ŗ", "ṙ", "ṛ", "ṝ", "ṟ"});
        unicodeMap.put('b', new String[]{"ḃ", "ḅ", "ḇ", "ɓ", "ƀ"});
        unicodeMap.put('d', new String[]{"ď", "đ", "ḋ", "ḍ", "ḏ", "ḑ", "ḓ", "ɖ", "ƌ"});
        unicodeMap.put('f', new String[]{"ḟ", "ƒ", "ḟ"});
        unicodeMap.put('g', new String[]{"ğ", "ģ", "ĝ", "ġ", "ḡ", "ɠ", "ǥ", "ǧ", "ǧ"});
        unicodeMap.put('j', new String[]{"ĵ", "ɉ", "ǰ"});
        unicodeMap.put('l', new String[]{"ľ", "ļ", "ł", "ḷ", "ḹ", "ḻ", "ḽ", "ŀ", "ƚ"});
        unicodeMap.put('q', new String[]{"ʠ", "ɋ"});
        unicodeMap.put('s', new String[]{"ѕ", "ś", "ŝ", "ş", "š", "ṡ", "ṣ", "ṥ", "ṧ", "ṩ", "ș", "ş"});
        unicodeMap.put('v', new String[]{"ѵ", "ṽ", "ṿ", "ʋ", "ѷ"});
        unicodeMap.put('w', new String[]{"ŵ", "ẃ", "ẅ", "ẇ", "ẉ", "ẁ", "ʍ"});
        unicodeMap.put('z', new String[]{"ź", "ż", "ž", "ẑ", "ẓ", "ẕ", "ƶ", "ȥ"});
        
        invisibleMap.put(' ', new String[]{"\u200B", "\u200C", "\u200D", "\u2060", "\uFEFF"});
        invisibleMap.put('.', new String[]{".\u200B", ".\u200C", ".\u2060"});
        invisibleMap.put(',', new String[]{",\u200B", ",\u200C", ",\u2060"});
    }
    
    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        if (!isActive()) return;
        
        String original = event.message;
        String bypassed = "";
        
        switch (mode.get()) {
            case Unicode:
                bypassed = applyUnicodeBypass(original);
                break;
            case Invisible:
                bypassed = applyInvisibleBypass(original);
                break;
            case Mixed:
                bypassed = applyMixedBypass(original);
                break;
            case Leetspeak:
                bypassed = applyLeetspeakBypass(original);
                break;
            case Advanced:
                bypassed = applyAdvancedBypass(original);
                break;
        }
        
        if (!bypassed.equals(original)) {
            event.message = bypassed;
        }
    }
    
    private String applyUnicodeBypass(String text) {
        StringBuilder result = new StringBuilder();
        int intensityLevel = intensity.get();
        
        for (char c : text.toCharArray()) {
            char lower = Character.toLowerCase(c);
            
            if (unicodeMap.containsKey(lower) && random.nextInt(10) < intensityLevel) {
                String[] replacements = unicodeMap.get(lower);
                String replacement = replacements[random.nextInt(replacements.length)];
                result.append(Character.isUpperCase(c) ? replacement.toUpperCase() : replacement);
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private String applyInvisibleBypass(String text) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            result.append(c);
            
            if (random.nextInt(10) < intensity.get() && invisibleMap.containsKey(c)) {
                String[] invisibles = invisibleMap.get(c);
                result.append(invisibles[random.nextInt(invisibles.length)]);
            }
            
            if (i < text.length() - 1 && random.nextInt(15) < intensity.get()) {
                result.append("\u200B");
            }
        }
        
        return result.toString();
    }
    
    private String applyMixedBypass(String text) {
        String result = text;
        
        if (random.nextBoolean()) {
            result = applyUnicodeBypass(result);
        }
        
        if (random.nextBoolean()) {
            result = applyInvisibleBypass(result);
        }
        
        return result;
    }
    
    private String applyLeetspeakBypass(String text) {
        Map<Character, Character> leetMap = new HashMap<>();
        leetMap.put('a', '@');
        leetMap.put('e', '3');
        leetMap.put('i', '!');
        leetMap.put('o', '0');
        leetMap.put('s', '$');
        leetMap.put('t', '7');
        leetMap.put('l', '1');
        leetMap.put('g', '9');
        
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            char lower = Character.toLowerCase(c);
            if (leetMap.containsKey(lower) && random.nextInt(10) < intensity.get()) {
                result.append(leetMap.get(lower));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private String applyAdvancedBypass(String text) {
        String result = text;
        
        result = addRandomSpacing(result);
        result = applyUnicodeBypass(result);
        result = applyInvisibleBypass(result);
        result = addHomoglyphs(result);
        
        return result;
    }
    
    private String addRandomSpacing(String text) {
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        
        for (int i = 0; i < words.length; i++) {
            result.append(words[i]);
            if (i < words.length - 1) {
                if (random.nextInt(5) < intensity.get()) {
                    result.append("  ");
                } else {
                    result.append(" ");
                }
            }
        }
        
        return result.toString();
    }
    
    private String addHomoglyphs(String text) {
        Map<Character, Character> homoglyphs = new HashMap<>();
        homoglyphs.put('0', 'О');
        homoglyphs.put('6', 'б');
        homoglyphs.put('9', 'գ');
        
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if (homoglyphs.containsKey(c) && random.nextInt(10) < intensity.get()) {
                result.append(homoglyphs.get(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    public enum Mode {
        Unicode("Unicode"),
        Invisible("Görünmez"),
        Mixed("Karışık"),
        Leetspeak("Leetspeak"),
        Advanced("Gelişmiş");
        
        private final String title;
        
        Mode(String title) {
            this.title = title;
        }
        
        @Override
        public String toString() {
            return title;
        }
    }
}