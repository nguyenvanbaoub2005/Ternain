#version 330 core

// Input từ vertex shader
in vec3 FragPos;   // Vị trí fragment
in vec3 Normal;    // Vector pháp tuyến
in vec2 TexCoord;  // Tọa độ texture
in float Height;   // Độ cao

// Output màu cuối cùng
out vec4 FragColor;

// Uniforms
uniform vec3 lightPos;    // Vị trí nguồn sáng
uniform vec3 viewPos;     // Vị trí camera
uniform vec3 lightColor;  // Màu ánh sáng

// Fog uniforms
uniform vec3 fogColor;    // Màu sương mù
uniform float fogDensity; // Độ dày sương mù
uniform float fogGradient; // Gradient của sương mù

// Texture samplers
uniform sampler2D grassTexture;  // Texture cỏ
uniform sampler2D rockTexture;   // Texture đá
uniform sampler2D snowTexture;   // Texture tuyết

// Hàm tính ánh sáng Blinn-Phong
vec3 calculateLighting(vec3 color) {
    // 1. Ambient (ánh sáng môi trường)
    float ambientStrength = 0.3;
    vec3 ambient = ambientStrength * lightColor;
    
    // 2. Diffuse (ánh sáng khuếch tán)
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;
    
    // 3. Specular (ánh sáng phản chiếu - Blinn-Phong)
    float specularStrength = 0.2;
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
    vec3 specular = specularStrength * spec * lightColor;
    
    // Tổng hợp ánh sáng
    return (ambient + diffuse + specular) * color;
}

void main() {
    // Lấy màu texture
    vec4 grassColor = texture(grassTexture, TexCoord * 10.0);
    vec4 rockColor = texture(rockTexture, TexCoord * 10.0);
    vec4 snowColor = texture(snowTexture, TexCoord * 10.0);
    
    vec4 finalColor;
    
    // Blend texture dựa theo độ cao
    if (Height < 5.0) {
        // Vùng thấp: Cỏ
        finalColor = grassColor;
    } else if (Height < 15.0) {
        // Vùng trung: Cỏ -> Đá (chuyển tiếp mượt)
        float blend = (Height - 5.0) / 10.0;
        finalColor = mix(grassColor, rockColor, blend);
    } else if (Height < 25.0) {
        // Vùng cao: Đá -> Tuyết (chuyển tiếp mượt)
        float blend = (Height - 15.0) / 10.0;
        finalColor = mix(rockColor, snowColor, blend);
    } else {
        // Vùng rất cao: Tuyết
        finalColor = snowColor;
    }
    
    // Blend thêm dựa theo độ dốc (vùng dốc = đá)
    float slope = 1.0 - abs(normalize(Normal).y);
    if (slope > 0.5 && Height > 5.0) {
        float rockBlend = (slope - 0.5) / 0.5;
        finalColor = mix(finalColor, rockColor, rockBlend * 0.7);
    }
    
    // Áp dụng ánh sáng
    vec3 result = calculateLighting(finalColor.rgb);
    
    // Tính sương mù dựa theo khoảng cách
    float distance = length(viewPos - FragPos);
    float fogAmount = 1.0 - exp(-pow(distance * fogDensity, fogGradient));
    fogAmount = clamp(fogAmount, 0.0, 1.0);
    
    // Trộn màu với sương mù
    result = mix(result, fogColor, fogAmount);
    
    FragColor = vec4(result, 1.0);
}
