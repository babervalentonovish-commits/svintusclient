#version 150

in vec4 vColor;
in vec2 vLocal;
in vec2 vSize;
in float vRadius;
in float vParam;

uniform sampler2D Sampler0;
out vec4 fragColor;

float rrect(vec2 p, vec2 hs, float r) {
    vec2 q = abs(p) - hs + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 hs = vSize * 0.5;
    vec2 p = vLocal - hs;
    float r = min(vRadius, min(hs.x, hs.y));
    float d = rrect(p, hs, r);
    float aa = max(fwidth(d), 0.0001);
    float mask = 1.0 - smoothstep(-aa, aa, d);
    if (mask <= 0.001) discard;
    vec2 uv = vLocal / vSize;
    vec4 s = texture(Sampler0, uv);
    vec3 rgb = mix(s.rgb, vColor.rgb, vColor.a);
    fragColor = vec4(rgb, mask);
}
