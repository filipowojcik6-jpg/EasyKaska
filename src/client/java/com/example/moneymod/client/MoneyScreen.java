package com.example.moneymod.client;

import com.example.moneymod.MoneyConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class MoneyScreen extends Screen {
    public MoneyScreen() { super(Text.literal("Ustaw pieniądze")); }

    @Override
    protected void init() {
        MoneyConfig cfg = MoneyConfig.get();
        int cx = this.width / 2, cy = this.height / 2;
        double start = (cfg.amount - MoneyConfig.MIN) / (double) (MoneyConfig.MAX - MoneyConfig.MIN);

        addDrawableChild(new SliderWidget(cx - 100, cy - 30, 200, 20,
                Text.literal("Pieniądze: " + cfg.amount), start) {
            @Override protected void updateMessage() {
                setMessage(Text.literal("Pieniądze: " + cfg.amount));
            }
            @Override protected void applyValue() {
                int raw = (int) (MoneyConfig.MIN + value * (MoneyConfig.MAX - MoneyConfig.MIN));
                int v = Math.round(raw / (float) MoneyConfig.STEP) * MoneyConfig.STEP;
                cfg.amount = Math.max(MoneyConfig.MIN, Math.min(MoneyConfig.MAX, v));
            }
        });

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Podmiana: " + (cfg.enabled ? "WŁ" : "WYŁ")), b -> {
            cfg.enabled = !cfg.enabled;
            b.setMessage(Text.literal("Podmiana: " + (cfg.enabled ? "WŁ" : "WYŁ")));
        }).dimensions(cx - 100, cy - 5, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Zatwierdź"), b -> close())
                .dimensions(cx - 100, cy + 20, 200, 20).build());
    }

    @Override
    public void removed() { MoneyConfig.save(); }
}
