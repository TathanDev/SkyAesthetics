package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record CloudSettings(boolean showCloud, Integer cloudHeight) {


    public static CloudSettings createDefaultSettings() {
        return new CloudSettings(true, 192);
    }

    public static final Codec<CloudSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("cloud").forGetter(CloudSettings::showCloud),
            Codec.INT.fieldOf("cloud_height").forGetter(CloudSettings::cloudHeight)
    ).apply(instance, CloudSettings::new));

}
