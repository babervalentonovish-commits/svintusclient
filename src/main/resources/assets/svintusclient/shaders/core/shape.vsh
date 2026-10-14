#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vColor;
out vec2 vLocal;
out vec2 vSize;
out float vRadius;
out float vParam;

void main() {
    vColor = Color;
    vLocal = UV0;
    vSize = vec2(UV1) / 8.0;
    vRadius = float(UV2.x) / 8.0;
    vParam = float(UV2.y) / 8.0;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
