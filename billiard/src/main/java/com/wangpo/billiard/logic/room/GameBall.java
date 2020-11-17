package com.wangpo.billiard.logic.room;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@Data
public class GameBall {
    private int number;
    public double x;
    public double y;
    public double z;

    public double ax;
    public double ay;
    public double az;
    public double aw;

    public double bx;
    public double by;
    public double bz;

    public GameBall(double x,double y){
        this.x = x;
        this.y = y;
    }

    public GameBall(double x,double y,double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

//    public GameProto.Ball toProto() {
//        return GameProto.Ball.newBuilder()
//                .setNumber(number).setX(x).setY(y).build();
//    }

    public double degreesToRadians(double angle){
        return angle * Math.PI/180;
    }

    /**
     * 客户端旋转角度，用于击球验证
     * @param out
     * @param v
     * @param o
     * @param angle
     * @return
     */
    public static GameBall rotateY(GameBall out, GameBall v, GameBall o, double angle) {
        double dx = v.x - o.x;
        double dy = v.y - o.y;
        double dz = v.z - o.z;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double rx = dz * sin + dx * cos;
        double ry = dy;
        double rz = dz * cos - dx * sin;

        // translate to correct position
        out.x = rx + o.x;
        out.y = ry + o.y;
        out.z = rz + o.z;

        return out;
    }

    public static GameBall mul(GameBall ball, double scale) {
        double x = ball.x*scale;
        double y = ball.y*scale;
        double z = ball.z*scale;
        return new GameBall(x,y,z);
    }

    public double getAngle(GameBall target) {
        float diff = 1e-6f;
        //特殊情况判断
        if( Math.abs(target.y-y) <diff && Math.abs(target.x-x) <diff) {
	        return 0;
        }
        if( Math.abs(target.y-y) <diff) {
            return target.x>x?0:180;
        }
        if( Math.abs(target.x-x) <diff) {
            return target.y>y?90:270;
        }
        double a = Math.atan( (target.y - y)/(target.x - x));
        double ret = a * 180 / Math.PI; //弧度转角度，方便调试
        //第二象限
        if(target.y>y && target.x<x) {
            return ret+180;
        }
        //第四象限
        if(target.y<y && target.x>x) {
            return ret+360;
        }
        //第三象限
        if(target.y<y && target.x<x) {
            return ret+180;
        }
        return ret;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
	        return true;
        }
        if (o == null || getClass() != o.getClass()) {
	        return false;
        }
        GameBall gameBall = (GameBall) o;
        return number == gameBall.number &&
                Double.compare(gameBall.x, x) == 0 &&
                Double.compare(gameBall.y, y) == 0 &&
                Double.compare(gameBall.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, x, y, z);
    }

    @Override
    public String toString() {
        return "{number:"+number+":"+x+","+y+"},";
    }
}
