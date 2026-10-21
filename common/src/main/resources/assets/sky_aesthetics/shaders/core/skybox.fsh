#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec3 localPosition;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * ColorModulator;

    // The layer settings are in the third column of TextureMat, which the UVs don't use
    // x = blend mode (0 normal, 1 multiply, 2 screen, 3 add), y = upper hemisphere, z = horizon fade, w = fog fade
    vec4 settings = TextureMat[2];
    int blendMode = int(settings.x + 0.5);
    // ModelOffset is the world up seen from the layer, so the horizon doesn't move when the layer turns
    float elevation = dot(normalize(localPosition), ModelOffset);

    if (settings.y > 0.5) {
        color.a *= settings.z > 0.0 ? smoothstep(0.0, settings.z, elevation) : step(0.0, elevation);
    }
    if (color.a <= 0.0) {
        discard;
    }

    if (settings.w > 0.5) {
        // Same fog as the vanilla sky disc, which is at y = 16
        float discDistance = elevation > 0.0 ? min(16.0 / elevation, 512.0) : 512.0;
        float fogValue = linear_fog_value(discDistance, 0.0, FogSkyEnd) * FogColor.a;
        if (blendMode == 0) {
            color.rgb = mix(color.rgb, FogColor.rgb, fogValue);
        } else {
            // Multiply, screen and add would tint the sky with the fog color, so they fade out instead
            color.a *= 1.0 - fogValue;
        }
    }

    if (blendMode == 2) {
        color.rgb *= color.a;
    } else if (blendMode == 1) {
        color.rgb = mix(vec3(1.0), color.rgb, color.a);
    }

    fragColor = color;
}
