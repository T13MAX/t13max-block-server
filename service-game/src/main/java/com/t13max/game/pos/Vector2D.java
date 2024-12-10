package com.t13max.game.pos;


import com.t13max.game.utils.Utils;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Vector2D {

    //横坐标
    public final double x;

    //纵坐标
    public final double z;

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public Vector2D() {
        this(0.0D, 0.0D);
    }

    /**
     * 构造函数
     * @param x
     * @param z
     */
    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    /**
     * 转换为三维float型数组
     */
    public float[] toFloat3() {
        return new float[]{(float)x, 0, (float)z};
    }

    /**
     * 导航网格的
     * @return
     */
    public float[] toDetourFloat3() {
        return new float[]{(float)z, 0, (float)x};
    }

    public Vector2D sub(Vector2D vector) {
        return new Vector2D(this.x - vector.x, this.z - vector.z);
    }

    public Vector2D sum(Vector2D vector) {
        return new Vector2D(this.x + vector.x, this.z + vector.z);
    }

    public Vector2D mul(Vector2D vector) {
        return new Vector2D(this.x * vector.x, this.z * vector.z);
    }

    public Vector2D mul(double a) {
        return new Vector2D(this.x * a, this.z * a);
    }

    public Vector2D div(double a) {
        return new Vector2D(this.x / a, this.z / a);
    }

    /**
     * 向量归一化
     * @return
     */
    public Vector2D normalize() {
        Vector2D result = new Vector2D();
        double dis = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.z, 2));
        return new Vector2D(this.x / dis, this.z / dis);
    }

    /**
     * 向量点乘
     * @param vector
     * @return
     */
    public double dot(Vector2D vector) {
        return this.x * vector.x + this.z * vector.z;
    }

    /**
     * 向量叉乘
     * @param other
     * @return
     */
    public double cross(Vector2D other) {
        return this.x * other.z - this.z * other.x;
    }

    /**
     * 求法向量
     * @return
     */
    public Vector2D skew() {
        return new Vector2D(-z, x);
    }

    /**
     * 获取易读的坐标字符串
     * @return
     */
    public String getPosStr() {
        return new StringBuilder("(").append(x).append(",").append(z).append(")").toString();
    }

    /**
     * 两点之间的距离
     * @param pos
     * @return
     */
    public double distance(Vector2D pos) {
        return Math.sqrt(distSquare(pos));
    }

    /**
     * 两点之间距离的平方
     * @param pos
     * @return
     */
    public double distSquare(Vector2D pos) {
        double dx = this.x - pos.x;
        double dz = this.z - pos.z;
        return dx * dx + dz * dz;
    }

    /**
     * 计算向量的长度
     * @return
     */
    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    // 判断两个点的坐标是否偏移一定距离
    public boolean offset(Vector2D pos, double len) {
        if (Math.abs(x - pos.x) <= len && Math.abs(z - pos.z) <= len) {
            return true;
        }
        return false;
    }

    /**
     * 返回pos在我的第几象限
     *    |
     *  2 | 1
     * ---+---
     *  3 | 4
     *    |
     * @param pos
     * @return [1,4]
     */
    public int quadrant(Vector2D pos) {
        double dx = pos.x - this.x;
        double dz = pos.z - this.z;
        if (dx >= 0) {
            if (dz >= 0) {
                return 1;
            } else {
                return 4;
            }
        } else {
            if (dz >= 0) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    /**
     * 计算向量v1逆时针转到v2的角度
     * @param v1
     * @param v2
     * @return
     */
    public static double angle(Vector2D v1, Vector2D v2) {
        double radian;
        // 归一化
        v1 = v1.normalize();
        v2 = v2.normalize();
        // 点乘算夹角
        double dot = v1.dot(v2);
        if(Utils.floatEqual(dot, 1.0)) {
            radian = 0.0;
        } else if(Utils.floatEqual(dot, -1.0)) {
            radian = Math.PI;
        } else {
            radian = Math.acos(dot);
            // 叉乘确定夹角是在第一二象限还是在第三四象限
            double cross = v1.cross(v2);
            // 夹角在第三四象限
            if(cross < 0) {
                radian = 2 * Math.PI - radian;
            }
        }

        return Math.toDegrees(radian);
    }

    public boolean isZero(){
        return this.x == 0 && this.z == 0;
    }

    /**
     * 从start 指向 end的方向 从org点移动 DIS的距离
     * @param start
     * @param end
     * @param dis
     * @return
     */
    public static Vector2D lookAtDis(Vector2D start, Vector2D end, Vector2D org, double dis) {
        Vector2D result = new Vector2D();

        if(end.equals(start)) {
            return start;
        }
        double diffX = end.x - start.x;
        double diffZ = end.z - start.z;

        //实际距离
        double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffZ, 2));

        //起始至目标的Sin,Cos值
        double tempSin = diffZ / diffTrue;
        double tempCos = diffX / diffTrue;

        double dX = tempCos * dis;
        double dZ = tempSin * dis;

        return new Vector2D(org.x + dX, org.z + dZ);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(x).append(",").append(z).append("]").toString();
    }
}
