#version 150

uniform sampler2D Sampler7;

uniform vec2 DepthSize;

out vec4 fragColor;

float near = 0.1;
float far  = 1000.0;

float LinearizeDepth(float depth) {
    return depth*500;
}

void main() {
    vec2 screenUV = vec2(gl_FragCoord.x / DepthSize.x, gl_FragCoord.y / DepthSize.y);
    float pixelDepth = LinearizeDepth(texture(Sampler7, screenUV).r);

    fragColor = vec4(vec3(pixelDepth/500.0), 1.0);
}
