package com.example.moneymod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Ustawienia + podmiana liczby w tekście. Plik: config/moneymod.json */
public class MoneyConfig {
    public static final int MIN = 100_000, MAX = 10_000_000, STEP = 100_000;

    // Pierwsza grupa z liczbą po słowie kluczowym (Money/Balance/Saldo/...) albo po znaku $
    public static final String DEFAULT_PATTERN =
            "(?i)(?:money|balance|saldo|pieni[ąa]dze|coins?|kasa|\\$)[^0-9\\n]{0,15}?"
          + "([0-9](?:[0-9.,]*[0-9])?(?:[kmbt](?![a-z]))?)";

    public int amount = MIN;
    public boolean enabled = true;
    public String pattern = DEFAULT_PATTERN;

    private static MoneyConfig INSTANCE = new MoneyConfig();
    private static Pattern compiled = Pattern.compile(DEFAULT_PATTERN);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MoneyConfig get() { return INSTANCE; }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("moneymod.json");
    }

    public static void load() {
        try {
            if (Files.exists(file())) {
                MoneyConfig c = GSON.fromJson(Files.readString(file()), MoneyConfig.class);
                if (c != null) INSTANCE = c;
            }
            compiled = Pattern.compile(INSTANCE.pattern);
        } catch (Exception e) {
            compiled = Pattern.compile(DEFAULT_PATTERN);
        }
        save();
    }

    public static void save() {
        try { Files.writeString(file(), GSON.toJson(INSTANCE)); } catch (Exception ignored) {}
    }

    private record Seg(String s, Style style) {}

    /** Zwraca nowy tekst z podmienioną kwotą albo null, jeśli nic nie znaleziono. */
    public static Text apply(Text in) {
        if (!INSTANCE.enabled || in == null) return null;

        List<Seg> segs = new ArrayList<>();
        in.visit((style, str) -> { segs.add(new Seg(str, style)); return Optional.empty(); }, Style.EMPTY);

        StringBuilder sb = new StringBuilder();
        for (Seg s : segs) sb.append(s.s());
        String full = sb.toString();
        if (full.isEmpty()) return null;

        Matcher m = compiled.matcher(full);
        if (!m.find()) return null;
        int g = -1;
        for (int i = 1; i <= m.groupCount(); i++) if (m.group(i) != null) { g = i; break; }
        if (g == -1) return null;

        int start = m.start(g), end = m.end(g);
        String rep = format(m.group(g), INSTANCE.amount);

        MutableText out = Text.empty();
        int pos = 0;
        boolean inserted = false;
        for (Seg seg : segs) {
            String s = seg.s();
            int a = pos, b = pos + s.length();
            pos = b;
            if (a < start) out.append(Text.literal(s.substring(0, Math.min(b, start) - a)).setStyle(seg.style()));
            if (b > start && a < end && !inserted) {
                out.append(Text.literal(rep).setStyle(seg.style()));
                inserted = true;
            }
            if (b > end) out.append(Text.literal(s.substring(Math.max(a, end) - a)).setStyle(seg.style()));
        }
        return out;
    }

    /** Formatuje kwotę tak samo jak oryginał: 1,234,567 / 1.234.567 / 1 234 567 / 2.5M / 1234567 */
    static String format(String orig, int value) {
        char last = orig.charAt(orig.length() - 1);
        if (Character.isLetter(last)) {
            boolean lower = Character.isLowerCase(last);
            String r;
            if (value >= 1_000_000) r = trim(value / 1_000_000.0) + "M";
            else r = trim(value / 1_000.0) + "K";
            return lower ? r.toLowerCase(Locale.ROOT) : r;
        }
        char sep = 0;
        for (char c : orig.toCharArray()) if (!Character.isDigit(c)) { sep = c; break; }
        String digits = Integer.toString(value);
        if (sep == 0) return digits;
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) r.append(sep);
            r.append(digits.charAt(i));
        }
        return r.toString();
    }

    private static String trim(double d) {
        String s = String.format(Locale.ROOT, "%.2f", d);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}
