#version 330

layout (location=0) in vec2 Position;

layout (location=1) in float index;
layout (location=2) in vec2 EntitySize;
layout (location=3) in vec3 EntityCenter;

uniform vec2 EntityCullingSize;
uniform float[24] CullingFrustum;
uniform float[10] DepthSize;

flat out vec4[6] frustum;
flat out vec2[5] DepthScreenSize;
flat out vec3 Pos;
flat out vec2 Size;

void main() {
    int xSize = 8;
    vec2 pixelPos = vec2(Position.x / xSize * 0.5, Position.y / EntityCullingSize.y * 0.5);
    int y = int(index / xSize);
    int x = int(index - (y*xSize));
    vec2 pos = vec2((2.0 * (float(x)+0.5) / xSize) - 1.0, (2.0 * (float(y)+0.5) / EntityCullingSize.y) - 1.0);

    vec4[6] frustumData = vec4[](
    vec4(CullingFrustum[0], CullingFrustum[1], CullingFrustum[2], CullingFrustum[3]),
    vec4(CullingFrustum[4], CullingFrustum[5], CullingFrustum[6], CullingFrustum[7]),
    vec4(CullingFrustum[8], CullingFrustum[9], CullingFrustum[10], CullingFrustum[11]),
    vec4(CullingFrustum[12], CullingFrustum[13], CullingFrustum[14], CullingFrustum[15]),
    vec4(CullingFrustum[16], CullingFrustum[17], CullingFrustum[18], CullingFrustum[19]),
    vec4(CullingFrustum[20], CullingFrustum[21], CullingFrustum[22], CullingFrustum[23])
    );

    DepthScreenSize = vec2[](
    vec2(DepthSize[0], DepthSize[1]),
    vec2(DepthSize[2], DepthSize[3]),
    vec2(DepthSize[4], DepthSize[5]),
    vec2(DepthSize[6], DepthSize[7]),
    vec2(DepthSize[8], DepthSize[9])
    );

    frustum = frustumData;
    gl_Position = vec4(pixelPos.x + pos.x, pixelPos.y + pos.y, 0.0, 1.0);
    Pos = EntityCenter;
    Size = EntitySize;
}
