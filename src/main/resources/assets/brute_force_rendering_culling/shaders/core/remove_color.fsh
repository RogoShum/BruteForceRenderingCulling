#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    vec3 removeColor = vec3((color.x + color.y + color.z)/3);
    removeColor = (vertexColor.a * color.rgb) + ((1.0-vertexColor.a) * removeColor.rgb);
    color.rgb = vec3((1.0 - removeColor + ColorModulator.a)*(1-ColorModulator.a));
    fragColor = color;
}
