#version 330 core

// Input
in vec3 FragColor;
in vec3 FragPos;

// Output
out vec4 Color;

// Uniforms
uniform vec3 viewPos;
uniform vec3 lightPos;
uniform vec3 fogColor;
uniform float fogDensity;
uniform float fogGradient;

void main() {
    // Ánh sáng đơn giản
    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 norm = vec3(0.0, 1.0, 0.0); // Normal đơn giản
    
    float ambient = 0.4;
    float diffuse = max(dot(norm, lightDir), 0.0) * 0.6;
    
    vec3 result = (ambient + diffuse) * FragColor;
    
    // Áp dụng fog
    float distance = length(viewPos - FragPos);
    float fogAmount = 1.0 - exp(-pow(distance * fogDensity, fogGradient));
    fogAmount = clamp(fogAmount, 0.0, 1.0);
    
    result = mix(result, fogColor, fogAmount);
    
    Color = vec4(result, 1.0);
}
