#version 150

uniform sampler2D Sampler0;
uniform float RenderDistance;

flat in  vec2 DepthScreenSize;
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
    float minX = gl_FragCoord.x / DepthScreenSize.x;
    float minY = gl_FragCoord.y / DepthScreenSize.y;
    float maxX = min(gl_FragCoord.x+1, DepthScreenSize.x)/DepthScreenSize.x;
    float maxY = min(gl_FragCoord.y+1, DepthScreenSize.y)/DepthScreenSize.y;
    float depth;

    for(float x = minX-xStep; x <= maxX+xStep; x+=xStep) {
        for(float y = minY-xStep; y <= maxY+xStep; y+=yStep) {
            vec2 depthUV = vec2(min(x, 1.0), min(y, 1.0));
            depth = max(depth, texture(Sampler0, depthUV).r);
        }
    }

    if(RenderDistance > 1) {
        fragColor = vec4(vec3(depth), 1.0);
    } else {
        fragColor = vec4(vec3(LinearizeDepth(depth)/500.0), 1.0);
    }
}
