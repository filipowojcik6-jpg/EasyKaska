package com.example.moneymod.mixin.client;

import com.example.moneymod.MoneyConfig;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Linie sidebara i nicki w tabie przechodzą przez Team.decorateName (prefix + nazwa + suffix). */
@Mixin(Team.class)
public class TeamMixin {
    @Inject(method = "decorateName", at = @At("RETURN"), cancellable = true)
    private static void moneymod$replace(AbstractTeam team, Text name,
                                         CallbackInfoReturnable<MutableText> cir) {
        Text changed = MoneyConfig.apply(cir.getReturnValue());
        if (changed != null) cir.setReturnValue(changed.copy());
    }
}
