package fr.tathan.fabric.sky_aesthetics;

import net.fabricmc.api.ModInitializer;

import fr.tathan.SkyAesthetics;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SkyAestheticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SkyAesthetics.init();
    }


}
