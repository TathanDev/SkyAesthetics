package fr.tathan.sky_aesthetics.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.settings.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class SkyPropertiesProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private String modid = SkyAesthetics.MODID;
    private final Codec<SkyProperties> codec;
    private final ResourceKey<Registry<SkyProperties>> registry = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(modid, "sky_aesthetics"));

    public SkyPropertiesProvider(PackOutput packOutput) {
        this(packOutput, PackOutput.Target.RESOURCE_PACK, SkyAesthetics.MODID);
    }

    public SkyPropertiesProvider(PackOutput packOutput, String modid) {
        this(packOutput, PackOutput.Target.RESOURCE_PACK, modid);
    }

    public SkyPropertiesProvider(PackOutput packOutput, PackOutput.Target target, String modid) {
        this.modid = modid;
        this.pathProvider = packOutput.createPathProvider(target, registry.location().getPath());
        this.codec = SkyProperties.CODEC;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        build((key, value) ->
                futures.add(DataProvider.saveStable(
                        output,
                        codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow(),
                        pathProvider.json(key)
                ))
        );

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "Sky Provider";
    }

    protected void build(BiConsumer<ResourceLocation, SkyProperties> consumer) {
        consumer.accept(skyLocation("test"),
                new SkyProperties(
                        ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("overworld")),
                        skyLocation("test"),
                        Optional.of(CloudSettings.createDefaultSettings()),
                        Optional.of(FogSettings.createDefaultSettings()),
                        true,
                        Optional.of(CustomVanillaObject.Sun.createDefaultSun()),
                        Optional.of(CustomVanillaObject.Moon.createDefaultMoon()),
                        StarSettings.createDefaultStars(),
                        Optional.of(SkyColorSettings.createDefaultSettings()),
                        List.of(),
                        Optional.empty()
                )
        );
    }

    public ResourceLocation skyLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(SkyAesthetics.MODID, path);
    }

}
