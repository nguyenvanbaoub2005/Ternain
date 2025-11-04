package org.example.terrain;

import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Terrain {
    private final int gridSize;
    private final float scale;
    private final float heightScale;
    
    private int vaoId;
    private int vertexVboId;
    private int normalVboId;
    private int texCoordVboId;
    private int indicesVboId;
    private int vertexCount;
    
    private float[][] heights;

    public Terrain(int gridSize, float scale, float heightScale, PerlinNoise noise) {
        this.gridSize = gridSize;
        this.scale = scale;
        this.heightScale = heightScale;
        
        generateTerrain(noise);
    }

    private void generateTerrain(PerlinNoise noise) {
        // Sinh độ cao sử dụng Perlin Noise
        heights = new float[gridSize][gridSize];
        
        for (int z = 0; z < gridSize; z++) {
            for (int x = 0; x < gridSize; x++) {
                double nx = (double) x / gridSize;
                double nz = (double) z / gridSize;
                
                // Sử dụng fractal noise để tạo địa hình tự nhiên hơn
                double height = noise.fractalNoise(nx * 5, nz * 5, 6, 0.5);
                heights[z][x] = (float) height * heightScale;
            }
        }
        
        // Tạo dữ liệu mesh
        int verticesPerRow = gridSize;
        int totalVertices = verticesPerRow * verticesPerRow;
        
        float[] vertices = new float[totalVertices * 3];   // x, y, z
        float[] normals = new float[totalVertices * 3];    // nx, ny, nz
        float[] texCoords = new float[totalVertices * 2];  // u, v
        
        int vertexPointer = 0;
        int normalPointer = 0;
        int texPointer = 0;
        
        for (int z = 0; z < gridSize; z++) {
            for (int x = 0; x < gridSize; x++) {
                // Vị trí vertex
                vertices[vertexPointer++] = x * scale;
                vertices[vertexPointer++] = heights[z][x];
                vertices[vertexPointer++] = z * scale;
                
                // Tọa độ texture (UV mapping)
                texCoords[texPointer++] = (float) x / (gridSize - 1);
                texCoords[texPointer++] = (float) z / (gridSize - 1);
                
                // Normal vector (sẽ được tính sau)
                normalPointer += 3;
            }
        }
        
        // Tính normal vectors cho ánh sáng
        calculateNormals(normals, vertices);
        
        // Tạo indices cho tam giác (mỗi ô vuông = 2 tam giác)
        int indicesCount = (gridSize - 1) * (gridSize - 1) * 6;
        int[] indices = new int[indicesCount];
        int indicePointer = 0;
        
        for (int z = 0; z < gridSize - 1; z++) {
            for (int x = 0; x < gridSize - 1; x++) {
                int topLeft = z * gridSize + x;
                int topRight = topLeft + 1;
                int bottomLeft = (z + 1) * gridSize + x;
                int bottomRight = bottomLeft + 1;
                
                // Tam giác 1 (trên trái)
                indices[indicePointer++] = topLeft;
                indices[indicePointer++] = bottomLeft;
                indices[indicePointer++] = topRight;
                
                // Tam giác 2 (dưới phải)
                indices[indicePointer++] = topRight;
                indices[indicePointer++] = bottomLeft;
                indices[indicePointer++] = bottomRight;
            }
        }
        
        vertexCount = indicesCount;
        
        // Upload dữ liệu lên GPU
        uploadToGPU(vertices, normals, texCoords, indices);
    }

    private void calculateNormals(float[] normals, float[] vertices) {
        // Tính normal vectors sử dụng tích có hướng (cross product) của các cạnh tam giác
        for (int z = 0; z < gridSize; z++) {
            for (int x = 0; x < gridSize; x++) {
                Vector3f normal = new Vector3f(0, 0, 0);
                
                // Lấy vị trí vertex hiện tại
                int index = (z * gridSize + x) * 3;
                Vector3f v0 = new Vector3f(vertices[index], vertices[index + 1], vertices[index + 2]);
                
                // Tính normal từ các tam giác xung quanh
                if (x > 0 && z > 0) {
                    int leftIndex = (z * gridSize + (x - 1)) * 3;
                    int topIndex = ((z - 1) * gridSize + x) * 3;
                    
                    Vector3f v1 = new Vector3f(vertices[leftIndex], vertices[leftIndex + 1], vertices[leftIndex + 2]);
                    Vector3f v2 = new Vector3f(vertices[topIndex], vertices[topIndex + 1], vertices[topIndex + 2]);
                    
                    Vector3f edge1 = new Vector3f(v1).sub(v0);
                    Vector3f edge2 = new Vector3f(v2).sub(v0);
                    Vector3f cross = new Vector3f(edge1).cross(edge2);
                    normal.add(cross);
                }
                
                if (x < gridSize - 1 && z < gridSize - 1) {
                    int rightIndex = (z * gridSize + (x + 1)) * 3;
                    int bottomIndex = ((z + 1) * gridSize + x) * 3;
                    
                    Vector3f v1 = new Vector3f(vertices[rightIndex], vertices[rightIndex + 1], vertices[rightIndex + 2]);
                    Vector3f v2 = new Vector3f(vertices[bottomIndex], vertices[bottomIndex + 1], vertices[bottomIndex + 2]);
                    
                    Vector3f edge1 = new Vector3f(v1).sub(v0);
                    Vector3f edge2 = new Vector3f(v2).sub(v0);
                    Vector3f cross = new Vector3f(edge1).cross(edge2);
                    normal.add(cross);
                }
                
                // Chuẩn hóa vector
                normal.normalize();
                
                normals[index] = normal.x;
                normals[index + 1] = normal.y;
                normals[index + 2] = normal.z;
            }
        }
    }

    private void uploadToGPU(float[] vertices, float[] normals, float[] texCoords, int[] indices) {
        // Tạo VAO (Vertex Array Object)
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        // Upload vertices (vị trí đỉnh)
        FloatBuffer verticesBuffer = memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();
        vertexVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        memFree(verticesBuffer);
        
        // Upload normals (vector pháp tuyến)
        FloatBuffer normalsBuffer = memAllocFloat(normals.length);
        normalsBuffer.put(normals).flip();
        normalVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        memFree(normalsBuffer);
        
        // Upload texture coordinates (tọa độ UV)
        FloatBuffer texCoordsBuffer = memAllocFloat(texCoords.length);
        texCoordsBuffer.put(texCoords).flip();
        texCoordVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texCoordVboId);
        glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        memFree(texCoordsBuffer);
        
        // Upload indices (chỉ số đỉnh)
        IntBuffer indicesBuffer = memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        indicesVboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        memFree(indicesBuffer);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vertexVboId);
        glDeleteBuffers(normalVboId);
        glDeleteBuffers(texCoordVboId);
        glDeleteBuffers(indicesVboId);
        
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public float getHeightAt(float x, float z) {
        // Lấy độ cao tại vị trí (x, z)
        int gridX = (int) (x / scale);
        int gridZ = (int) (z / scale);
        
        if (gridX < 0 || gridX >= gridSize - 1 || gridZ < 0 || gridZ >= gridSize - 1) {
            return 0;
        }
        
        return heights[gridZ][gridX];
    }

    public int getGridSize() {
        return gridSize;
    }

    public float getScale() {
        return scale;
    }
}
