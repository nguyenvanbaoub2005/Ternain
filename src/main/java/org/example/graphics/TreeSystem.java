package org.example.graphics;

import org.example.terrain.Terrain;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Hệ thống tạo và render cây cối procedural
 */
public class TreeSystem {
    private int vaoId;
    private int vertexVboId;
    private int colorVboId;
    private int indicesVboId;
    private int vertexCount;
    
    private List<TreeInstance> trees;
    
    /**
     * Thông tin một cây
     */
    private static class TreeInstance {
        Vector3f position;
        float height;
        float width;
        
        TreeInstance(Vector3f position, float height, float width) {
            this.position = position;
            this.height = height;
            this.width = width;
        }
    }
    
    public TreeSystem() {
        trees = new ArrayList<>();
    }
    
    /**
     * Sinh cây ngẫu nhiên trên địa hình (sử dụng độ cao thực tế từ terrain)
     */
    public void generateTrees(int count, float minX, float maxX, float minZ, float maxZ, 
                             float waterHeight, Terrain terrain, Random random) {
        trees.clear();
        
        int attempts = 0;
        int maxAttempts = count * 3; // Cho phép thử nhiều lần để tìm vị trí phù hợp
        
        while (trees.size() < count && attempts < maxAttempts) {
            float x = minX + random.nextFloat() * (maxX - minX);
            float z = minZ + random.nextFloat() * (maxZ - minZ);
            
            // Lấy độ cao thực tế từ địa hình
            float terrainHeight = terrain.getHeightAt(x, z);
            
            // Chỉ sinh cây trên đất khô (cao hơn mực nước + buffer)
            // và không quá cao (tránh vùng tuyết)
            if (terrainHeight > waterHeight + 0.5f && terrainHeight < 20.0f) {
                float treeHeight = 3.0f + random.nextFloat() * 4.0f;
                float treeWidth = 0.3f + random.nextFloat() * 0.4f;
                
                // Đặt cây đúng trên bề mặt địa hình
                trees.add(new TreeInstance(new Vector3f(x, terrainHeight, z), treeHeight, treeWidth));
            }
            
            attempts++;
        }
        
        System.out.println("→ Đã sinh " + trees.size() + " cây trên địa hình (đất khô)");
        
        generateMesh();
    }
    
    /**
     * Tạo mesh cho tất cả cây
     */
    private void generateMesh() {
        List<Float> vertices = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        
        int indexOffset = 0;
        
        for (TreeInstance tree : trees) {
            // Tạo thân cây (cylinder đơn giản)
            addTrunk(vertices, colors, indices, tree, indexOffset);
            indexOffset += 8; // 8 vertices cho thân cây
            
            // Tạo lá cây (cone đơn giản)
            addLeaves(vertices, colors, indices, tree, indexOffset);
            indexOffset += 5; // 5 vertices cho lá
        }
        
        // Chuyển List sang array
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }
        
        float[] colorArray = new float[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            colorArray[i] = colors.get(i);
        }
        
        int[] indexArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }
        
        vertexCount = indexArray.length;
        
        // Upload lên GPU
        uploadToGPU(vertexArray, colorArray, indexArray);
    }
    
    /**
     * Thêm thân cây (cylinder)
     */
    private void addTrunk(List<Float> vertices, List<Float> colors, List<Integer> indices, 
                         TreeInstance tree, int offset) {
        float x = tree.position.x;
        float y = tree.position.y;
        float z = tree.position.z;
        float w = tree.width;
        float h = tree.height * 0.6f; // Thân chiếm 60% chiều cao
        
        // Màu nâu cho thân
        float r = 0.4f, g = 0.25f, b = 0.1f;
        
        // 8 vertices cho cylinder đơn giản (4 dưới, 4 trên)
        // Dưới
        vertices.add(x - w); vertices.add(y); vertices.add(z - w);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x + w); vertices.add(y); vertices.add(z - w);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x + w); vertices.add(y); vertices.add(z + w);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x - w); vertices.add(y); vertices.add(z + w);
        colors.add(r); colors.add(g); colors.add(b);
        
        // Trên
        vertices.add(x - w * 0.8f); vertices.add(y + h); vertices.add(z - w * 0.8f);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x + w * 0.8f); vertices.add(y + h); vertices.add(z - w * 0.8f);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x + w * 0.8f); vertices.add(y + h); vertices.add(z + w * 0.8f);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x - w * 0.8f); vertices.add(y + h); vertices.add(z + w * 0.8f);
        colors.add(r); colors.add(g); colors.add(b);
        
        // Indices cho 4 mặt
        // Mặt 1
        indices.add(offset + 0); indices.add(offset + 1); indices.add(offset + 5);
        indices.add(offset + 5); indices.add(offset + 4); indices.add(offset + 0);
        
        // Mặt 2
        indices.add(offset + 1); indices.add(offset + 2); indices.add(offset + 6);
        indices.add(offset + 6); indices.add(offset + 5); indices.add(offset + 1);
        
        // Mặt 3
        indices.add(offset + 2); indices.add(offset + 3); indices.add(offset + 7);
        indices.add(offset + 7); indices.add(offset + 6); indices.add(offset + 2);
        
        // Mặt 4
        indices.add(offset + 3); indices.add(offset + 0); indices.add(offset + 4);
        indices.add(offset + 4); indices.add(offset + 7); indices.add(offset + 3);
    }
    
    /**
     * Thêm lá cây (cone)
     */
    private void addLeaves(List<Float> vertices, List<Float> colors, List<Integer> indices,
                          TreeInstance tree, int offset) {
        float x = tree.position.x;
        float y = tree.position.y + tree.height * 0.5f;
        float z = tree.position.z;
        float w = tree.width * 2.5f;
        float h = tree.height * 0.5f;
        
        // Màu xanh lá
        float r = 0.1f, g = 0.6f, b = 0.1f;
        
        // Đỉnh (1 vertex)
        vertices.add(x); vertices.add(y + h); vertices.add(z);
        colors.add(r * 0.8f); colors.add(g * 0.8f); colors.add(b * 0.8f);
        
        // Đáy (4 vertices)
        vertices.add(x - w); vertices.add(y); vertices.add(z - w);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x + w); vertices.add(y); vertices.add(z - w);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x + w); vertices.add(y); vertices.add(z + w);
        colors.add(r); colors.add(g); colors.add(b);
        
        vertices.add(x - w); vertices.add(y); vertices.add(z + w);
        colors.add(r); colors.add(g); colors.add(b);
        
        // Indices cho 4 mặt tam giác
        indices.add(offset + 0); indices.add(offset + 1); indices.add(offset + 2);
        indices.add(offset + 0); indices.add(offset + 2); indices.add(offset + 3);
        indices.add(offset + 0); indices.add(offset + 3); indices.add(offset + 4);
        indices.add(offset + 0); indices.add(offset + 4); indices.add(offset + 1);
    }
    
    private void uploadToGPU(float[] vertices, float[] colors, int[] indices) {
        // Tạo VAO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        // Upload vertices
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();
        vertexVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(verticesBuffer);
        
        // Upload colors
        FloatBuffer colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
        colorsBuffer.put(colors).flip();
        colorVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
        glBufferData(GL_ARRAY_BUFFER, colorsBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(colorsBuffer);
        
        // Upload indices
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        indicesVboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void render() {
        if (vertexCount == 0) return;
        
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }
    
    public void cleanup() {
        if (vaoId != 0) {
            glDeleteBuffers(vertexVboId);
            glDeleteBuffers(colorVboId);
            glDeleteBuffers(indicesVboId);
            glDeleteVertexArrays(vaoId);
        }
    }
    
    public int getTreeCount() {
        return trees.size();
    }
}
