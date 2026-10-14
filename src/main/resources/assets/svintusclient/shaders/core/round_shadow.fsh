#version 150

in vec4 vColor;
in vec2 vLocal;
in vec2 vSize;
in float vRadius;
in float vParam;

out vec4 fragColor;

float rrect(vec2 p, vec2 hs, float r) {
    vec2 q = abs(p) - hs + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 hs = vSize * 0.5;
    vec2 p = vLocal - hs;
    float r = min(vRadius, min(hs.x, hs.y));
    float soft = max(vParam, 0.0001);
    float d = rrect(p, hs, r);
    float mask = 1.0 - smoothstep(0.0, soft, d);
    if (mask <= 0.001) discard;
    fragColor = vec4(vColor.rgb, vColor.a * mask);
}
