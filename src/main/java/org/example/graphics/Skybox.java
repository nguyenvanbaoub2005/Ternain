package org.example.graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Skybox - Bầu trời 360° bao quanh toàn bộ cảnh
 */
public class Skybox {
    private int vaoId;
    private int vboId;
    private int textureId;
    
    // Vertices của cube (chỉ cần vị trí, không cần normal/texcoord)
    private static final float[] SKYBOX_VERTICES = {
        // Positions          
        -1.0f,  1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
         1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,

        -1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

         1.0f, -1.0f, -1.0f,
         1.0f, -1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,

        -1.0f, -1.0f,  1.0f,
        -1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

        -1.0f,  1.0f, -1.0f,
         1.0f,  1.0f, -1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
        -1.0f,  1.0f,  1.0f,
        -1.0f,  1.0f, -1.0f,

        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f,  1.0f,
         1.0f, -1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f,  1.0f,
         1.0f, -1.0f,  1.0f
    };

    public Skybox() {
        setupMesh();
        textureId = createProceduralSkyboxTexture();
    }

    private void setupMesh() {
        // Tạo VAO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Upload vertices
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(SKYBOX_VERTICES.length);
        verticesBuffer.put(SKYBOX_VERTICES).flip();
        
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        
        MemoryUtil.memFree(verticesBuffer);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    /**
     * Tạo cubemap texture procedurally (gradient trời -> chân trời)
     */
    private int createProceduralSkyboxTexture() {
        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, texId);

        int size = 512;
        
        // 6 mặt của cube: +X, -X, +Y, -Y, +Z, -Z
        for (int face = 0; face < 6; face++) {
            FloatBuffer data = MemoryUtil.memAllocFloat(size * size * 3);
            
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    float u = (float) x / size;
                    float v = (float) y / size;
                    
                    // Gradient từ trời xanh (trên) -> chân trời trắng (giữa) -> xanh đậm (dưới)
                    float r, g, b;
                    
                    if (face == 2) { // +Y (trên đầu - trời)
                        r = 0.3f + v * 0.2f;
                        g = 0.5f + v * 0.3f;
                        b = 0.9f;
                    } else if (face == 3) { // -Y (dưới chân - tối)
                        r = 0.2f;
                        g = 0.3f;
                        b = 0.5f;
                    } else { // Các mặt bên (chân trời)
                        float height = 1.0f - v; // v=0 ở trên, v=1 ở dưới
                        
                        if (height > 0.5f) {
                            // Phần trên: Xanh trời
                            float t = (height - 0.5f) * 2.0f;
                            r = 0.3f + t * 0.2f;
                            g = 0.5f + t * 0.3f;
                            b = 0.9f;
                        } else {
                            // Phần dưới: Chân trời sáng
                            float t = height * 2.0f;
                            r = 0.9f * t + 0.7f * (1 - t);
                            g = 0.8f * t + 0.6f * (1 - t);
                            b = 0.7f * t + 0.5f * (1 - t);
                        }
                    }
                    
                    data.put(r);
                    data.put(g);
                    data.put(b);
                }
            }
            
            data.flip();
            
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL_RGB, 
                        size, size, 0, GL_RGB, GL_FLOAT, data);
            
            MemoryUtil.memFree(data);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        return texId;
    }

    public void render() {
        glDepthFunc(GL_LEQUAL); // Thay đổi depth function để skybox ở xa nhất
        glBindVertexArray(vaoId);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
        glDepthFunc(GL_LESS); // Reset về default
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteTextures(textureId);
    }

    public int getTextureId() {
        return textureId;
    }
}
