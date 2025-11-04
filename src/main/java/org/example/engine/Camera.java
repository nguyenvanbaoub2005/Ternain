package org.example.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.example.terrain.Terrain;

public class Camera {
    private final Vector3f position;  // Vị trí camera
    private final Vector3f rotation;  // Góc xoay camera (pitch, yaw, roll)
    
    private float moveSpeed = 20.0f;           // Tốc độ di chuyển (đơn vị/giây)
    private float mouseSensitivity = 0.12f;    // Độ nhạy chuột vừa phải
    
    private Terrain terrain;                   // Địa hình để kiểm tra va chạm
    private float minHeightAboveTerrain = 2.0f; // Khoảng cách tối thiểu phía trên địa hình

    public Camera() {
        position = new Vector3f(0, 50, 0);
        rotation = new Vector3f(0, 0, 0);
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }
    
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public void moveForward(float distance) {
        // Di chuyển theo hướng nhìn (forward vector)
        position.x += (float) Math.sin(Math.toRadians(rotation.y)) * distance;
        position.z -= (float) Math.cos(Math.toRadians(rotation.y)) * distance;
        checkTerrainCollision();
    }

    public void moveBackward(float distance) {
        // Di chuyển ngược hướng nhìn
        position.x -= (float) Math.sin(Math.toRadians(rotation.y)) * distance;
        position.z += (float) Math.cos(Math.toRadians(rotation.y)) * distance;
        checkTerrainCollision();
    }

    public void moveLeft(float distance) {
        // Di chuyển sang trái (vuông góc với hướng nhìn)
        position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * distance;
        position.z -= (float) Math.cos(Math.toRadians(rotation.y - 90)) * distance;
        checkTerrainCollision();
    }

    public void moveRight(float distance) {
        // Di chuyển sang phải (vuông góc với hướng nhìn)
        position.x += (float) Math.sin(Math.toRadians(rotation.y + 90)) * distance;
        position.z -= (float) Math.cos(Math.toRadians(rotation.y + 90)) * distance;
        checkTerrainCollision();
    }

    public void moveUp(float distance) {
        // Di chuyển lên trên (trục Y)
        position.y += distance;
    }

    public void moveDown(float distance) {
        // Di chuyển xuống dưới (trục Y)
        position.y -= distance;
        checkTerrainCollision();
    }
    
    /**
     * Kiểm tra và ngăn camera xuyên qua địa hình
     */
    private void checkTerrainCollision() {
        if (terrain != null) {
            float terrainHeight = terrain.getHeightAt(position.x, position.z);
            float minY = terrainHeight + minHeightAboveTerrain;
            
            if (position.y < minY) {
                position.y = minY;
            }
        }
    }

    public void rotate(float offsetX, float offsetY) {
        // Xoay camera theo di chuyển chuột
        rotation.x += offsetY * mouseSensitivity;  // Pitch (nhìn lên/xuống)
        rotation.y += offsetX * mouseSensitivity;  // Yaw (nhìn trái/phải)

        // Giới hạn góc pitch để tránh camera lật ngược
        if (rotation.x > 89.0f) {
            rotation.x = 89.0f;
        }
        if (rotation.x < -89.0f) {
            rotation.x = -89.0f;
        }
    }

    public Matrix4f getViewMatrix() {
        // Tạo ma trận view (biến đổi từ world space sang camera space)
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        
        // Xoay trước, sau đó dịch chuyển
        viewMatrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                  .rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        
        viewMatrix.translate(-position.x, -position.y, -position.z);
        
        return viewMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public void setMouseSensitivity(float sensitivity) {
        this.mouseSensitivity = sensitivity;
    }
}
