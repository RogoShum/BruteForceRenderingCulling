#version 150

uniform sampler2D Sampler7;

uniform float RenderDistance;
uniform vec2 ScreenSize;

out vec4 fragColor;

void main() {
    vec2 screenUV = vec2(gl_FragCoord.x / ScreenSize.x, gl_FragCoord.y / ScreenSize.y);

    float cellSizeX = 1.0 / float(RenderDistance);
    float cellSizeY = 1.0 / float(RenderDistance);

    if (mod(screenUV.x, cellSizeX) < 0.001 || mod(screenUV.y, cellSizeY) < 0.001)
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    else
        fragColor = vec4(1.0, 1.0, 1.0, 0.0);
}
