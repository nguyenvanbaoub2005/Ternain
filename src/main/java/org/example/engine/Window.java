package org.example.engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final String title;
    private int width;
    private int height;
    private long windowHandle;
    private boolean resized;
    private boolean vSync;

    public Window(String title, int width, int height, boolean vSync) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
    }

    public void init() {
        // Thiết lập callback để bắt lỗi
        GLFWErrorCallback.createPrint(System.err).set();

        // Khởi tạo GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Không thể khởi tạo GLFW");
        }

        // Cấu hình GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // Ẩn cửa sổ ban đầu
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);  // Cho phép thay đổi kích thước
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);  // OpenGL 3.3
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Tạo cửa sổ
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Không thể tạo cửa sổ GLFW");
        }

        // Thiết lập callback khi thay đổi kích thước cửa sổ
        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> {
            this.width = w;
            this.height = h;
            this.resized = true;
        });

        // Thiết lập callback phím bấm
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);  // Đóng cửa sổ khi nhấn ESC
            }
        });

        // Căn giữa cửa sổ
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowHandle, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // Đặt OpenGL context hiện tại
        glfwMakeContextCurrent(windowHandle);

        if (vSync) {
            glfwSwapInterval(1);  // Bật VSync
        }

        // Hiển thị cửa sổ
        glfwShowWindow(windowHandle);

        GL.createCapabilities();

        // Thiết lập màu nền (màu xanh trời)
        glClearColor(0.53f, 0.81f, 0.92f, 1.0f);
        glEnable(GL_DEPTH_TEST);  // Bật depth testing
        glEnable(GL_CULL_FACE);   // Bật face culling
        glCullFace(GL_BACK);      // Không vẽ mặt sau
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getWindowHandle() {
        return windowHandle;
    }
}
