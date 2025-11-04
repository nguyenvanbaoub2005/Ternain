package org.example.terrain;

import java.util.Random;

/**
 * Bộ sinh Perlin Noise để tạo độ cao địa hình
 * Dựa trên thuật toán Perlin Noise cải tiến của Ken Perlin
 */
public class PerlinNoise {
    private static final int PERMUTATION_SIZE = 256;
    private final int[] permutation;  // Bảng hoán vị
    private final int[] p;            // Bảng hoán vị mở rộng (x2)

    public PerlinNoise(long seed) {
        permutation = new int[PERMUTATION_SIZE];
        p = new int[PERMUTATION_SIZE * 2];
        
        Random random = new Random(seed);
        
        // Khởi tạo mảng hoán vị
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            permutation[i] = i;
        }
        
        // Xáo trộn mảng (Fisher-Yates shuffle)
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            int j = random.nextInt(PERMUTATION_SIZE);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        
        // Nhân đôi mảng hoán vị để tránh tràn số
        for (int i = 0; i < PERMUTATION_SIZE * 2; i++) {
            p[i] = permutation[i % PERMUTATION_SIZE];
        }
    }

    /**
     * Tạo giá trị Perlin noise 2D
     * @param x Tọa độ X
     * @param z Tọa độ Z
     * @return Giá trị noise trong khoảng -1 đến 1
     */
    public double noise(double x, double z) {
        // Tìm ô lưới chứa điểm
        int X = (int) Math.floor(x) & 255;
        int Z = (int) Math.floor(z) & 255;
        
        // Lấy tọa độ tương đối trong ô
        x -= Math.floor(x);
        z -= Math.floor(z);
        
        // Tính đường cong fade (làm mượt)
        double u = fade(x);
        double v = fade(z);
        
        // Hash tọa độ các góc
        int A = p[X] + Z;
        int AA = p[A];
        int AB = p[A + 1];
        int B = p[X + 1] + Z;
        int BA = p[B];
        int BB = p[B + 1];
        
        // Nội suy kết quả từ các góc
        double result = lerp(v, 
            lerp(u, grad(p[AA], x, z), grad(p[BA], x - 1, z)),
            lerp(u, grad(p[AB], x, z - 1), grad(p[BB], x - 1, z - 1))
        );
        
        return result;
    }

    /**
     * Tạo fractal noise với nhiều tầng (octaves)
     * @param x Tọa độ X
     * @param z Tọa độ Z
     * @param octaves Số lượng tầng noise
     * @param persistence Độ ảnh hưởng của mỗi tầng (thường 0.5)
     * @return Giá trị noise tổng hợp
     */
    public double fractalNoise(double x, double z, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;      // Tần số (độ chi tiết)
        double amplitude = 1;      // Biên độ (độ cao)
        double maxValue = 0;       // Giá trị tối đa để chuẩn hóa
        
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, z * frequency) * amplitude;
            
            maxValue += amplitude;
            amplitude *= persistence;  // Giảm biên độ
            frequency *= 2;            // Tăng tần số
        }
        
        // Chuẩn hóa về khoảng -1 đến 1
        return total / maxValue;
    }

    private double fade(double t) {
        // Hàm fade: 6t^5 - 15t^4 + 10t^3 (làm mượt đường cong)
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        // Nội suy tuyến tính (Linear interpolation)
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double z) {
        // Chuyển 4 bit thấp của hash thành vector gradient
        int h = hash & 3;
        double u = h < 2 ? x : z;
        double v = h < 2 ? z : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
