package org.example;

import org.example.engine.*;
import org.example.graphics.Skybox;
import org.example.graphics.TreeSystem;
import org.example.terrain.PerlinNoise;
import org.example.terrain.Terrain;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

public class Main {
    // Window settings
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final String TITLE = "3D Terrain Generator - OpenGL";
    
    // Terrain settings
    private static final int TERRAIN_SIZE = 200;
    private static final float TERRAIN_SCALE = 2.0f;
    private static final float HEIGHT_SCALE = 30.0f;
    
    // Fog settings
    private static final Vector3f FOG_COLOR = new Vector3f(0.7f, 0.8f, 0.9f);
    private static final float FOG_DENSITY = 0.007f;
    private static final float FOG_GRADIENT = 1.5f;
    
    private Window window;
    private Camera camera;
    private InputHandler inputHandler;
    
    // Shaders
    private ShaderProgram terrainShader;
    private ShaderProgram skyboxShader;
    private ShaderProgram treeShader;
    
    // Objects
    private Terrain terrain;
    private Skybox skybox;
    private TreeSystem treeSystem;
    
    private Texture grassTexture;
    private Texture rockTexture;
    private Texture snowTexture;
    
    private Matrix4f projectionMatrix;
    private Vector3f lightPos;
    
    private float lastFrame = 0.0f;

    public static void main(String[] args) {
        try {
            Main app = new Main();
            app.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        init();
        gameLoop();
        cleanup();
    }

    private void init() throws Exception {
        System.out.println("=== Khởi tạo Trình Tạo Địa Hình 3D ===");
        
        // Tạo cửa sổ OpenGL
        System.out.println("→ Đang tạo cửa sổ...");
        window = new Window(TITLE, WIDTH, HEIGHT, true);
        window.init();
        
        // Khởi tạo camera ở giữa địa hình
        System.out.println("→ Đang khởi tạo camera...");
        float centerX = (TERRAIN_SIZE * TERRAIN_SCALE) / 2.0f;
        float centerZ = (TERRAIN_SIZE * TERRAIN_SCALE) / 2.0f;
        // Đặt camera ở vị trí cao hơn và nhìn xuống để thấy rõ dòng sông
        camera = new Camera(new Vector3f(centerX + 50, 60, centerZ - 100), new Vector3f(20, 45, 0));
        
        // Khởi tạo bộ xử lý đầu vào (bàn phím + chuột)
        System.out.println("→ Đang thiết lập điều khiển...");
        inputHandler = new InputHandler();
        inputHandler.init(window);
        
        // Tạo và biên dịch shaders
        System.out.println("→ Đang biên dịch shaders...");
        
        // Terrain shader
        terrainShader = new ShaderProgram();
        terrainShader.createVertexShader(loadResource("/shaders/terrain.vert"));
        terrainShader.createFragmentShader(loadResource("/shaders/terrain.frag"));
        terrainShader.link();
        
        // Skybox shader
        skyboxShader = new ShaderProgram();
        skyboxShader.createVertexShader(loadResource("/shaders/skybox.vert"));
        skyboxShader.createFragmentShader(loadResource("/shaders/skybox.frag"));
        skyboxShader.link();
        
        // Tree shader
        treeShader = new ShaderProgram();
        treeShader.createVertexShader(loadResource("/shaders/tree.vert"));
        treeShader.createFragmentShader(loadResource("/shaders/tree.frag"));
        treeShader.link();
        
        // Tạo texture cho địa hình
        System.out.println("→ Đang tạo texture (cỏ, đá, tuyết)...");
        grassTexture = ProceduralTexture.createGrassTexture(256);
        rockTexture = ProceduralTexture.createRockTexture(256);
        snowTexture = ProceduralTexture.createSnowTexture(256);
        
        // Sinh địa hình bằng Perlin Noise
        System.out.println("→ Đang sinh địa hình ngẫu nhiên...");
        PerlinNoise noise = new PerlinNoise(System.currentTimeMillis());
        terrain = new Terrain(TERRAIN_SIZE, TERRAIN_SCALE, HEIGHT_SCALE, noise);
        
        // Gắn terrain vào camera để kiểm tra va chạm
        camera.setTerrain(terrain);
        
        // Tạo Skybox
        System.out.println("→ Đang tạo skybox...");
        skybox = new Skybox();
        
        // Tạo cây cối
        System.out.println("→ Đang sinh cây cối...");
        treeSystem = new TreeSystem();
        Random random = new Random(System.currentTimeMillis());
        float terrainSize = TERRAIN_SIZE * TERRAIN_SCALE;
        // Sinh cây trên toàn bộ địa hình, tránh vùng thấp
        treeSystem.generateTrees(300, 0, terrainSize, 0, terrainSize, 5.0f, terrain, random);
        
        // Thiết lập ma trận phép chiếu (projection)
        System.out.println("→ Đang thiết lập camera projection...");
        projectionMatrix = new Matrix4f();
        float aspectRatio = (float) window.getWidth() / window.getHeight();
        projectionMatrix.perspective((float) Math.toRadians(60.0f), aspectRatio, 0.1f, 1000.0f);
        
        // Thiết lập nguồn sáng
        lightPos = new Vector3f(centerX + 100, 150, centerZ + 100);
        
        System.out.println("\n✓ Khởi tạo hoàn tất!");
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║       HƯỚNG DẪN ĐIỀU KHIỂN         ║");
        System.out.println("╠════════════════════════════════════╣");
        System.out.println("║  W/A/S/D    - Di chuyển camera     ║");
        System.out.println("║  Space      - Bay lên              ║");
        System.out.println("║  Shift      - Bay xuống            ║");
        System.out.println("║  Chuột      - Xoay góc nhìn        ║");
        System.out.println("║  ESC        - Thoát                ║");
        System.out.println("╚════════════════════════════════════╝\n");
    }

    private void gameLoop() {
        while (!window.windowShouldClose()) {
            float currentFrame = (float) glfwGetTime();
            float deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;
            
            // Xử lý input
            processInput(deltaTime);
            
            // Cập nhật
            update(deltaTime);
            
            // Render
            render();
            
            // Swap buffers và poll events
            window.update();
        }
    }

    private void processInput(float deltaTime) {
        inputHandler.update(window);
        
        float speed = camera.getMoveSpeed() * deltaTime;
        
        // Xử lý di chuyển camera
        if (inputHandler.isKeyPressed(window, GLFW_KEY_W)) {
            camera.moveForward(speed);  // Tiến lên
        }
        if (inputHandler.isKeyPressed(window, GLFW_KEY_S)) {
            camera.moveBackward(speed);  // Lùi lại
        }
        if (inputHandler.isKeyPressed(window, GLFW_KEY_A)) {
            camera.moveLeft(speed);  // Sang trái
        }
        if (inputHandler.isKeyPressed(window, GLFW_KEY_D)) {
            camera.moveRight(speed);  // Sang phải
        }
        if (inputHandler.isKeyPressed(window, GLFW_KEY_SPACE)) {
            camera.moveUp(speed);  // Bay lên
        }
        if (inputHandler.isKeyPressed(window, GLFW_KEY_LEFT_SHIFT)) {
            camera.moveDown(speed);  // Bay xuống
        }
        
        // Xử lý xoay camera bằng chuột
        Vector2f mouseMovement = inputHandler.getDisplayVector();
        if (mouseMovement.lengthSquared() > 0) {
            camera.rotate(mouseMovement.y, mouseMovement.x);
        }
    }

    private void update(float deltaTime) {
        // Xử lý khi thay đổi kích thước cửa sổ
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            float aspectRatio = (float) window.getWidth() / window.getHeight();
            projectionMatrix.identity();
            projectionMatrix.perspective((float) Math.toRadians(60.0f), aspectRatio, 0.1f, 1000.0f);
            window.setResized(false);
        }
    }

    private void render() {
        // Xóa màn hình và depth buffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        Matrix4f modelMatrix = new Matrix4f().identity();
        Matrix4f viewMatrix = camera.getViewMatrix();
        
        // 1. Render Skybox (vẽ trước, ở xa nhất)
        skyboxShader.bind();
        skyboxShader.setUniform("view", viewMatrix);
        skyboxShader.setUniform("projection", projectionMatrix);
        skyboxShader.setUniform("skybox", 0);
        skybox.render();
        skyboxShader.unbind();
        
        // 2. Render Terrain với Fog
        terrainShader.bind();
        terrainShader.setUniform("model", modelMatrix);
        terrainShader.setUniform("view", viewMatrix);
        terrainShader.setUniform("projection", projectionMatrix);
        terrainShader.setUniform("lightPos", lightPos);
        terrainShader.setUniform("viewPos", camera.getPosition());
        terrainShader.setUniform("lightColor", new Vector3f(1.0f, 1.0f, 1.0f));
        
        // Fog uniforms
        terrainShader.setUniform("fogColor", FOG_COLOR);
        terrainShader.setUniform("fogDensity", FOG_DENSITY);
        terrainShader.setUniform("fogGradient", FOG_GRADIENT);
        
        // Gán textures
        terrainShader.setUniform("grassTexture", 0);
        terrainShader.setUniform("rockTexture", 1);
        terrainShader.setUniform("snowTexture", 2);
        
        grassTexture.bind(0);
        rockTexture.bind(1);
        snowTexture.bind(2);
        
        terrain.render();
        terrainShader.unbind();
        
        // 3. Render Trees
        treeShader.bind();
        treeShader.setUniform("model", modelMatrix);
        treeShader.setUniform("view", viewMatrix);
        treeShader.setUniform("projection", projectionMatrix);
        treeShader.setUniform("lightPos", lightPos);
        treeShader.setUniform("viewPos", camera.getPosition());
        treeShader.setUniform("fogColor", FOG_COLOR);
        treeShader.setUniform("fogDensity", FOG_DENSITY);
        treeShader.setUniform("fogGradient", FOG_GRADIENT);
        
        treeSystem.render();
        treeShader.unbind();
    }

    private void cleanup() {
        System.out.println("\n→ Đang dọn dẹp tài nguyên...");
        
        if (terrain != null) {
            terrain.cleanup();
        }
        if (skybox != null) {
            skybox.cleanup();
        }
        if (treeSystem != null) {
            treeSystem.cleanup();
        }
        if (terrainShader != null) {
            terrainShader.cleanup();
        }
        if (skyboxShader != null) {
            skyboxShader.cleanup();
        }
        if (treeShader != null) {
            treeShader.cleanup();
        }
        if (grassTexture != null) {
            grassTexture.cleanup();
        }
        if (rockTexture != null) {
            rockTexture.cleanup();
        }
        if (snowTexture != null) {
            snowTexture.cleanup();
        }
        if (window != null) {
            window.cleanup();
        }
        
        System.out.println("✓ Dọn dẹp hoàn tất!");
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║     CẢM ƠN BẠN ĐÃ SỬ DỤNG!         ║");
        System.out.println("╚════════════════════════════════════╝");
    }

    private String loadResource(String path) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new Exception("Không tìm thấy file: " + path);
            }
            return new String(is.readAllBytes());
        }
    }
}