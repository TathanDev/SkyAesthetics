package fr.tathan.sky_aesthetics.config;


import fr.tathan.exoconfig.common.infos.ConfigInfos;
import fr.tathan.exoconfig.common.infos.ScreenInfos;
import fr.tathan.exoconfig.common.utils.Side;

@ConfigInfos(modDisplayName = "Sky Aesthetics", name = "sky_aesthetics", side = Side.CLIENT)
public class SkyConfig {

    public String[] disabledSkies = new String[]{
            "bad_sky:sky_to_disable"
    };

    @ScreenInfos.Hidden
    public String[] disabledDimensions = new String[]{
            "bad_mod:bad_dimension"
    };

    public boolean disableCustomCloud = false;

    public boolean disableCustomWeather = false;

    public String[] modDisablingCloudRender = new String[]{
            "distanthorizons",
    };

    public String[] modDisablingWeather = new String[]{
            "bad_weather_mod",
    };
}