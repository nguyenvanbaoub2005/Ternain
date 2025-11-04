package org.example.engine;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {
    private final int id;
    private int width;
    private int height;

    public Texture(String fileName) throws Exception {
        this.id = loadTexture(fileName);
    }

    // Protected constructor for procedural textures
    protected Texture(int id, int width, int height) throws Exception {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    private int loadTexture(String fileName) throws Exception {
        int textureId;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Load image
            ByteBuffer buf = STBImage.stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [" + fileName + "] not loaded: " + STBImage.stbi_failure_reason());
            }

            width = w.get();
            height = h.get();

            // Create texture
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glGenerateMipmap(GL_TEXTURE_2D);

            STBImage.stbi_image_free(buf);
        }

        return textureId;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void bind(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
