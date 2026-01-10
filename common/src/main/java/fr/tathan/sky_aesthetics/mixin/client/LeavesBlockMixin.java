package fr.tathan.sky_aesthetics.mixin.client;

import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

//    @WrapOperation(method = "animateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LeavesBlock;makeFallingLeavesParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
//    public void stopLeavesDrip(LeavesBlock instance, Level level, BlockPos pos, RandomSource random, BlockState state, BlockPos pos2, Operation<Void> original) {
//        if (!(level instanceof ClientLevel)) return;
//        SkyHelper.canRenderSky((ClientLevel) level, (planetSky -> {
//            if (planetSky.getRenderer().weather || (SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
//                original.call(level, pos, random, state, pos2);
//            }
//        }));
//    }
}
