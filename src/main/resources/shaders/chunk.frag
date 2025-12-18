#version 330 core
in vec3 vColor;
in vec3 vNormal;
out vec4 FragColor;

void main() {
    vec3 lightDir = normalize(vec3(-0.6, -1.0, -0.4));
    float diffuse = max(dot(normalize(vNormal), -lightDir), 0.0);
    float ambient = 0.35;
    vec3 litColor = vColor * (ambient + diffuse * 0.65);
    FragColor = vec4(litColor, 1.0);
}
