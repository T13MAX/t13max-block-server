package com.t13max.game.pos;

import lombok.EqualsAndHashCode;
import world.entity.PBVector3D;

import java.util.List;

/**
 * 3D向量
 *
 * @Author t13max
 * @Date 15:02 2024/12/10
 */
@EqualsAndHashCode
public class Vector3D {

    private static double EPSILON = 1.0e-6;

    //X坐标
    public final double x;

    //Y坐标
    public final double y;

    //Z坐标
    public final double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D() {
        this(0.0D, 0.0D, 0.0D);
    }

    public Vector3D(Vector3D vector3D) {
        this(vector3D.x, vector3D.y, vector3D.z);
    }

    public Vector3D(PBVector3D pbVector3) {
        this(pbVector3.getX(), pbVector3.getY(), pbVector3.getZ());
    }

    /**
     * 数组转向量
     *
     * @Author t13max
     * @Date 15:07 2024/12/10
     */
    public static Vector3D create(float[] pos) {
        if (pos.length < 3) {
            throw new IllegalArgumentException("Vector3D must contains x、y、z");
        }
        return new Vector3D(pos[0], pos[1], pos[2]);
    }

    /**
     * list转向量
     *
     * @Author t13max
     * @Date 15:07 2024/12/10
     */
    public static Vector3D create(List<Float> pos) {
        if (pos.size() < 3) {
            throw new IllegalArgumentException("Vector3D must contains x、y、z");
        }
        return new Vector3D(pos.get(0), pos.get(1), pos.get(2));
    }

    public static Vector3D create(PBVector3D pos) {
        return new Vector3D(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * 通过整型数组创建，数组的值为源坐标乘100得到的整数
     *
     * @Author t13max
     * @Date 15:18 2024/12/10
     */
    public static Vector3D createByIntArray(int[] pos) {
        if (pos.length < 3) {
            throw new IllegalArgumentException("Vector3D must contains x、y、z");
        }
        return new Vector3D(pos[0] / 100.0D, pos[1] / 100.0D, pos[2] / 100.0D);
    }

    /**
     * Vector3D -> DVector3
     *
     * @return
     */
    public PBVector3D toMsg() {
        PBVector3D.Builder msg = PBVector3D.newBuilder();
        msg.setX((float) x);
        msg.setY((float) y);
        msg.setZ((float) z);

        return msg.build();
    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    /**
     * 导航网格的
     * xyz 转到 detour 的格式
     *
     * @return
     */
    public float[] toDetourFloat3() {
        //return new float[]{(float)z, (float)y, (float)x};
        return new float[]{(float) -x, (float) y, (float) z};
    }

    public Vector3D toServFromDetour() {
        //return new Vector3D(z, y, x);
        return new Vector3D(-x, y, z);
    }

    public List<Double> toDoubleList() {
        return List.of(x, y, z);
    }

    public List<Float> toFloatList() {
        return List.of((float) x, (float) y, (float) z);
    }


    public Vector2D toVector2D() {
        return new Vector2D(x, z);
    }

    public Vector3D clone() {
        return new Vector3D(this.x, this.y, this.z);
    }

    /**
     * 两点之间的距离，忽视高度
     *
     * @Author t13max
     * @Date 15:20 2024/12/10
     */
    public double distance(Vector3D pos) {
        double t1x = this.x;
        double t1z = this.z;
        double t2x = pos.x;
        double t2z = pos.z;

        return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1z - t2z), 2));
    }

    /**
     * 两点之间距离的平方，忽视高度
     *
     * @Author t13max
     * @Date 15:20 2024/12/10
     */
    public double distance2DSquared(Vector3D pos) {
        double dx = this.x - pos.x;
        double dz = this.z - pos.z;

        return dx * dx + dz * dz;
    }

    /**
     * 两点之间距离的平方，忽视高度
     *
     * @Author t13max
     * @Date 15:20 2024/12/10
     */
    public double distance2DSquared(Vector2D pos) {
        double dx = this.x - pos.x;
        double dz = this.z - pos.z;

        return dx * dx + dz * dz;
    }

    /**
     * 两点之间的距离,带高度
     *
     * @Author t13max
     * @Date 15:21 2024/12/10
     */
    public double distance3D(Vector3D pos) {
        double t1x = this.x;
        double t1y = this.y;
        double t1z = this.z;
        double t2x = pos.x;
        double t2y = pos.y;
        double t2z = pos.z;

        return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1y - t2y), 2) + Math.pow((t1z - t2z), 2));
    }

    /**
     * 两点之间距离的平方
     *
     * @Author t13max
     * @Date 15:21 2024/12/10
     */
    public double distanceSquared(Vector3D pos) {
        double dx = this.x - pos.x;
        double dy = this.y - pos.y;
        double dz = this.z - pos.z;

        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * 坐标高度差
     *
     * @Author t13max
     * @Date 15:21 2024/12/10
     */
    public double diffHeight(Vector3D pos) {

        return Math.abs(y - pos.y);
    }

    /**
     * 向量点乘-2D
     *
     * @Author t13max
     * @Date 15:21 2024/12/10
     */
    public double dot2D(Vector3D pos) {
        return this.x * pos.x + this.z * pos.z;
    }

    /**
     * 向量点乘-3D
     *
     * @Author t13max
     * @Date 15:21 2024/12/10
     */
    public double dot3D(Vector3D pos) {
        return this.x * pos.x + this.y * pos.y + this.z * pos.z;
    }

    /**
     * 向量叉乘
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D cross(Vector3D other) {
        return new Vector3D(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
    }

    /**
     * 向量减法
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D sub(Vector3D pos) {
        return new Vector3D(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    /**
     * 向量加法
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D sum(Vector3D pos) {
        return new Vector3D(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    /**
     * 向量乘以一个数
     *
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D mul(double multi) {
        return new Vector3D(this.x * multi, this.y * multi, this.z * multi);
    }

    /**
     * 向量除以一个数
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D div(double div) {
        return new Vector3D(this.x / div, this.y / div, this.z / div);
    }

    /**
     * 求法向量
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D skew() {
        return new Vector3D(-z, y, x);
    }

    /**
     * 向量归一化
     * @Author t13max
     * @Date 15:22 2024/12/10
     */
    public Vector3D normalize() {
        return normalize(false);
    }

    /**
     * 向量归一化
     * @Author t13max
     * @Date 15:23 2024/12/10
     */
    public Vector3D normalize(boolean isNeedY) {
        double y = isNeedY ? this.y : 0.0D;
        double dis = Math.sqrt(x * x + y * y + z * z);
        if (Double.doubleToLongBits(dis) == 0L) {
            return null;
        }
        return new Vector3D(x / dis, y / dis, z / dis);
    }

    public double getNormalize() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * 获得方向相同模为r的向量
     * @Author t13max
     * @Date 15:23 2024/12/10
     */
    public static Vector3D getmR(Vector3D v, double r) {
        double x = v.x;
        double y = v.y;
        double z = v.z;
        double sum = x * x + y * y + z * z;
        double k = Math.sqrt(sum / (r * r));
        x = x / k;
        y = y / k;
        z = z / k;
        return new Vector3D(x, y, z);
    }

    /**
     * 向量旋转 center旋转轴 vector旋转向量 degree旋转角度
     * @Author t13max
     * @Date 15:23 2024/12/10
     */
    public static Vector3D getRotate(Vector3D center, Vector3D vector, double degree) {
        double radian = Math.toRadians(degree);
        center = getmR(center, 1.0);
        Double x = center.x;
        Double y = center.y;
        Double z = center.z;
        Matrix a = new Matrix(3, 3);
        Matrix b = new Matrix(3, 3);
        Matrix i = new Matrix(3, 3);
        double[][] A = new double[3][3];
        A[0][0] = x * x;
        A[0][1] = x * y;
        A[0][2] = x * z;
        A[1][0] = y * x;
        A[1][1] = y * y;
        A[1][2] = y * z;
        A[2][0] = z * x;
        A[2][1] = z * y;
        A[2][2] = z * z;
        double[][] B = new double[3][3];
        B[0][0] = 0;
        B[0][1] = -z;
        B[0][2] = y;
        B[1][0] = z;
        B[1][1] = 0;
        B[1][2] = -x;
        B[2][0] = -y;
        B[2][1] = x;
        B[2][2] = 0;
        double[][] I = new double[3][3];
        I[0][0] = 1;
        I[0][1] = 0;
        I[0][2] = 0;
        I[1][0] = 0;
        I[1][1] = 1;
        I[1][2] = 0;
        I[2][0] = 0;
        I[2][1] = 0;
        I[2][2] = 1;
        a.init(A);
        b.init(B);
        i.init(I);
        Matrix m = new Matrix(3, 3);
        m = i.Minus(a);
        m = m.mMultiply(Math.cos(radian));
        Matrix t = new Matrix(3, 3);
        t = b;
        t = t.mMultiply(Math.sin(radian));
        m = m.Plus(t);
        m = m.Plus(a);
        m = m.Trans();
        t = new Matrix(3, 1);
        double[][] T = new double[3][1];

        T[0][0] = vector.x;
        T[1][0] = vector.y;
        T[2][0] = vector.z;
        t.init(T);
        m = m.Multiply(t);
        m = m.Trans();
        return new Vector3D(m.mat[0][0], m.mat[0][1], m.mat[0][2]);
    }

    /**
     * 零向量(向量的x,y,z都为零)
     *
     * @return
     */
    public boolean isZero() {
        return (x > -EPSILON && x < EPSILON)
                && (y > -EPSILON && y < EPSILON)
                && (z > -EPSILON && z < EPSILON);
    }

    /**
     * 零向量(向量的x,z都为零)
     *
     * @return
     */
    public boolean isZero2D() {
        return (x > -EPSILON && x < EPSILON)
                && (z > -EPSILON && z < EPSILON);
    }

    public boolean isLegal() {
        return isLegal(x) && isLegal(y) && isLegal(z);
    }

    private boolean isLegal(double val) {
        return !Double.isInfinite(val) && !Double.isNaN(val);
    }

    /**
     * 计算向量v1逆时针转到v2的角度(2D)
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double angle2D(Vector3D v1, Vector3D v2) {
        Vector2D tv1 = new Vector2D(v1.x, v1.z);
        Vector2D tv2 = new Vector2D(v2.x, v2.z);

        return Vector2D.angle(tv1, tv2);
    }

    /**
     * 计算向量v1逆时针转到v2的角度(3D)
     *  TODO 此方法有问题
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double angle3D(Vector3D v1, Vector3D v2) {
        double normProduct = v1.getNormalize() * v2.getNormalize();
        double dot = v1.dot3D(v2);
        double threshold = normProduct * 0.9999;

        if ((dot < -threshold) || (dot > threshold)) {
            Vector3D v3 = v1.cross(v2);
            if (dot >= 0) {
                double radian = Math.asin(v3.getNormalize() / normProduct);
                return Math.toDegrees(radian);
            }

            double radian = Math.PI - Math.asin(v3.getNormalize() / normProduct);
            return Math.toDegrees(radian);
        }

        double radian = Math.acos(dot / normProduct);
        return Math.toDegrees(radian);
    }

    /**
     * 向量按顺时针旋转角度
     *
     * @param degree 角度
     * @return Vector3D
     */
    public Vector3D rotateCW(double degree) {
        double radian = Math.toRadians(degree);

        return rotateRad(radian, true);
    }

    /**
     * 将向量旋转给定弧度(x, z)
     *
     * @param radians 弧度
     * @param cw      true 顺时针 false 逆时针
     * @return Vector3D
     */
    public Vector3D rotateRad(double radians, boolean cw) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double newX, newZ;

        if (cw) {
            newX = this.z * sin + this.x * cos;
            newZ = this.z * cos - this.x * sin;
        } else {
            newX = this.x * cos - this.z * sin;
            newZ = this.x * sin + this.z * cos;
        }

        return new Vector3D(newX, this.y, newZ);
    }

    /**
     * 获取以该坐标为球心球体内随机Vector3D
     *
     * @param radius radius
     * @return Vector3D
     */
    /*public Vector3D random(double radius) {
        float u = GameRandomUtils.nextFloat();
        float v = GameRandomUtils.nextFloat();

        double azimuthalAngle = 2 * Math.PI * u;
        double polarAngle = Math.acos(2 * v - 1);

        double cosAzimuthal = Math.cos(azimuthalAngle);
        double sinAzimuthal = Math.sin(azimuthalAngle);

        double cosPolar = Math.cos(polarAngle);
        double sinPolar = Math.sin(polarAngle);

        Vector3D direction = new Vector3D(cosAzimuthal * sinPolar, sinAzimuthal * sinPolar, cosPolar);
        double randomRadius = radius * GameRandomUtils.nextFloat();

        return this.sum(direction.mul(randomRadius));
    }*/

    /**
     * 获取当前点与目标点的近似距离，不考虑y轴
     *
     * @param o
     * @return
     */
    public double approximateDistanceIgnoreY(final Vector3D o) {
        if (!o.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("不同的坐标类型");
        }

        final double xValue = Math.abs((x - o.getX()));
        final double zValue = Math.abs(z - o.getZ());

        return Math.max(xValue, zValue);
    }

    /**
     * @param o 终点
     * @return 两点间距离
     */
    public double approximateDistance(final Vector3D o) {
        if (!o.getClass().equals(this.getClass())) {
            throw new IllegalArgumentException("不同的坐标类型");
        }
        final double longValue = Math.abs((x - o.getX()));
        final double widthValue = Math.abs(y - o.getY());
        final double highValue = Math.abs(z - o.getZ());

        double tmp = Math.max(longValue, widthValue);
        return Math.max(tmp, highValue);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }
}
