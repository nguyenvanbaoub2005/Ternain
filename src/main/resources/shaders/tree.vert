#version 330 core

// Input
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 color;

// Output
out vec3 FragColor;
out vec3 FragPos;

// Uniforms
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    FragPos = vec3(model * vec4(position, 1.0));
    FragColor = color;
    gl_Position = projection * view * vec4(FragPos, 1.0);
}
