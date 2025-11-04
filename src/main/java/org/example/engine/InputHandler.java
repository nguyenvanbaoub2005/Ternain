package org.example.engine;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private final Vector2f displayVector;
    private final Vector2d previousPos;
    private final Vector2d currentPos;
    private boolean inWindow;
    private boolean leftButtonPressed;
    private boolean rightButtonPressed;
    private boolean firstMouse;

    public InputHandler() {
        previousPos = new Vector2d(-1, -1);
        currentPos = new Vector2d(0, 0);
        displayVector = new Vector2f();
        inWindow = false;
        leftButtonPressed = false;
        rightButtonPressed = false;
        firstMouse = true;
    }

    public void init(Window window) {
        long windowHandle = window.getWindowHandle();
        
        // Callback cho vị trí chuột
        glfwSetCursorPosCallback(windowHandle, (handle, xpos, ypos) -> {
            currentPos.x = xpos;
            currentPos.y = ypos;
        });

        // Callback cho nút chuột
        glfwSetMouseButtonCallback(windowHandle, (handle, button, action, mods) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });

        // Callback khi chuột vào/ra cửa sổ
        glfwSetCursorEnterCallback(windowHandle, (handle, entered) -> {
            inWindow = entered;
        });

        // Bắt con trỏ chuột (FPS mode)
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void update(Window window) {
        displayVector.x = 0;
        displayVector.y = 0;
        
        if (firstMouse) {
            previousPos.x = currentPos.x;
            previousPos.y = currentPos.y;
            firstMouse = false;
        }

        if (previousPos.x > 0 && previousPos.y > 0 && inWindow) {
            double deltax = currentPos.x - previousPos.x;
            double deltay = currentPos.y - previousPos.y;
            
            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;
            
            if (rotateX) {
                displayVector.y = (float) deltax;
            }
            if (rotateY) {
                displayVector.x = (float) deltay;
            }
        }
        
        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

    public Vector2f getDisplayVector() {
        return displayVector;
    }

    public boolean isKeyPressed(Window window, int keyCode) {
        return glfwGetKey(window.getWindowHandle(), keyCode) == GLFW_PRESS;
    }
}
