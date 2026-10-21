package fr.tathan.sky_aesthetics.client;

import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.SkyRenderState;

/**
 * The class containing the time and weather used by the skybox layers, captured every frame
 * @param sunAngle The vanilla sun angle in radians
 * @param moonAngle The vanilla moon angle in radians
 * @param starAngle The vanilla star angle in radians
 * @param starBrightness The vanilla star brightness
 * @param dayTime The time of the day in ticks (0-24000), the same as /time set
 * @param gameTime The game time in ticks
 * @param rainLevel The rain level (0-1)
 * @param thunderLevel The thunder level (0-1)
 * @param moonPhase The moon phase (0 is the full moon)
 * @param skyColor The vanilla sky color
 * @param playerY The height of the player
 */
public record SkyClock(float sunAngle, float moonAngle, float starAngle, float starBrightness, double dayTime, double gameTime, float rainLevel, float thunderLevel, int moonPhase, int skyColor, double playerY) {

    public static final double DAY_LENGTH = 24000.0;

    public static SkyClock capture(SkyRenderState skyRenderState) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        double gameTime = 0.0;
        double dayTime = 0.0;
        float rainLevel = 0.0f;
        float thunderLevel = 0.0f;
        if (level != null) {
            gameTime = level.getGameTime() + partialTick;
            dayTime = Math.floorMod(level.getDefaultClockTime(), (long) DAY_LENGTH) + partialTick;
            rainLevel = level.getRainLevel(partialTick);
            thunderLevel = level.getThunderLevel(partialTick);
        }

        SkiesRegistry.PreviewOverrides overrides = SkiesRegistry.getPreviewOverrides();
        if (overrides.dayTime() >= 0) {
            dayTime = overrides.dayTime();
        }
        if (overrides.weather() != SkiesRegistry.PreviewWeather.REAL) {
            rainLevel = overrides.weather() == SkiesRegistry.PreviewWeather.CLEAR ? 0.0f : 1.0f;
            thunderLevel = overrides.weather() == SkiesRegistry.PreviewWeather.THUNDER ? 1.0f : 0.0f;
        }

        int moonPhase = skyRenderState.moonPhase != null ? skyRenderState.moonPhase.index() : 0;
        double playerY = player != null ? player.getY() : 0.0;

        return new SkyClock(skyRenderState.sunAngle, skyRenderState.moonAngle, skyRenderState.starAngle, skyRenderState.starBrightness, dayTime % DAY_LENGTH, gameTime, rainLevel, thunderLevel, moonPhase, skyRenderState.skyColor, playerY);
    }
}
