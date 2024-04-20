#version 150

uniform vec2 EntityCullingSize;
uniform mat4 CullingViewMat;
uniform vec3 CullingCameraPos;
uniform mat4 CullingProjMat;
uniform float DepthOffset;
uniform vec3 FrustumPos;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;

flat in vec3 Pos;
flat in vec2 Size;
flat in vec4[6] frustum;
flat in vec2[4] DepthScreenSize;

out vec4 fragColor;

float near = 0.1;
float far  = 1000.0;

int getSampler(float xLength, float yLength) {
    for(int i = 0; i < DepthScreenSize.length(); ++i) {
        float xStep = 3.0 / DepthScreenSize[i].x;
        float yStep = 3.0 / DepthScreenSize[i].y;
        if(xStep > xLength && yStep > yLength) {
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

float getUVDepth(int idx, vec2 uv) {
    if(idx == 0)
        return texture(Sampler0, uv).r * 500;
    else if(idx == 1)
        return texture(Sampler1, uv).r * 500;
    else if(idx == 2)
        return texture(Sampler2, uv).r * 500;

    return texture(Sampler3, uv).r * 500;
}

void main() {
    float halfWidth = Size.x*0.5;
    float halfHeight = Size.y*0.5;

    float entityDepth = worldToScreenSpace(moveTowardsCamera(Pos, sqrt(halfWidth*halfWidth+halfWidth*halfWidth))).z;

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

    bool inside = false;

    for (int i = 0; i < 8; ++i) {
        vec3 screenPos = worldToScreenSpace(aabb[i]);
        bool xIn = screenPos.x >= 0.0 && screenPos.x <= 1.0;
        bool yIn = screenPos.y >= 0.0 && screenPos.y <= 1.0;
        bool zIn = screenPos.z >= 0.0 && screenPos.z <= 1.0;

        if (xIn && yIn && zIn) {
            inside = true;
        }

        if(screenPos.x > maxX)
        maxX = screenPos.x;
        if(screenPos.y > maxY)
        maxY = screenPos.y;
        if(screenPos.x < minX)
        minX = screenPos.x;
        if(screenPos.y < minY)
        minY = screenPos.y;
    }

    if (!inside) {
        fragColor = vec4(0.0, 0.0, 1.0, 1.0);
        return;
    }

    entityDepth = LinearizeDepth(entityDepth);

    if(entityDepth < 0) {
        fragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    }

    int idx = getSampler(
    min(1.0, max(0.0, maxX))-min(1.0, max(0.0, minX)),
    min(1.0, max(0.0, maxY))-min(1.0, max(0.0, minY)));

    float xStep = 1.0/DepthScreenSize[idx].x;
    float yStep = 1.0/DepthScreenSize[idx].y;

    minX = min(1.0, max(0.0, minX-xStep));
    maxX = min(1.0, max(0.0, maxX+xStep));
    maxY = min(1.0, max(0.0, maxY+yStep));
    minY = min(1.0, max(0.0, minY-yStep));

    for(float x = minX; x <= maxX; x += xStep) {
        for(float y = minY; y <= maxY; y += yStep) {
            float pixelDepth = getUVDepth(idx, vec2(x, y));
            if(entityDepth < pixelDepth) {
                fragColor = vec4(0.0, 1.0, 0.0, 1.0);
                return;
            }
        }
    }

    fragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
