package fr.tathan.sky_aesthetics.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
