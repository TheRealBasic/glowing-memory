package com.glowingmemory.world;

import java.util.Random;

public class SimplexNoise {
    private final short[] perm = new short[512];
    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

    public SimplexNoise(long seed) {
        short[] p = new short[256];
        Random rand = new Random(seed);
        for (int i = 0; i < 256; i++) {
            p[i] = (short) i;
        }
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            short tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }

    public double noise(double xin, double zin) {
        double s = (xin + zin) * F2;
        int i = fastfloor(xin + s);
        int j = fastfloor(zin + s);
        double t = (i + j) * G2;
        double X0 = i - t;
        double Z0 = j - t;
        double x0 = xin - X0;
        double z0 = zin - Z0;

        int i1, j1;
        if (x0 > z0) { i1 = 1; j1 = 0; }
        else { i1 = 0; j1 = 1; }

        double x1 = x0 - i1 + G2;
        double z1 = z0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double z2 = z0 - 1.0 + 2.0 * G2;

        int ii = i & 255;
        int jj = j & 255;
        double n0 = corner(ii, jj, x0, z0);
        double n1 = corner(ii + i1, jj + j1, x1, z1);
        double n2 = corner(ii + 1, jj + 1, x2, z2);

        return 70.0 * (n0 + n1 + n2);
    }

    private double corner(int i, int j, double x, double z) {
        double t = 0.5 - x * x - z * z;
        if (t < 0) return 0.0;
        t *= t;
        int gi = perm[i + perm[j]] % 12;
        double grad = grad(gi, x, z);
        return t * t * grad;
    }

    private int fastfloor(double x) {
        return x > 0 ? (int) x : (int) x - 1;
    }

    private double grad(int hash, double x, double z) {
        switch (hash % 12) {
            case 0: return x + z;
            case 1: return -x + z;
            case 2: return x - z;
            case 3: return -x - z;
            case 4: return x;
            case 5: return -x;
            case 6: return z;
            case 7: return -z;
            case 8: return x + 0.5 * z;
            case 9: return -x + 0.5 * z;
            case 10: return 0.5 * x + z;
            default: return 0.5 * x - z;
        }
    }
}
