#version 150

uniform sampler2D Sampler7;

uniform vec2 EntityCullingSize;
uniform vec2 DepthSize;
uniform mat4 CullingViewMat;
uniform mat4 CullingProjMat;
uniform float DepthOffset;
uniform vec3 FrustumPos;

flat in vec3 Pos;
flat in vec2 Size;
flat in vec4[6] frustum;

out vec4 fragColor;

float near = 0.1;
float far  = 1000.0;

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

bool cubeInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    for(int i = 0; i < 6; ++i) {
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

bool isVisible(vec3 vec, float width, float height) {
    float minX, minY, minZ, maxX, maxY, maxZ;
    minX = vec.x - width;
    minY = vec.y - height;
    minZ = vec.z - width;

    maxX = vec.x + width;
    maxY = vec.y + height;
    maxZ = vec.z + width;
    return calculateCube(minX, minY, minZ, maxX, maxY, maxZ);
}

void main() {
    float halfWidth = Size.x*0.5;
    float halfHeight = Size.y*0.5;

    float entityDepth = worldToScreenSpace(Pos).z;

    if(!isVisible(Pos, halfWidth, halfHeight)) {
        fragColor = vec4(0.0, 0.0, 1.0, 1.0);
        return;
    }

    vec3 aabb[8] = vec3[](
        Pos+vec3(-halfWidth, -halfHeight, -halfWidth), Pos+vec3(halfWidth, -halfHeight, -halfWidth),
        Pos+vec3(-halfWidth, halfHeight, -halfWidth), Pos+vec3(halfWidth, halfHeight, -halfWidth),
        Pos+vec3(-halfWidth, -halfHeight, halfWidth), Pos+vec3(halfWidth, -halfHeight, halfWidth),
        Pos+vec3(-halfWidth, halfHeight, halfWidth), Pos+vec3(halfWidth, halfHeight, halfWidth)
    );

    float maxX = -1;
    float maxY = -1;
    float minX = 1;
    float minY = 1;

    for (int i = 0; i < 8; ++i) {
        vec3 screenPos = worldToScreenSpace(aabb[i]);
        bool xIn = screenPos.x >= 0.0 && screenPos.x <= 1.0;
        bool yIn = screenPos.y >= 0.0 && screenPos.y <= 1.0;
        bool zIn = screenPos.z >= 0.0 && screenPos.z <= 1.0;

        if(screenPos.x > maxX)
        maxX = screenPos.x;
        if(screenPos.y > maxY)
        maxY = screenPos.y;
        if(screenPos.x < minX)
        minX = screenPos.x;
        if(screenPos.y < minY)
        minY = screenPos.y;
    }

    entityDepth = LinearizeDepth(entityDepth)-sqrt(halfWidth*halfWidth+halfWidth*halfWidth)*1.2 - DepthOffset*0.25;

    if(entityDepth < 0) {
        fragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    }

    float xStep = 1.0/DepthSize.x;
    float yStep = 1.0/DepthSize.y;

    minX = min(1.0, max(0.0, minX-xStep));
    maxX = min(1.0, max(0.0, maxX+xStep));
    maxY = min(1.0, max(0.0, maxY+yStep));
    minY = min(1.0, max(0.0, minY-yStep));

    for(float x = minX; x <= maxX; x += xStep) {
        for(float y = minY; y <= maxY; y += yStep) {
            float pixelDepth = texture(Sampler7, vec2(x, y)).r * 500;
            if(entityDepth < pixelDepth) {
                fragColor = vec4(0.0, 1.0, 0.0, 1.0);
                return;
            }
        }
    }

    fragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
