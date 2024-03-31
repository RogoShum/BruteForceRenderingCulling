#version 150

in vec3 Position;

uniform vec2 ScreenSize;

flat out float xStep;
flat out float yStep;

void main() {
    xStep = 1.0/ScreenSize.x;
    yStep = 1.0/ScreenSize.y;
    gl_Position = vec4(Position, 1.0);
}
