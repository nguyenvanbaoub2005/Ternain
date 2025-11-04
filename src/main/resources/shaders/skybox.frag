#version 330 core

// Input từ vertex shader
in vec3 TexCoords;

// Output màu
out vec4 FragColor;

// Cubemap texture
uniform samplerCube skybox;

void main()
{    
    // Sample màu từ cubemap sử dụng direction vector
    FragColor = texture(skybox, TexCoords);
}
