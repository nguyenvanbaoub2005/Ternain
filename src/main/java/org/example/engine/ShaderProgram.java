package org.example.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Không thể tạo Shader Program");
        }
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Lỗi khi tạo shader. Loại: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Lỗi biên dịch Shader: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Lỗi liên kết Shader: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Cảnh báo khi validate Shader: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            int location = glGetUniformLocation(programId, uniformName);
            glUniformMatrix4fv(location, false, fb);
        }
    }

    public void setUniform(String uniformName, Vector3f value) {
        int location = glGetUniformLocation(programId, uniformName);
        glUniform3f(location, value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, float value) {
        int location = glGetUniformLocation(programId, uniformName);
        glUniform1f(location, value);
    }

    public void setUniform(String uniformName, int value) {
        int location = glGetUniformLocation(programId, uniformName);
        glUniform1i(location, value);
    }

    public static String loadShaderFile(String filePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
