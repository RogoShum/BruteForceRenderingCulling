#version 150

in vec3 Position;

uniform vec2 ScreenSize;
uniform float[10] DepthSize;

flat out float xStep;
flat out float yStep;
flat out vec2 DepthScreenSize;

void main() {
    xStep = 1.0/ScreenSize.x;
    yStep = 1.0/ScreenSize.y;
    DepthScreenSize = vec2(DepthSize[0], DepthSize[1]);
    gl_Position = vec4(Position, 1.0);
}
