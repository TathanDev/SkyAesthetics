#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;

out vec2 texCoord0;
out vec3 localPosition;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // TextureMat has the scroll and the animation frame of the layer
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
    localPosition = Position;
}
