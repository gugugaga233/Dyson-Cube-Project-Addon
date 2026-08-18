package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.render;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;

/**
 * 星体像素图标生成器
 * <p>
 * 根据星体类型生成 128×128 像素纹理（ARGB 格式）。
 * 每个方法返回 int[128*128] 像素数组，供 {@link StarTextureManager} 缓存。
 * <p>
 * 画布尺寸：128×128，坐标原点：左上角 (0,0)，中心点：默认 (64,64)。
 * 颜色格式：ARGB（0xAARRGGBB），默认 Alpha = 0xFF（不透明）。
 * 绘制方式：遍历像素，根据几何形状填充颜色数组。
 */
public final class StarTextureGenerator {

    private static final int SIZE = 128;
    private static final int CENTER = 64;

    private StarTextureGenerator() {}

    /**
     * 根据星体类型生成对应纹理。
     *
     * @param star 星体数据
     * @return 128×128 ARGB 像素数组
     */
    public static int[] generate(StarData star) {
        if (star == null) star = StarData.SUN;
return switch (star.type()) {
            // 主序星
            case "M_TYPE" -> generateRedDwarf();
            case "K_TYPE" -> generateOrangeDwarf();
            case "G_TYPE" -> generateYellowDwarf();
            case "F_TYPE" -> generateFStar();
            case "A_TYPE" -> generateAStar();
            case "B_TYPE" -> generateBStar();
            case "O_TYPE" -> generateOStar();
            // 棕矮星
            case "L_DWARF" -> generateLDwarf();
            case "T_DWARF" -> generateTDwarf();
            case "Y_DWARF" -> generateYDwarf();
            // 致密星
            case "WHITE_DWARF" -> generateWhiteDwarf();
            case "NEUTRON_STAR" -> generateNeutronStar();
            case "QUARK_STAR" -> generateQuarkStar();
            case "Q_STAR" -> generateQStar();
            case "ELECTROWEAK_STAR" -> generateElectroweakStar();
            case "BOSON_STAR" -> generateBosonStar();
            // 星系核
            case "SEYFERT" -> generateSeyfert();
            case "BLAZAR" -> generateBlazar();
            case "QUASAR" -> generateQuasar();
            // 巨星
            case "RED_SUPERGIANT" -> generateRedSupergiant();
            case "WOLF_RAYET" -> generateWolfRayet();
            // 行星
            case "HOT_JUPITER" -> generateHotJupiter();
            case "SUPER_EARTH" -> generateSuperEarth();
            // 特殊事件
            case "BLUE_STRAGGLER" -> generateBlueStraggler();
            case "SUPERNOVA" -> generateSupernova();
            case "HYPERNOVA" -> generateHypernova();
            case "GRB" -> generateGRB();
            // 黑洞
            case "STELLAR_BH" -> generateStellarBlackHole();
            case "SUPERMASSIVE_BH" -> generateSupermassiveBlackHole();
            // 反物质星体：统一使用紫色调
            case "ANTI_M_TYPE", "ANTI_K_TYPE", "ANTI_G_TYPE",
                 "ANTI_F_TYPE", "ANTI_A_TYPE", "ANTI_B_TYPE", "ANTI_O_TYPE" -> generateAntimatterStar();
            case "ANTI_L_DWARF", "ANTI_T_DWARF", "ANTI_Y_DWARF" -> generateAntimatterStar();
            case "ANTI_WHITE_DWARF", "ANTI_NEUTRON_STAR", "ANTI_QUARK_STAR", "ANTI_Q_STAR",
                 "ANTI_ELECTROWEAK_STAR" -> generateAntimatterStar();
            case "ANTI_BLACK_HOLE", "ANTI_STELLAR_BH", "ANTI_SUPERMASSIVE_BH" -> generateAntimatterBlackHole();
            // 寰宇之心：∞符号图标
            case "COSMIC_HEART" -> generateCosmicHeart();
            case "DARK_MATTER" -> generateDarkMatterStar();
            case "PLANCK" -> generatePlanckStar();
            // 默认：黄矮星（太阳）
            default -> generateYellowDwarf();
        };
    }

    // ==================== 绘制工具方法 ====================

    /** 填充径向渐变圆 */
    private static void fillRadialGradient(int[] pixels, int radius, int centerColor, int edgeColor) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= radius) {
                    float t = (float) (dist / radius);
                    pixels[y * SIZE + x] = StarColors.lerpColor(centerColor, edgeColor, t);
                }
            }
        }
    }

    /** 填充均匀色圆 */
    private static void fillSolidCircle(int[] pixels, int radius, int color) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= radius) {
                    pixels[y * SIZE + x] = color;
                }
            }
        }
    }

    /** 画线（Bresenham 简化版） */
    private static void drawLine(int[] pixels, int x0, int y0, int x1, int y1, int width, int color) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int halfWidth = width / 2;
        while (true) {
            for (int w = -halfWidth; w <= halfWidth; w++) {
                for (int h = -halfWidth; h <= halfWidth; h++) {
                    int px = x0 + w, py = y0 + h;
                    if (px >= 0 && px < SIZE && py >= 0 && py < SIZE) {
                        pixels[py * SIZE + px] = color;
                    }
                }
            }
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    /** 绘制星芒 */
    private static void drawSpikes(int[] pixels, int maxRadius, int spikeWidth, int color, int count) {
        double angleStep = 2 * Math.PI / count;
        for (int i = 0; i < count; i++) {
            double angle = angleStep * i;
            int ex = CENTER + (int) (maxRadius * Math.cos(angle));
            int ey = CENTER + (int) (maxRadius * Math.sin(angle));
            drawLine(pixels, CENTER, CENTER, ex, ey, spikeWidth, color);
        }
    }

    /** 绘制椭圆 */
    private static void fillEllipse(int[] pixels, int cx, int cy, int rx, int ry, int color) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dx = (x - cx) / (double) rx;
                double dy = (y - cy) / (double) ry;
                if (dx * dx + dy * dy <= 1.0) {
                    pixels[y * SIZE + x] = color;
                }
            }
        }
    }

    /** 旋转坐标 */
    private static double[] rotate(double x, double y, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        return new double[]{x * cos - y * sin, x * sin + y * cos};
    }

    // ==================== 主序星 ====================

    private static int[] generateRedDwarf() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 30, StarColors.COLOR_RED_DWARF_CENTER, StarColors.COLOR_RED_DWARF_EDGE);
        return pixels;
    }

    private static int[] generateOrangeDwarf() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 35, StarColors.COLOR_ORANGE_DWARF_CENTER, StarColors.COLOR_ORANGE_DWARF_EDGE);
        return pixels;
    }

    private static int[] generateYellowDwarf() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 38, StarColors.COLOR_YELLOW_DWARF_CENTER, StarColors.COLOR_YELLOW_DWARF_EDGE);
        return pixels;
    }

    private static int[] generateFStar() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 40, StarColors.COLOR_F_STAR_CENTER, StarColors.COLOR_F_STAR_EDGE);
        return pixels;
    }

    private static int[] generateAStar() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 42, StarColors.COLOR_A_STAR_CENTER, StarColors.COLOR_A_STAR_EDGE);
        return pixels;
    }

    private static int[] generateBStar() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 45, StarColors.COLOR_B_STAR_CENTER, StarColors.COLOR_B_STAR_EDGE);
        return pixels;
    }

    private static int[] generateOStar() {
        int[] pixels = new int[SIZE * SIZE];
        // 圆
        fillRadialGradient(pixels, 50, StarColors.COLOR_O_STAR_CENTER, StarColors.COLOR_O_STAR_EDGE);
        // 8条星芒
        drawSpikes(pixels, 70, 3, StarColors.COLOR_O_STAR_SPIKE, 8);
        return pixels;
    }

    // ==================== 棕矮星 ====================

    private static int[] generateLDwarf() {
        return fillSolidColor(25, StarColors.COLOR_L_DWARF);
    }

    private static int[] generateTDwarf() {
        return fillSolidColor(22, StarColors.COLOR_T_DWARF);
    }

    private static int[] generateYDwarf() {
        return fillSolidColor(18, StarColors.COLOR_Y_DWARF);
    }

    private static int[] fillSolidColor(int radius, int color) {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, radius, color);
        return pixels;
    }

    // ==================== 致密星 ====================

    private static int[] generateWhiteDwarf() {
        int[] pixels = new int[SIZE * SIZE];
        // 核心纯白
        fillSolidCircle(pixels, 10, StarColors.COLOR_WHITE_DWARF_CORE);
        // 光晕（透明度衰减）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 10 && dist <= 20) {
                    float t = (float) ((dist - 10) / 10);
                    int alpha = (int) (200 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(StarColors.COLOR_WHITE_DWARF_HALO, alpha);
                    }
                }
            }
        }
        return pixels;
    }

    private static int[] generateNeutronStar() {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, 6, StarColors.COLOR_NEUTRON_CORE);
        // 两条对角脉冲线
        drawLine(pixels, CENTER, CENTER, CENTER - 40, CENTER - 40, 2, StarColors.COLOR_NEUTRON_PULSE);
        drawLine(pixels, CENTER, CENTER, CENTER + 40, CENTER + 40, 2, StarColors.COLOR_NEUTRON_PULSE);
        return pixels;
    }

    private static int[] generateQuarkStar() {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, 12, StarColors.COLOR_QUARK_CORE);
        // 绿色晕
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 12 && dist <= 18) {
                    float t = (float) ((dist - 12) / 6);
                    int alpha = (int) (200 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(StarColors.COLOR_QUARK_HALO, alpha);
                    }
                }
            }
        }
        return pixels;
    }

    private static int[] generateQStar() {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, 18, StarColors.COLOR_QSTAR_CORE);
        // 暗环
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 22 && dist <= 26) {
                    pixels[y * SIZE + x] = StarColors.COLOR_QSTAR_RING;
                }
            }
        }
        return pixels;
    }

    private static int[] generateElectroweakStar() {
        int[] pixels = new int[SIZE * SIZE];
        pixels[CENTER * SIZE + CENTER] = StarColors.COLOR_ELECTROWEAK_CORE;
        // 4条对称弧线
        for (int deg = 0; deg < 360; deg += 90) {
            double rad = Math.toRadians(deg);
            for (int r = 10; r <= 20; r++) {
                int x = CENTER + (int) (r * Math.cos(rad));
                int y = CENTER + (int) (r * Math.sin(rad));
                if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
                    pixels[y * SIZE + x] = StarColors.COLOR_ELECTROWEAK_ARC;
                }
            }
        }
        return pixels;
    }

    private static int[] generateBosonStar() {
        int[] pixels = new int[SIZE * SIZE];
        // 半透明大圆
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= 40) {
                    pixels[y * SIZE + x] = StarColors.COLOR_BOSON_CORE;
                }
            }
        }
        // 同心波纹
        for (int r : new int[]{10, 20, 30}) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    double dist = Math.hypot(x - CENTER, y - CENTER);
                    if (Math.abs(dist - r) < 1.5) {
                        pixels[y * SIZE + x] = StarColors.COLOR_BOSON_RING;
                    }
                }
            }
        }
        return pixels;
    }

    // ==================== 星系核 ====================

    private static int[] generateSeyfert() {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, 12, StarColors.COLOR_SEYFERT_CORE);
        // 两条对称旋臂
        drawLine(pixels, CENTER, CENTER, CENTER + 36, CENTER + 24, 4, StarColors.COLOR_SEYFERT_ARM);
        drawLine(pixels, CENTER, CENTER, CENTER - 36, CENTER - 24, 4, StarColors.COLOR_SEYFERT_ARM);
        return pixels;
    }

    private static int[] generateBlazar() {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, 10, StarColors.COLOR_BLAZAR_CORE);
        // 窄喷流朝右下
        for (int t = 0; t <= 56; t++) {
            int x = CENTER + t;
            int y = CENTER + t;
            int w = 2 + t / 7; // 宽度渐增
            for (int dw = -w / 2; dw <= w / 2; dw++) {
                int px = x + dw, py = y;
                if (px >= 0 && px < SIZE && py >= 0 && py < SIZE) {
                    float fade = 1.0f - (float) t / 56;
                    int alpha = (int) (255 * fade);
                    pixels[py * SIZE + px] = StarColors.withAlpha(StarColors.COLOR_BLAZAR_JET, alpha);
                }
            }
        }
        return pixels;
    }

    private static int[] generateQuasar() {
        int[] pixels = new int[SIZE * SIZE];
        // 核心纯白
        fillSolidCircle(pixels, 8, StarColors.COLOR_QUASAR_CORE);
        // 倾斜椭圆吸积盘
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double[] rot = rotate(x - CENTER, y - CENTER, 30);
                double dx = rot[0] / 50, dy = rot[1] / 18;
                if (dx * dx + dy * dy <= 1.0) {
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    int color;
                    if (dist < 0.33) color = StarColors.COLOR_QUASAR_DISK_INNER;
                    else if (dist < 0.66) color = StarColors.COLOR_QUASAR_DISK_MID;
                    else color = StarColors.COLOR_QUASAR_DISK_OUTER;
                    pixels[y * SIZE + x] = color;
                }
            }
        }
        // 垂直喷流
        for (int y = 30; y >= 10; y--) {
            for (int w = -2; w <= 2; w++) {
                int px = CENTER + w, py = y;
                if (px >= 0 && px < SIZE && py >= 0 && py < SIZE) {
                    pixels[py * SIZE + px] = StarColors.COLOR_QUASAR_JET;
                }
            }
        }
        return pixels;
    }

    // ==================== 巨星/超新星前身 ====================

    private static int[] generateRedSupergiant() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 55, StarColors.COLOR_RED_SUPERGIANT_CENTER, StarColors.COLOR_RED_SUPERGIANT_EDGE);
        // 随机暗斑（20个，基于位置固定）
        int[][] spots = {{10,10,4},{20,30,5},{35,20,3},{45,45,6},{15,50,4},
                {30,10,5},{50,30,3},{10,40,5},{40,15,4},{55,50,5},
                {5,55,3},{25,45,5},{35,50,4},{15,20,5},{45,35,3},
                {20,5,4},{50,10,5},{10,30,3},{30,55,5},{40,40,4}};
        for (int[] spot : spots) {
            int cx = spot[0], cy = spot[1], r = spot[2];
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (Math.hypot(x - cx, y - cy) <= r) {
                        pixels[y * SIZE + x] = StarColors.COLOR_RED_SUPERGIANT_SPOT;
                    }
                }
            }
        }
        return pixels;
    }

    private static int[] generateWolfRayet() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 35, StarColors.COLOR_WR_CORE, 0xFF3C78A0);
        // 气体环（透明度衰减）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 45 && dist <= 60) {
                    float t = (float) ((dist - 45) / 15);
                    int alpha = (int) (120 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(StarColors.COLOR_WR_HALO, alpha);
                    }
                }
            }
        }
        return pixels;
    }

    // ==================== 行星 ====================

    private static int[] generateHotJupiter() {
        int[] pixels = new int[SIZE * SIZE];
        // 椭圆
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dx = (x - CENTER) / 50.0, dy = (y - CENTER) / 35.0;
                if (dx * dx + dy * dy <= 1.0) {
                    // 横向条纹，每5px交替
                    boolean band = ((x - CENTER) / 5) % 2 == 0;
                    pixels[y * SIZE + x] = band ? StarColors.COLOR_HOT_JUPITER_BAND1 : StarColors.COLOR_HOT_JUPITER_BAND2;
                }
            }
        }
        return pixels;
    }

    private static int[] generateSuperEarth() {
        int[] pixels = new int[SIZE * SIZE];
        // 地球风格：蓝+绿随机斑块
        java.util.Random rng = new java.util.Random(42);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= 30) {
                    boolean isLand = rng.nextDouble() < 0.35;
                    pixels[y * SIZE + x] = isLand ? StarColors.COLOR_SUPER_EARTH_GREEN : StarColors.COLOR_SUPER_EARTH_BLUE;
                }
            }
        }
        return pixels;
    }

    // ==================== 特殊事件 ====================

    private static int[] generateBlueStraggler() {
        int[] pixels = new int[SIZE * SIZE];
        fillRadialGradient(pixels, 35, StarColors.COLOR_BLUE_STRAGGLER_CENTER, StarColors.COLOR_BLUE_STRAGGLER_EDGE);
        // 微弱光晕
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 35 && dist <= 45) {
                    float t = (float) ((dist - 35) / 10);
                    int alpha = (int) (60 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(StarColors.COLOR_BLUE_STRAGGLER_EDGE, alpha);
                    }
                }
            }
        }
        return pixels;
    }

    private static int[] generateSupernova() {
        int[] pixels = new int[SIZE * SIZE];
        // 中心白点
        fillSolidCircle(pixels, 5, StarColors.COLOR_SUPERNOVA);
        // 六角星芒
        drawSpikes(pixels, 70, 4, StarColors.COLOR_SUPERNOVA, 6);
        return pixels;
    }

    private static int[] generateHypernova() {
        int[] pixels = new int[SIZE * SIZE];
        fillSolidCircle(pixels, 6, StarColors.COLOR_HYPERNOVA_INNER);
        // 双环
        for (int r : new int[]{20, 40}) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    double dist = Math.hypot(x - CENTER, y - CENTER);
                    if (Math.abs(dist - r) < 2) {
                        int color = r == 20 ? StarColors.COLOR_HYPERNOVA_INNER : StarColors.COLOR_HYPERNOVA_OUTER;
                        pixels[y * SIZE + x] = color;
                    }
                }
            }
        }
        // 8条宽星芒
        drawSpikes(pixels, 64, 5, StarColors.COLOR_HYPERNOVA_OUTER, 8);
        return pixels;
    }

    private static int[] generateGRB() {
        int[] pixels = new int[SIZE * SIZE];
        // 两个对称窄三角形喷流（水平方向）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int dx = Math.abs(x - CENTER);
                int dy = Math.abs(y - CENTER);
                if (dy < 8 && dx > 10 && dx < 60) {
                    float fade = 1.0f - (dx - 10) / 50.0f;
                    int alpha = (int) (255 * fade);
                    pixels[y * SIZE + x] = StarColors.withAlpha(StarColors.COLOR_GRB, alpha);
                }
            }
        }
        return pixels;
    }

    // ==================== 黑洞 ====================

    private static int[] generateStellarBlackHole() {
        int[] pixels = new int[SIZE * SIZE];
        // 黑色圆（视界）
        fillSolidCircle(pixels, 25, 0xFF000000);
        // 倾斜薄吸积盘（椭圆，倾斜20°）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double[] rot = rotate(x - CENTER, y - CENTER, 20);
                double dx = rot[0] / 45.0, dy = rot[1] / 12.0;
                if (dx * dx + dy * dy <= 1.0 && Math.hypot(x - CENTER, y - CENTER) > 25) {
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    int color = StarColors.lerpColor(StarColors.COLOR_BH_DISK_INNER, StarColors.COLOR_BH_DISK_OUTER, (float) dist);
                    pixels[y * SIZE + x] = color;
                }
            }
        }
        return pixels;
    }

    private static int[] generateSupermassiveBlackHole() {
        int[] pixels = new int[SIZE * SIZE];
        // 黑色圆
        fillSolidCircle(pixels, 30, 0xFF000000);
        // 彩色环状吸积盘（半径30~60）
        java.util.Random rng = new java.util.Random(12345);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 30 && dist <= 60) {
                    float t = (float) ((dist - 30) / 30);
                    int color;
                    if (t < 0.25) color = StarColors.COLOR_SMBH_DISK_INNER;
                    else if (t < 0.5) color = StarColors.COLOR_SMBH_DISK_MID;
                    else if (t < 0.75) color = StarColors.COLOR_SMBH_DISK_WARM;
                    else color = StarColors.COLOR_SMBH_DISK_OUTER;
                    // 随机噪点模拟湍流
                    if (rng.nextDouble() < 0.15) {
                        color = 0xFFFFFFFF;
                    }
                    pixels[y * SIZE + x] = color;
                }
            }
        }
        return pixels;
    }

    // ==================== 反物质星体纹理 ====================
    // 反物质星体：紫色/粉色系，色相旋转 180°，带发光光环

    private static int[] generateAntimatterStar() {
        int[] pixels = new int[SIZE * SIZE];
        // 紫色径向渐变（中心亮紫 → 边缘暗粉）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= 35) {
                    float t = (float) (dist / 35);
                    int center = 0xFFFF66FF; // 亮紫
                    int edge = 0xFF9933CC;   // 暗粉
                    pixels[y * SIZE + x] = StarColors.lerpColor(center, edge, t);
                } else if (dist <= 45) {
                    // 发光光环（半透明紫）
                    float t = (float) ((dist - 35) / 10);
                    int alpha = (int) (120 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(0xFFFF66FF, alpha);
                    }
                }
            }
        }
        return pixels;
    }

    private static int[] generateAntimatterBlackHole() {
        int[] pixels = new int[SIZE * SIZE];
        // 黑色圆
        fillSolidCircle(pixels, 25, 0xFF000000);
        // 紫色吸积盘
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist > 25 && dist <= 50) {
                    float t = (float) ((dist - 25) / 25);
                    int inner = 0xFFFF66FF; // 内紫
                    int outer = 0xFF6600CC; // 外紫蓝
                    pixels[y * SIZE + x] = StarColors.lerpColor(inner, outer, t);
                }
            }
        }
        return pixels;
    }

    // ==================== 寰宇之心纹理 ====================
    // ∞符号图标，8层绘制：背景→外晕→内辉→∞符号发光描边→∞符号主体→核心亮点→环绕粒子→粒子光晕

    private static int[] generateCosmicHeart() {
        int[] pixels = new int[SIZE * SIZE];
        int cx = CENTER, cy = CENTER;

        // 第1层：背景（纯黑 → 底部极淡深紫渐变）
        fillSolidCircle(pixels, 64, 0xFF000000);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - cx, y - cy);
                if (dist > 60 && dist <= 64) {
                    float t = (float) ((dist - 60) / 4);
                    int alpha = (int) (17 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(0x00080020, alpha);
                    }
                }
            }
        }

        // 第2层：最外微光（半径62，淡紫）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - cx, y - cy);
                if (dist <= 62) {
                    float t = (float) (dist / 62);
                    int alpha = (int) (17 * (1 - t));
                    if (alpha > 0) {
                        pixels[y * SIZE + x] = StarColors.withAlpha(0x0088AAFF, alpha);
                    }
                }
            }
        }

        // 第3层：外晕（半径35~55，白→紫→金三层渐变环）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - cx, y - cy);
                if (dist > 35 && dist <= 55) {
                    float t = (float) ((dist - 35) / 20);
                    int color;
                    if (t < 0.33) {
                        color = 0x22FFFFFF; // 白
                    } else if (t < 0.66) {
                        color = 0x22AA88FF; // 紫
                    } else {
                        color = 0x44FFD700; // 金
                    }
                    pixels[y * SIZE + x] = color;
                }
            }
        }

        // 第4层：内辉（半径20，金色→紫色径向渐变）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - cx, y - cy);
                if (dist <= 20) {
                    float t = (float) (dist / 20);
                    pixels[y * SIZE + x] = StarColors.lerpColor(0x44FFD700, 0x22AA88FF, t);
                }
            }
        }

        // 第5~7层：∞符号（发光描边 + 主体 + 核心亮点）
        drawInfinitySymbol(pixels, cx, cy, 28, 0x66FFFFFF, 4);  // 发光描边
        drawInfinitySymbolGradient(pixels, cx, cy, 28, 0xFFFFFFFF, 0xFFE0D0FF, 1);  // 主体
        // 核心亮点
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int px = cx + dx, py = cy + dy;
                if (px >= 0 && px < SIZE && py >= 0 && py < SIZE && Math.hypot(dx, dy) <= 2) {
                    pixels[py * SIZE + px] = 0xFFFFFFFF;
                }
            }
        }

        // 第8层：12颗环绕粒子（椭圆轨道，金/粉交替）
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6; // 30°
            double px = cx + 50 * Math.cos(angle);
            double py = cy + 30 * Math.sin(angle);
            int color = (i % 2 == 0) ? 0xFFFFDD44 : 0xFFFF66AA; // 金/粉交替
            // 粒子光晕（半径5px）
            for (int dy = -5; dy <= 5; dy++) {
                for (int dx = -5; dx <= 5; dx++) {
                    int sx = (int) px + dx, sy = (int) py + dy;
                    if (sx >= 0 && sx < SIZE && sy >= 0 && sy < SIZE) {
                        double d = Math.hypot(dx, dy);
                        if (d <= 5) {
                            int alpha = (int) (34 * (1 - d / 5));
                            pixels[sy * SIZE + sx] = StarColors.withAlpha(0x00FFFFDD, alpha);
                        }
                    }
                }
            }
            // 粒子核心（半径2px）
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    int sx = (int) px + dx, sy = (int) py + dy;
                    if (sx >= 0 && sx < SIZE && sy >= 0 && sy < SIZE && Math.hypot(dx, dy) <= 2) {
                        pixels[sy * SIZE + sx] = color;
                    }
                }
            }
        }
        return pixels;
    }

    // ==================== 辅助绘制函数 ====================

    /** 绘制∞符号（指定宽度和颜色） */
    private static void drawInfinitySymbol(int[] pixels, int cx, int cy, int size, int color, int thickness) {
        for (double t = 0; t < 2 * Math.PI; t += 0.005) {
            double sinT = Math.sin(t);
            double cosT = Math.cos(t);
            double denom = 1 + sinT * sinT;
            double x = cx + size * cosT / denom;
            double y = cy + size * sinT * cosT / denom;
            for (int dx = -thickness / 2; dx <= thickness / 2; dx++) {
                for (int dy = -thickness / 2; dy <= thickness / 2; dy++) {
                    int px = (int) x + dx, py = (int) y + dy;
                    if (px >= 0 && px < SIZE && py >= 0 && py < SIZE) {
                        pixels[py * SIZE + px] = color;
                    }
                }
            }
        }
    }

    /** 绘制∞符号（渐变颜色，从中心到边缘渐变） */
    private static void drawInfinitySymbolGradient(int[] pixels, int cx, int cy, int size, int centerColor, int edgeColor, int thickness) {
        for (double t = 0; t < 2 * Math.PI; t += 0.005) {
            double sinT = Math.sin(t);
            double cosT = Math.cos(t);
            double denom = 1 + sinT * sinT;
            double x = cx + size * cosT / denom;
            double y = cy + size * sinT * cosT / denom;
            // 根据到中心的距离计算渐变
            double dist = Math.hypot(x - cx, y - cy) / size;
            float fade = (float) Math.min(1, dist);
            int color = StarColors.lerpColor(centerColor, edgeColor, fade);
            for (int dx = -thickness / 2; dx <= thickness / 2; dx++) {
                for (int dy = -thickness / 2; dy <= thickness / 2; dy++) {
                    int px = (int) x + dx, py = (int) y + dy;
                    if (px >= 0 && px < SIZE && py >= 0 && py < SIZE) {
                        pixels[py * SIZE + px] = color;
                    }
                }
            }
        }
    }
    // ==================== 暗物质星 / 普朗克星 纹理 ====================

    private static int[] generateDarkMatterStar() {
        int[] pixels = new int[SIZE * SIZE];
        // 极暗紫色，近乎黑色，边缘有微弱的暗紫色晕
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= 30) {
                    float t = (float)dist / 30f;
                    int c = StarColors.lerpColor(0xFF080818, 0xFF1A1A2E, t);
                    pixels[y * SIZE + x] = c;
                } else if (dist <= 40) {
                    float t = (float) ((dist - 30) / 10);
                    int alpha = (int) (60 * (1 - t));
                    if (alpha > 0) pixels[y * SIZE + x] = StarColors.withAlpha(0xFF1A1A2E, alpha);
                }
            }
        }
        return pixels;
    }

    private static int[] generatePlanckStar() {
        int[] pixels = new int[SIZE * SIZE];
        // 极小核心 + 极强光晕（普朗克温度 ~1.42e32K，极端亮白）
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = Math.hypot(x - CENTER, y - CENTER);
                if (dist <= 2) {
                    pixels[y * SIZE + x] = 0xFFFFFFFF;
                } else if (dist <= 6) {
                    float t = (float) ((dist - 2) / 4);
                    pixels[y * SIZE + x] = StarColors.lerpColor(0xFFFFFFFF, 0xFFFFCC88, t);
                } else if (dist <= 20) {
                    float t = (float) ((dist - 6) / 14);
                    pixels[y * SIZE + x] = StarColors.lerpColor(0xFFFFCC88, 0xFF4488CC, t);
                } else if (dist <= 35) {
                    float t = (float) ((dist - 20) / 15);
                    int alpha = (int) (80 * (1 - t));
                    if (alpha > 0) pixels[y * SIZE + x] = StarColors.withAlpha(0xFF4488CC, alpha);
                }
            }
        }
        return pixels;
    }
}

