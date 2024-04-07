#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;

uniform float[24] CullingFrustum;
uniform float[10] DepthSize;
uniform float RenderDistance;

flat out int spacePartitionSize;
flat out vec4[6] frustum;
flat out vec2[5] DepthScreenSize;

void main() {
    spacePartitionSize = 2 * int(RenderDistance) + 1;
    vec4[6] frustumData =  vec4[](
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
    gl_Position = vec4(Position, 1.0);
}
