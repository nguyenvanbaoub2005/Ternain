# 🏔️ 3D Terrain Generator - OpenGL

Dự án sinh địa hình 3D ngẫu nhiên (núi, đồi) sử dụng **Perlin Noise** và hiển thị với **OpenGL** trong Java.

## 📋 Mục tiêu

-  ✅ Sinh địa hình 3D ngẫu nhiên bằng thuật toán Perlin Noise
-  ✅ Hiển thị mesh 3D với OpenGL (LWJGL)
-  ✅ Ánh sáng Blinn-Phong cho địa hình
-  ✅ Texture blending (cỏ, đá, tuyết) dựa theo độ cao
-  ✅ Camera bay (Fly Camera) với điều khiển WASD + chuột

## 🛠️ Công nghệ sử dụng

-  **Java 21**
-  **Maven** - Build tool
-  **LWJGL 3.3.3** - OpenGL binding cho Java
   -  OpenGL - Graphics rendering
   -  GLFW - Window & input handling
   -  STB - Image loading
-  **JOML** - Java OpenGL Math Library

## 📁 Cấu trúc dự án

```
OpenGL_Detai/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/example/
│       │       ├── Main.java                 # Entry point
│       │       ├── engine/                    # Engine core
│       │       │   ├── Window.java           # GLFW window
│       │       │   ├── Camera.java           # Fly camera
│       │       │   ├── ShaderProgram.java    # GLSL shader loader
│       │       │   ├── Texture.java          # Texture loader
│       │       │   ├── ProceduralTexture.java # Procedural texture gen
│       │       │   └── InputHandler.java     # Keyboard & mouse
│       │       └── terrain/                   # Terrain generation
│       │           ├── PerlinNoise.java      # Perlin noise algorithm
│       │           └── Terrain.java          # Terrain mesh generator
│       └── resources/
│           └── shaders/
│               ├── terrain.vert              # Vertex shader
│               └── terrain.frag              # Fragment shader
└── pom.xml                                    # Maven config
```

## 🚀 Cách chạy dự án

### 1. Yêu cầu hệ thống

-  Java 21 trở lên
-  Maven 3.6+
-  GPU hỗ trợ OpenGL 3.3+

### 2. Build dự án

```bash
mvn clean install
```

### 3. Chạy ứng dụng

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Hoặc trong IntelliJ IDEA:

-  Mở file `Main.java`
-  Click nút Run (▶️)

## 🎮 Điều khiển

| Phím      | Chức năng                     |
| --------- | ----------------------------- |
| **W**     | Di chuyển về phía trước       |
| **S**     | Di chuyển về phía sau         |
| **A**     | Di chuyển sang trái           |
| **D**     | Di chuyển sang phải           |
| **Space** | Bay lên                       |
| **Shift** | Bay xuống                     |
| **Mouse** | Xoay camera (nhìn xung quanh) |
| **ESC**   | Thoát chương trình            |

## 🎨 Các tính năng đã implement

### 1. **Perlin Noise Generator** ⭐⭐

-  Thuật toán Perlin Noise cải tiến
-  Fractal noise với nhiều octave
-  Tạo địa hình tự nhiên và mượt mà

### 2. **Terrain Mesh Generation** ⭐⭐

-  Grid-based mesh (200x200 vertices)
-  Tự động tính normal vectors cho ánh sáng
-  VBO/VAO upload lên GPU

### 3. **Blinn-Phong Lighting** ⭐⭐

-  Ambient, Diffuse, Specular lighting
-  Normal mapping cho địa hình
-  Directional light source

### 4. **Texture Blending** ⭐⭐⭐

-  3 loại texture: Cỏ (grass), Đá (rock), Tuyết (snow)
-  Blend dựa theo độ cao (height-based)
-  Blend thêm dựa theo độ dốc (slope-based)
-  Procedurally generated textures

### 5. **Fly Camera** ⭐⭐

-  WASD movement
-  Mouse look (FPS-style)
-  Smooth camera controls

## 🧮 Công thức Perlin Noise

Perlin Noise tạo ra giá trị ngẫu nhiên "mượt mà" bằng cách:

1. **Grid-based hashing**: Chia không gian thành lưới
2. **Gradient interpolation**: Nội suy giữa các góc
3. **Fractal layering**: Nhiều tầng noise với frequency khác nhau

```java
double height = noise.fractalNoise(x, z, octaves=6, persistence=0.5);
```

## 📊 Thông số địa hình

| Tham số      | Giá trị | Mô tả                       |
| ------------ | ------- | --------------------------- |
| Grid Size    | 200x200 | Số lượng vertices           |
| Scale        | 2.0     | Khoảng cách giữa các điểm   |
| Height Scale | 30.0    | Chiều cao tối đa            |
| Octaves      | 6       | Số lớp noise                |
| Persistence  | 0.5     | Độ ảnh hưởng của mỗi octave |

## 🎯 Texture Blending Logic

```
Height < 5:    🌱 Cỏ (Grass)
Height 5-15:   🌱➜🪨 Cỏ → Đá (transition)
Height 15-25:  🪨➜❄️ Đá → Tuyết (transition)
Height > 25:   ❄️ Tuyết (Snow)

+ Độ dốc cao → Thêm đá
```

## 🔧 Cấu hình nâng cao

Bạn có thể chỉnh sửa trong `Main.java`:

```java
// Window settings
private static final int WIDTH = 1280;
private static final int HEIGHT = 720;

// Terrain settings
private static final int TERRAIN_SIZE = 200;     // Tăng = nhiều chi tiết hơn
private static final float TERRAIN_SCALE = 2.0f;  // Tăng = rộng hơn
private static final float HEIGHT_SCALE = 30.0f;  // Tăng = núi cao hơn
```

## 🚀 Mở rộng trong tương lai

-  [ ] **LOD (Level of Detail)**: Giảm polygon xa camera
-  [ ] **Water rendering**: Mặt nước phản chiếu
-  [ ] **Skybox**: Bầu trời 360°
-  [ ] **Fog effect**: Sương mù tạo chiều sâu
-  [ ] **Heightmap import**: Load địa hình từ ảnh
-  [ ] **Procedural trees**: Cây cối tự động
-  [ ] **Shadow mapping**: Bóng đổ thật

## 📚 Kiến thức đã áp dụng

1. **OpenGL Core Concepts**

   -  VAO/VBO (Vertex Buffer Objects)
   -  Shader Programs (GLSL)
   -  Texture mapping
   -  Depth testing & Culling

2. **Computer Graphics**

   -  3D transformations (Model-View-Projection)
   -  Lighting models (Blinn-Phong)
   -  Normal vector calculation
   -  Texture blending

3. **Procedural Generation**

   -  Perlin Noise algorithm
   -  Fractal noise
   -  Mesh generation

4. **Game Development**
   -  Game loop
   -  Input handling
   -  Camera systems
   -  Resource management

## 📝 Tài liệu tham khảo

-  [LWJGL Documentation](https://www.lwjgl.org/)
-  [LearnOpenGL](https://learnopengl.com/)
-  [Perlin Noise by Ken Perlin](https://mrl.nyu.edu/~perlin/noise/)
-  [OpenGL Tutorial](http://www.opengl-tutorial.org/)

## 👨‍💻 Tác giả

Dự án thực hiện cho môn **Lập Trình Mạng** - OpenGL

## 📄 License

MIT License - Free to use for educational purposes

---

**Chúc bạn code vui! 🎉**
# Ternain
