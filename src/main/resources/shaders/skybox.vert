#version 330 core

// Input: Vị trí vertex của cube
layout (location = 0) in vec3 aPos;

// Output: Tọa độ texture (sử dụng vị trí làm direction vector)
out vec3 TexCoords;

// Ma trận view và projection (không có model vì skybox không di chuyển)
uniform mat4 view;
uniform mat4 projection;

void main()
{
    TexCoords = aPos;
    
    // Loại bỏ phần translation của view matrix (chỉ giữ rotation)
    // Để skybox luôn ở "xa vô tận"
    mat4 viewNoTranslation = mat4(mat3(view));
    
    vec4 pos = projection * viewNoTranslation * vec4(aPos, 1.0);
    
    // Đặt z = w để skybox luôn ở depth = 1.0 (xa nhất)
    gl_Position = pos.xyww;
}
