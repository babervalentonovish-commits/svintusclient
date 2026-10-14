#version 150

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 t = 1.0 / vec2(textureSize(InSampler, 0));
    vec4 c = texture(InSampler, texCoord + t * vec2(-1.0, -1.0))
           + texture(InSampler, texCoord + t * vec2( 1.0, -1.0))
           + texture(InSampler, texCoord + t * vec2(-1.0,  1.0))
           + texture(InSampler, texCoord + t * vec2( 1.0,  1.0));
    fragColor = c * 0.25;
}
