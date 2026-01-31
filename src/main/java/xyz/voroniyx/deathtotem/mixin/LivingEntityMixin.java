package xyz.voroniyx.deathtotem.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.voroniyx.deathtotem.features.PlayerTotemPop;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("RETURN")
    )
    private void deathtotem$onTotemPop(
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) return;

        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self instanceof ServerPlayer player)) return;

        PlayerTotemPop.handle(player);
    }
}
