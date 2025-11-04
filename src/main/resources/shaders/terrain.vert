#version 330 core

// Input từ vertex buffer
layout (location = 0) in vec3 position;  // Vị trí đỉnh (x, y, z)
layout (location = 1) in vec3 normal;    // Vector pháp tuyến
layout (location = 2) in vec2 texCoord;  // Tọa độ texture (u, v)

// Output cho fragment shader
out vec3 FragPos;    // Vị trí fragment trong world space
out vec3 Normal;     // Vector pháp tuyến đã biến đổi
out vec2 TexCoord;   // Tọa độ texture
out float Height;    // Độ cao (để blend texture)

// Ma trận biến đổi
uniform mat4 model;       // Model matrix (local -> world)
uniform mat4 view;        // View matrix (world -> camera)
uniform mat4 projection;  // Projection matrix (camera -> screen)

void main() {
    // Tính vị trí trong world space
    FragPos = vec3(model * vec4(position, 1.0));
    
    // Biến đổi normal vector (loại bỏ ảnh hưởng scale/rotation)
    Normal = mat3(transpose(inverse(model))) * normal;
    
    // Truyền texture coordinate
    TexCoord = texCoord;
    
    // Lưu độ cao để blend texture
    Height = position.y;
    
    // Tính vị trí cuối cùng trên màn hình
    gl_Position = projection * view * vec4(FragPos, 1.0);
}
