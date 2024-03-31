#version 150

uniform sampler2D Sampler0;

uniform vec2 DepthSize;

flat in float xStep;
flat in float yStep;

out vec4 fragColor;

float near = 0.1;
float far  = 1000.0;

float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    float minX = gl_FragCoord.x / DepthSize.x;
    float minY = gl_FragCoord.y / DepthSize.y;
    float maxX = min(gl_FragCoord.x+1, DepthSize.x)/DepthSize.x;
    float maxY = min(gl_FragCoord.y+1, DepthSize.y)/DepthSize.y;
    float depth;

    for(float x = minX; x <= maxX; x+=xStep) {
        for(float y = minY; y <= maxY; y+=yStep) {
            vec2 depthUV = vec2(min(x, 1.0), min(y, 1.0));
            depth = max(depth, texture(Sampler0, depthUV).r);
        }
    }

    fragColor = vec4(vec3(LinearizeDepth(depth)/500.0), 1.0);
}
