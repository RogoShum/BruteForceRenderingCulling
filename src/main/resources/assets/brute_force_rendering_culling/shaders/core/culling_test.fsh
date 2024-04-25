#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;

uniform vec2 CullingSize;
uniform vec2 ScreenSize;
uniform mat4 CullingViewMat;
uniform mat4 CullingProjMat;
uniform vec3 CullingCameraPos;
uniform vec3 CullingCameraDir;
uniform float CullingFov;
uniform vec3 FrustumPos;
uniform float RenderDistance;
uniform int LevelHeightOffset;
uniform int LevelMinSection;

flat in int spacePartitionSize;
flat in vec4[6] frustum;
flat in vec2[4] DepthScreenSize;

out vec4 fragColor;

float near = 0.1;
float far  = 1000.0;

int getSampler(float xLength, float yLength) {
    for (int i = 0; i < DepthScreenSize.length(); ++i) {
        float xStep = 3.0 / DepthScreenSize[i].x;
        float yStep = 3.0 / DepthScreenSize[i].y;
        if (xStep > xLength && yStep > yLength) {
            return i;
        }
    }

    return DepthScreenSize.length() - 1;
}

float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

float calculateDistance(vec3 P, vec3 Q) {
    return pow(Q.x - P.x, 2) + pow(Q.y - P.y, 2) + pow(Q.z - P.z, 2);
}

vec3 worldToScreenSpace(vec3 pos) {
    vec4 cameraSpace = CullingProjMat * CullingViewMat * vec4(pos, 1);
    vec3 ndc = cameraSpace.xyz/cameraSpace.w;
    return (ndc + vec3(1.0)) * 0.5;
}

vec3 moveTowardsCamera(vec3 pos, float distance) {
    vec3 direction = normalize(pos - CullingCameraPos);
    vec3 newPos = pos - direction * distance;

    return newPos;
}

vec3 blockToChunk(vec3 blockPos) {
    vec3 chunkPos;
    chunkPos.x = floor(blockPos.x / 16.0);
    chunkPos.y = floor(blockPos.y / 16.0);
    chunkPos.z = floor(blockPos.z / 16.0);
    return chunkPos;
}

bool cubeInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    for (int i = 0; i < 6; ++i) {
        vec4 plane = frustum[i];
        if (!(dot(plane, vec4(minX, minY, minZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(maxX, minY, minZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(minX, maxY, minZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(maxX, maxY, minZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(minX, minY, maxZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(maxX, minY, maxZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(minX, maxY, maxZ, 1.0)) > 0.0) &&
        !(dot(plane, vec4(maxX, maxY, maxZ, 1.0)) > 0.0)) {
            return false;
        }
    }
    return true;
}

bool calculateCube(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    float f = minX - FrustumPos.x;
    float f1 = minY - FrustumPos.y;
    float f2 = minZ - FrustumPos.z;
    float f3 = maxX - FrustumPos.x;
    float f4 = maxY - FrustumPos.y;
    float f5 = maxZ - FrustumPos.z;
    return cubeInFrustum(f, f1, f2, f3, f4, f5);
}

bool isVisible(vec3 vec) {
    float minX, minY, minZ, maxX, maxY, maxZ;
    minX = vec.x - 8;
    minY = vec.y - 8;
    minZ = vec.z - 8;

    maxX = vec.x + 8;
    maxY = vec.y + 8;
    maxZ = vec.z + 8;
    return calculateCube(minX, minY, minZ, maxX, maxY, maxZ);
}

float getUVDepth(int idx, vec2 uv) {
    if (idx == 0)
    return texture(Sampler0, uv).r * 500;
    else if (idx == 1)
    return texture(Sampler1, uv).r * 500;
    else if (idx == 2)
    return texture(Sampler2, uv).r * 500;

    return texture(Sampler3, uv).r * 500;
}

void main() {
    vec2 screenUV = gl_FragCoord.xy / ScreenSize;
    fragColor = vec4(screenUV.x, screenUV.y, (screenUV.x+screenUV.y)*0.5, 1.0);
    return;
    vec3 chunkBasePos = vec3(0, 8, 0);
    vec3 chunkPos = vec3(0, 8, 0)*16;
    chunkPos = vec3(chunkPos.x, chunkPos.y, chunkPos.z)+vec3(8.0);

    float chunkCenterDepth = worldToScreenSpace(chunkPos).z;

    vec3 aabb[8] = vec3[](
    chunkPos+vec3(-8.0, -8.0, -8.0), chunkPos+vec3(8.0, -8.0, -8.0),
    chunkPos+vec3(-8.0, 8.0, -8.0), chunkPos+vec3(8.0, 8.0, -8.0),
    chunkPos+vec3(-8.0, -8.0, 8.0), chunkPos+vec3(8.0, -8.0, 8.0),
    chunkPos+vec3(-8.0, 8.0, 8.0), chunkPos+vec3(8.0, 8.0, 8.0)
    );

    float maxX = -1;
    float maxY = -1;
    float minX = 1;
    float minY = 1;

    for (int i = 0; i < 8; ++i) {
        vec3 screenPos = worldToScreenSpace(aabb[i]);

        if (screenPos.x > maxX)
        maxX = screenPos.x;
        if (screenPos.y > maxY)
        maxY = screenPos.y;
        if (screenPos.x < minX)
        minX = screenPos.x;
        if (screenPos.y < minY)
        minY = screenPos.y;
    }

    float chunkDepth = LinearizeDepth(chunkCenterDepth);
    if (minX < screenUV.x && maxX > screenUV.x) {
        if (minY < screenUV.y && maxY > screenUV.y) {
            fragColor = vec4(0.0, 1.0, 0.0, 1.0);
            return;
        }
    }


    fragColor = vec4(screenUV.x, screenUV.y, (screenUV.x+screenUV.y)*0.5, 1.0);
}
