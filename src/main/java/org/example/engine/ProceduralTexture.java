package org.example.engine;

import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.*;

public class ProceduralTexture {
    
    public static Texture createGrassTexture(int size) throws Exception {
        ByteBuffer buffer = generateGrassTexture(size);
        int textureId = uploadTexture(buffer, size);
        memFree(buffer);
        return new TextureWrapper(textureId, size);
    }
    
    public static Texture createRockTexture(int size) throws Exception {
        ByteBuffer buffer = generateRockTexture(size);
        int textureId = uploadTexture(buffer, size);
        memFree(buffer);
        return new TextureWrapper(textureId, size);
    }
    
    public static Texture createSnowTexture(int size) throws Exception {
        ByteBuffer buffer = generateSnowTexture(size);
        int textureId = uploadTexture(buffer, size);
        memFree(buffer);
        return new TextureWrapper(textureId, size);
    }
    
    private static ByteBuffer generateGrassTexture(int size) {
        ByteBuffer buffer = memAlloc(size * size * 4);
        Random random = new Random(123);
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Base green color with variation
                float variation = random.nextFloat() * 0.3f;
                int r = (int) ((0.1f + variation * 0.3f) * 255);
                int g = (int) ((0.5f + variation) * 255);
                int b = (int) ((0.1f + variation * 0.2f) * 255);
                int a = 255;
                
                buffer.put((byte) r);
                buffer.put((byte) g);
                buffer.put((byte) b);
                buffer.put((byte) a);
            }
        }
        
        buffer.flip();
        return buffer;
    }
    
    private static ByteBuffer generateRockTexture(int size) {
        ByteBuffer buffer = memAlloc(size * size * 4);
        Random random = new Random(456);
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Gray color with variation
                float variation = random.nextFloat() * 0.4f;
                int gray = (int) ((0.3f + variation) * 255);
                int r = gray;
                int g = gray;
                int b = gray;
                int a = 255;
                
                buffer.put((byte) r);
                buffer.put((byte) g);
                buffer.put((byte) b);
                buffer.put((byte) a);
            }
        }
        
        buffer.flip();
        return buffer;
    }
    
    private static ByteBuffer generateSnowTexture(int size) {
        ByteBuffer buffer = memAlloc(size * size * 4);
        Random random = new Random(789);
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // White color with slight variation
                float variation = random.nextFloat() * 0.1f;
                int value = (int) ((0.9f + variation) * 255);
                int r = value;
                int g = value;
                int b = value;
                int a = 255;
                
                buffer.put((byte) r);
                buffer.put((byte) g);
                buffer.put((byte) b);
                buffer.put((byte) a);
            }
        }
        
        buffer.flip();
        return buffer;
    }
    
    private static int uploadTexture(ByteBuffer buffer, int size) {
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        
        return textureId;
    }
    
    // Wrapper class to create Texture-compatible object
    private static class TextureWrapper extends Texture {
        public TextureWrapper(int id, int size) throws Exception {
            super(id, size, size);
        }
    }
}
