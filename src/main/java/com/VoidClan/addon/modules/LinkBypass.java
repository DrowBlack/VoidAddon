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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<BypassMode> mode = sgGeneral.add(new EnumSetting.Builder<BypassMode>()
            .name("bypass-mode")
            .description("Link bypass yöntemi")
            .defaultValue(BypassMode.Unicode)
            .build()
    );
    
    private final Setting<Boolean> autoDetect = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-detect")
            .description("Linkleri otomatik algıla ve bypass et")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> maskDomains = sgGeneral.add(new BoolSetting.Builder()
            .name("mask-domains")
            .description("Domain isimlerini maskele")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> breakLinks = sgGeneral.add(new BoolSetting.Builder()
            .name("break-links")
            .description("Linkleri böl ve görünmez karakterler ekle")
            .defaultValue(true)
            .build()
    );

    private final Random random = new Random();
    private final Map<Character, String[]> unicodeMap = new HashMap<>();
    private final Pattern urlPattern = Pattern.compile(
        "(https?://)?(www\\.)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?",
        Pattern.CASE_INSENSITIVE
    );
    
    public LinkBypass() {
        super(Main.CHAT, "link-bypass", "Bypasses Link Protection");
        initializeUnicodeMap();
    }
    
    private void initializeUnicodeMap() {
        unicodeMap.put('a', new String[]{"а", "ɑ", "α", "ā", "à", "á", "â"});
        unicodeMap.put('e', new String[]{"е", "ë", "ê", "é", "è", "ē", "ě"});
        unicodeMap.put('o', new String[]{"о", "ο", "ö", "ô", "ó", "ò", "õ", "ø"});
        unicodeMap.put('i', new String[]{"і", "ι", "ï", "î", "í", "ì", "ī", "į"});
        unicodeMap.put('u', new String[]{"υ", "ü", "û", "ú", "ù", "ū", "ů"});
        unicodeMap.put('c', new String[]{"с", "ĉ", "ċ", "č", "ç", "ć"});
        unicodeMap.put('p', new String[]{"р", "ρ", "þ"});
        unicodeMap.put('h', new String[]{"һ", "ħ", "ĥ", "ḥ"});
        unicodeMap.put('x', new String[]{"х", "χ", "ҳ"});
        unicodeMap.put('y', new String[]{"у", "ỳ", "ý", "ÿ", "ȳ"});
        unicodeMap.put('n', new String[]{"п", "ň", "ñ", "ń", "ņ"});
        unicodeMap.put('m', new String[]{"м", "ɱ", "ḿ", "ṁ"});
        unicodeMap.put('t', new String[]{"т", "ť", "ţ", "ṫ", "ṭ"});
        unicodeMap.put('r', new String[]{"г", "ř", "ŕ", "ŗ", "ṙ"});
        unicodeMap.put('w', new String[]{"ω", "ŵ", "ẃ", "ẅ", "ẇ"});
        unicodeMap.put('s', new String[]{"ѕ", "ś", "ŝ", "š", "ṡ"});
        unicodeMap.put('d', new String[]{"ď", "đ", "ḋ", "ḍ", "ɖ"});
        unicodeMap.put('g', new String[]{"ğ", "ģ", "ĝ", "ġ", "ḡ"});
        unicodeMap.put('l', new String[]{"ľ", "ļ", "ł", "ḷ", "ḹ"});
        unicodeMap.put('b', new String[]{"ḃ", "ḅ", "ḇ", "ɓ"});
        unicodeMap.put('v', new String[]{"ѵ", "ṽ", "ṿ", "ʋ"});
        unicodeMap.put('k', new String[]{"κ", "ķ", "ḱ", "ḳ"});
        unicodeMap.put('j', new String[]{"ĵ", "ɉ", "ǰ"});
        unicodeMap.put('f', new String[]{"ḟ", "ƒ"});
        unicodeMap.put('z', new String[]{"ź", "ż", "ž", "ẑ"});
    }
    
    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        if (!isActive()) return;
        
        String original = event.message;
        String processed = original;
        
        if (autoDetect.get()) {
            processed = processLinks(processed);
        }
        
        if (!processed.equals(original)) {
            event.message = processed;
        }
    }
    
    private String processLinks(String text) {
        Matcher matcher = urlPattern.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String url = matcher.group();
            String bypassedUrl = bypassLink(url);
            matcher.appendReplacement(result, Matcher.quoteReplacement(bypassedUrl));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String bypassLink(String url) {
        String result = url;
        
        switch (mode.get()) {
            case Unicode:
                result = applyUnicodeBypass(result);
                break;
            case Invisible:
                result = applyInvisibleBypass(result);
                break;
            case Dot:
                result = applyDotBypass(result);
                break;
            case Brackets:
                result = applyBracketBypass(result);
                break;
            case Advanced:
                result = applyAdvancedBypass(result);
                break;
        }
        
        if (maskDomains.get()) {
            result = maskDomain(result);
        }
        
        if (breakLinks.get()) {
            result = breakLink(result);
        }
        
        return result;
    }
    
    private String applyUnicodeBypass(String url) {
        StringBuilder result = new StringBuilder();
        
        for (char c : url.toCharArray()) {
            char lower = Character.toLowerCase(c);
            
            if (unicodeMap.containsKey(lower) && random.nextInt(3) == 0) {
                String[] replacements = unicodeMap.get(lower);
                String replacement = replacements[random.nextInt(replacements.length)];
                result.append(Character.isUpperCase(c) ? replacement.toUpperCase() : replacement);
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private String applyInvisibleBypass(String url) {
        StringBuilder result = new StringBuilder();
        String[] invisibleChars = {"\u200B", "\u200C", "\u200D", "\u2060"};
        
        for (int i = 0; i < url.length(); i++) {
            result.append(url.charAt(i));
            
            if (i > 0 && i < url.length() - 1 && random.nextInt(4) == 0) {
                result.append(invisibleChars[random.nextInt(invisibleChars.length)]);
            }
        }
        
        return result.toString();
    }
    
    private String applyDotBypass(String url) {
        return url.replace(".", "[.]")
                 .replace("://", "[://]")
                 .replace("www", "[www]");
    }
    
    private String applyBracketBypass(String url) {
        StringBuilder result = new StringBuilder();
        boolean inProtocol = true;
        
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            
            if (inProtocol && c == '/') {
                inProtocol = false;
            }
            
            if (!inProtocol && (c == '.' || c == '/' || c == '?') && random.nextBoolean()) {
                result.append('[').append(c).append(']');
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private String applyAdvancedBypass(String url) {
        String result = url;
        
        result = applyUnicodeBypass(result);
        result = applyInvisibleBypass(result);
        
        if (random.nextBoolean()) {
            result = applyDotBypass(result);
        }
        
        return result;
    }
    
    private String maskDomain(String url) {
        String[] commonDomains = {"com", "net", "org", "io", "gg", "co"};
        String result = url;
        
        for (String domain : commonDomains) {
            if (result.contains("." + domain)) {
                switch (random.nextInt(3)) {
                    case 0:
                        result = result.replace("." + domain, "[.]" + domain);
                        break;
                    case 1:
                        result = result.replace("." + domain, "." + domain.replace("o", "0"));
                        break;
                    case 2:
                        result = result.replace("." + domain, ".\u200B" + domain);
                        break;
                }
                break;
            }
        }
        
        return result;
    }
    
    private String breakLink(String url) {
        if (url.length() < 10) return url;
        
        StringBuilder result = new StringBuilder();
        int breakPoint = url.length() / 2 + random.nextInt(url.length() / 4);
        
        result.append(url.substring(0, breakPoint));
        
        String[] breakMethods = {
            " ",
            "\u200B",
            " \u200B ",
            "[continue]",
            "..."
        };
        
        result.append(breakMethods[random.nextInt(breakMethods.length)]);
        result.append(url.substring(breakPoint));
        
        return result.toString();
    }
    
    public enum BypassMode {
        Unicode("Unicode Karakterler"),
        Invisible("Görünmez Karakterler"),
        Dot("Nokta Maskeleme"),
        Brackets("Köşeli Parantez"),
        Advanced("Gelişmiş Karışım");
        
        private final String title;
        
        BypassMode(String title) {
            this.title = title;
        }
        
        @Override
        public String toString() {
            return title;
        }
    }
}