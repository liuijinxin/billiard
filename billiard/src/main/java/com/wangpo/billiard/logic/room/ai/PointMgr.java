package com.wangpo.billiard.logic.room.ai;

import com.wangpo.billiard.logic.room.GameBall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 向量工具类，用于向量计算
 */
@Slf4j
@Component
public class PointMgr {
    /*private static final PointMgr me = new PointMgr();
    private PointMgr(){}
    public static PointMgr me() {
        return me;
    }*/

    public static final double eps=1e-10;
    private static final double r = 1.2;
//    private static final GameBall bag1 = new GameBall(-200,-100);
//    private static final GameBall bag2 = new GameBall(-200,100);
//    private static final GameBall bag3 = new GameBall(0,-100);
//    private static final GameBall bag4 = new GameBall(0,100);
//    private static final GameBall bag5 = new GameBall(200,-100);
//    private static final GameBall bag6 = new GameBall(200,100);
    private static final GameBall[] bags = {
            new GameBall(-35.765,12.744),
            new GameBall(0.02,13.658),
            new GameBall(35.87,12.62),
            new GameBall(35.87,-22.019),
            new GameBall(-0.207,-22.807),
            new GameBall(-35.654,-21.766),
    };


    //向量加
    public static GameBall add(GameBall a, GameBall b) {
        return new GameBall(a.x+b.x,a.y+b.y);
    }

    //向量减
    public static GameBall sub(GameBall a, GameBall b) {
        return new GameBall(a.x-b.x,a.y-b.y);
    }
    //向量乘
    public static GameBall multi(GameBall a, GameBall b) {
        return new GameBall(a.x*b.x,a.y*b.y);
    }
    public static GameBall multi(double d, GameBall a ) {
        return new GameBall(a.x*d,a.y*d);
    }
    //向量除
    public static GameBall div(GameBall a, GameBall b) {
        return new GameBall(a.x/b.x,a.y/b.y);
    }
    //向量相等
    public static boolean equal(GameBall a, GameBall b) {
        return dcmp(a.x-b.x)==0 && dcmp(a.y-b.y)==0;
    }
    //向量点积
    public static double dot(GameBall a, GameBall b) {
        return a.x*b.x+a.y*b.y;
    }
    //向量求模
    public static double length(GameBall a){
        return Math.sqrt(dot(a,a));
    }
    //两个向量求模
    public static double length(GameBall a, GameBall b){
        return length(sub(b,a));
    }
    //向量叉积
    public static double cross(GameBall a, GameBall b) {
        return a.x*b.y-b.x*a.y;
    }
    //极限
    static int dcmp(double x){if(Math.abs(x)<eps) {
	    return 0;
    }
	    return (x>0)?1:-1;}


    public static double distanceBetween(GameBall p1, GameBall p2) {
        return Math.sqrt((p2.y-p1.y)*(p2.y-p1.y)+(p2.x-p1.x)*(p2.x-p1.x));
    }
    /**
     * 点到直线的距离
     * 直线方程：AX+BY+C=0
     * 注意：直线方程公式不适用于与x,y轴平行的情况，所以要特殊处理
     * 点坐标：x,y
     * 距离公式：d = |Ax+By+C|/|srq(A*A+B*B)|
     * @param o
     * @param p1
     * @param p2
     * @return
     */
    public static double distanceToSegment(GameBall o, GameBall p1, GameBall p2) {
        //平行Y轴
        if( p1.x == p2.x ) {
	        return Math.abs(o.x-p1.x);
        }
        //平行X轴
        if( p1.y == p2.y ) {
	        return Math.abs(o.y-p1.y);
        }
        double a = p2.y - p1.y;
        double b = p1.x-p2.x;
        double c =  p1.y*(p2.x-p1.x) - p1.x*(p2.y-p1.y);
        double t = a*o.x+b*o.y+c;
        double up = a*o.x+b*o.y+c;
        double down = a*a+b*b;
        double r =  Math.abs(up)/Math.sqrt(down);
        return r;
        /*if ( a==b) return length(sub(p,a));
        GameBall p1 = sub(b,a);
        GameBall p2 = sub(p,a);
        GameBall p3 = sub(p,b);

        if(dcmp(dot(p1,p2))<0)return length(p2);           //第二类第一小类
        else if(dcmp(dot(p1,p3))>0)return length(p3);      //第二类第二小类
        else return Math.abs(cross(p1,p2))/length(p1);*/
    }
    //求向量夹角
    public static double includedAngle(GameBall a, GameBall b, GameBall c, GameBall d) {
        GameBall p1 = sub(b,a);
        GameBall p2 = sub(d,c);
        return includedAngle(p1,p2);
    }
    //求向量夹角
    public static double includedAngle(GameBall a, GameBall b ) {
        return Math.toDegrees(Math.acos(dot(a,b)/(length(a)*length(b))));
    }

    /**
     * 向量延长线的指定距离的点坐标
     * @param start 向量起点
     * @param end 向量终点
     * @return
     */
    public static GameBall extensionCord(GameBall start, GameBall end, double r){
        double x = (r*(end.x-start.x) + length(start,end)*end.x)/length(start,end);
        double y = (r*(end.y-start.y) + length(start,end)*end.y)/length(start,end);
        return new GameBall(x,y);
    }

    public static double degreeToRadians(double angle) {
        return angle*Math.PI/180;
    }

//    protected final static GameBall[] bags = new GameBall[6];
    public static void main(String[] args) {
//        GameBall a = new GameBall(-7.26,10.46);
//        GameBall b = new GameBall(6.39,0.87);
//        GameBall c = new GameBall(0.38,-20.4);

//        GameBall d =  extensionCord(c,b,r);
//        System.out.println(extensionCord(new GameBall(0,0),new GameBall(0,5),r));
//        System.out.println(extensionCord(new GameBall(0,5),new GameBall(0,0),r));
//        System.out.println(extensionCord(new GameBall(0,0),new GameBall(0,-5),r));
//        System.out.println(extensionCord(new GameBall(0,0),new GameBall(5,0),r));
//        System.out.println(extensionCord(new GameBall(0,0),new GameBall(-5,0),r));

        System.out.println(extensionCord(new GameBall(2,5),new GameBall(-2,-5),r));
        System.out.println(extensionCord(new GameBall(-2,-5),new GameBall(2,5),r));
//        System.out.println(d);
    }

    /**
     * AI摆球
     * 规则：
     * @return
     */
    public static GameBall layBall(List<GameBall> pointList, List<GameBall> targetList, GameBall[] bags2) {
        Map<GameBall, GameBall> targets = new HashMap<>();
        //第一步，选球离洞口小于6米并且连线没有阻挡的球集合A
        for(GameBall target:targetList) {
            GameBall targetBag = null;
            double min = 18;//固定10米
            for(GameBall bag:bags) {
                if( !hasBlock(pointList,bag,target)) {
                    double distance = distanceBetween(target,bag);
                    if( distance < min ) {
//                    min = distance;
                        targetBag = bag;
                    } else {
//                        System.out.println("---"+point.getNumber()+"号球和球袋-"+bag+"-的距离："+distance);
                    }
                } else {
//                    System.out.println("---"+point.getNumber()+"号球和球袋-"+bag+"-有阻挡");
                }
            }
            if( targetBag != null ) {
                targets.put(target,targetBag);
            }
        }
        if( targets.size()> 0 ) {
            //2，从A中选取延长线2米内没有阻挡的目标球进行摆放。
            Iterator<GameBall> it = targets.keySet().iterator();
            while(it.hasNext()) {
                GameBall gameBall =  it.next();
                GameBall bag = targets.get(gameBall);
                GameBall extensionPoint = extensionCord(bag,gameBall,4*r);
                if( bag.getY() >= gameBall.getY() ) {
                    extensionPoint.setY(extensionPoint.getY() - r/2);
                } else {
                    extensionPoint.setY(extensionPoint.getY() + r/2);
                }
                boolean ok = true;
                for(GameBall gb:pointList) {
                    if(distanceBetween(extensionPoint,gb) < (2*r+0.5) ) {
                        ok = false;
                        break;
                    }
                }
                if( ok ) {
//                    log.info("找到摆球点,球袋{},目标球：{}：延长点：{}",bag,gameBall,extensionPoint);
                    return extensionPoint;
                }
            }
        } else {
//            System.out.println("没有找到小于10米的可摆球-目标球");
        }

        //3，没有目标球，或者延长线上有阻挡,从球桌中心往四周随机位置
        for(int k=0;k<29;k+=2) {
            for(int i=0;i<12;i+=2) {
                GameBall gameBall = getGameBall(pointList, k, i);
                if (gameBall != null) {
	                return gameBall;
                }
            }
        }

        for(int k=-29;k<0;k+=2) {
            for(int i=0;i<12;i+=2) {
                GameBall gameBall = getGameBall(pointList, k, i);
                if (gameBall != null) {
	                return gameBall;
                }
            }
        }

        for(int k=-29;k<0;k+=2) {
            for(int i=-12;i<0;i+=2) {
                GameBall gameBall = getGameBall(pointList, k, i);
                if (gameBall != null) {
	                return gameBall;
                }
            }
        }

        for(int k=0;k<29;k+=2) {
            for(int i=-12;i<0;i+=2) {
                GameBall gameBall = getGameBall(pointList, k, i);
                if (gameBall != null) {
	                return gameBall;
                }
            }
        }
        return null;
    }

    private static GameBall getGameBall(List<GameBall> pointList, int k, int i) {
        GameBall gameBall = new GameBall(k,i);
        boolean ok = true;
        for(GameBall gb:pointList) {
            if(distanceBetween(gameBall,gb) < r ) {
                ok = false;
            }
        }
        if( ok ) {
	        return gameBall;
        }
        return null;
    }

    /**
     * 最简单的选球AI，选取最近的球，且中间没有阻挡球
     * @param pointList
     * @param targetList
     * @param white
     * @param bags2
     * @return
     */
    public static double oldAngle(List<GameBall> pointList, List<GameBall> targetList, GameBall white, GameBall[] bags2){
        List<GameBall> aList = new ArrayList<>();
        for (GameBall p:targetList) {
            boolean hasBlock = hasBlock(pointList, white, p);
            if( !hasBlock ) {
	            aList.add(p);
            }
        }

        if( aList.size()>0 ) {
            aList.sort((o1, o2) -> (int) (1000 * (distanceBetween(o1, white) - distanceBetween(o2, white))));
            double min = 90;
            GameBall extension = null;
            GameBall bag2 = null;
            GameBall target2 = null;
            for (int i = 0; i < aList.size(); i++) {
                GameBall target = aList.get(i);
                if( target != null ) {
                    //Todo 计算白球和延长线的角度
                    for(GameBall bag:bags) {
                        GameBall extension2 = extensionCord(bag,target,r*2);
                        double angle =  includedAngle(white,extension2,extension2,bag);
//                        log.info("不计算障碍，得到角度：{}，白球：{}，球袋：{},目标球：{}",angle,white,bag,target);
//                        log.info("夹角：{}",angle);

//                        if(Math.abs(angle)<= min) {
//                            log.info("直达目标球：{}，夹角：{}",target,angle);
//                            boolean hasBlock = hasBlock(pointList, extension2, bag);
//                            if( !hasBlock) {
//                                log.info("找到目标球：{}，夹角：{}",target,angle);
//                                extension = extension2;
//                                min = Math.abs(angle);
//                            }
//                        }
                        if( Math.abs(angle)< min ) {
                            min = Math.abs(angle);
                            extension = extension2;
                            target2 = target;
                            bag2 = bag;
                        }
                    }
                }
            }
            if( extension != null ) {
                double angle2 = white.getAngle(extension);
//                log.info("找到可以直接进的球：{}",angle2);
//                log.info("击球角度：{},延长点：{}，球洞：{}，白球位置：{},目标球：{}",angle2,extension,bag2,white,target2);
                return angle2;
            }

//            log.info("找不到角度，对着最近的目标打。。。");
//            double angle2 = white.getAngle(extension);
            return white.getAngle(aList.get(0));
        }


        if(targetList.size()<=0 ) {
	        return 0;
        }
        return white.getAngle(targetList.get(0));
    }


    /**
     * AI击球角度选择
     * 1，先找到自己击球集合内白球可以直接无障碍到达的目标球集合
     * 2，再从目标球集合内选择到球袋无障碍的球
     * 3，找到则直接击球，没找到，则直接击打最近的目标球
     * @param pointList 所有球集合
     * @param targetList 当前玩家需要击打的球集合
     * @param white 白球
     * @param bags2 球洞。
     * @return
     */
    public static double calculateAngle(List<GameBall> pointList, List<GameBall> targetList, GameBall white, GameBall[] bags2){
        List<GameBall> aList = new ArrayList<>();
        for (GameBall p:targetList) {
            boolean hasBlock = hasBlock(pointList, white, p);
            if( !hasBlock ) {
	            aList.add(p);
            }
        }

        if( aList.size()>0 ) {
            double min = 180;
            GameBall extension = null;
            for (int i = 0; i < aList.size(); i++) {
                GameBall target = aList.get(i);
                if( target != null ) {
                    //Todo 计算白球和延长线的角度
                    for(GameBall bag:bags) {
                        if((white.getY()<target.getY()) && (bag.getY()<white.getY()) ) {
	                        continue;
                        }
                        if((white.getY()>target.getY()) && (bag.getY()>white.getY()) ) {
	                        continue;
                        }

                        if((white.getX()<target.getX()) && (bag.getX()<target.getX()) ) {
	                        continue;
                        }
                        if((white.getX()>target.getX()) && (bag.getX()>target.getX()) ) {
	                        continue;
                        }

                        GameBall extension2 = extensionCord(bag,target,r*2);
                        double angle =  includedAngle(white,extension2,extension2,bag);
                        if( angle < min) {
//                            log.info("直达目标球：{}",target);
                            boolean hasBlock = hasBlock(pointList, extension2, bag,target);
                            if( !hasBlock) {
//                                log.info("找到目标球：{}",target);
                                extension = extension2;
                                min = Math.abs(angle);
                            }
                        }
                    }
                }
            }
            if( extension != null ) {
                double angle2 = white.getAngle(extension);
//                log.info("找到可以直接进的球：{}",angle2);
                return angle2;
            }

//            log.info("找不到角度，对着最近的目标打。。。");
            return white.getAngle(aList.get(0));
        }


        if(targetList.size()<=0 ) {
	        return 0;
        }
        return white.getAngle(targetList.get(0));
    }

    /**
     * AI 选择目标，并计算精准角度
     * @param pointList 所有球
     * @param targetList 目标球
     * @param white 白球
     * @return
     */
    public static double calculateAngle2(List<GameBall> pointList, List<GameBall> targetList, GameBall white, GameBall[] bags) {
        //Step1:选出所有与白球可以连线(中间没有阻挡)的球集合A
//        log.info("机器人计算角度，所有球：{}，目标球：{}，白球：{}",pointList.size(),targetList.size(),white);
        List<GameBall> aList = new ArrayList<>();
        for (GameBall p:targetList) {
            boolean hasBlock = hasBlock(pointList, white, p);
            if( !hasBlock ) {
	            aList.add(p);
            }
        }
//        aList.stream().forEach((p)-> System.out.println(p.x+","+p.y));
        //Step1:选出所有与白球可以连线(中间没有阻挡)的球集合A
        //Step2:将A中的各个球跟球袋连线，排除连线上有阻挡的路线，生成B
        double minAngle = 180;
        GameBall target = null;
        GameBall targetBag = null;
        for(GameBall point:aList) {
            for(GameBall bag:bags ) {
                //1，先判断到达球袋是否有阻碍点
                if( hasBlock(pointList,bag,point)) {
	                continue;
                }
                //2,如果夹角小于最小值，则设置为最小值
                double angle =  includedAngle(white,point,point,bag);
                if(angle < minAngle) {
                    minAngle = angle;
                    target = point;
                    targetBag = bag;
                }
            }
        }

        double finalAngle  ;
        if ( target != null ) {
            //TODO 判断是否大于90度夹角,怎么处理？
            double angle =  includedAngle(white,target,target,targetBag);
            if( angle >90 ) {
            }

//            log.info("找到目标球，号码：{}",target.getNumber());
            //找到目标击球
            //确定击球角度
            //step1:找到延长线的点
            GameBall extensionPoint = extensionCord(targetBag,target,r);
            //计算白球和延长线的角度
            finalAngle = white.getAngle(extensionPoint);
        } else if( aList.size()>0 ){
//            log.info("没有找到目标路线，随机一个球");
            Random random = new Random();
            int index = random.nextInt(aList.size());
            target = aList.get(index);
            finalAngle = white.getAngle(target);
            double r =  random.nextDouble()*10-5;
            finalAngle += r;
        } else {
//            log.info("没有找到目标路线，随机一个角度");
            Random random = new Random();
            finalAngle = random.nextInt(360);
        }
        return finalAngle;
        //Step3:以白球到集合{A}中各目标球的延长线为基准线
        //寻找集合{B}中偏差角度最小的路线为当次击球的目标路线
        //Step4:如果没有目标路线，则随机A集合中的任意一个目标
        //Step5:如果A集合没有目标球，则随机发球角度
    }

    /**
     * 判断开始点到目标点中间是否有障碍
     * @param pointList    所有球
     * @param start 开始点
     * @param end   目标点
     * @return
     */
    private static boolean hasBlock(List<GameBall> pointList, GameBall start, GameBall end) {
        boolean hasBlock = false;
        for (GameBall pp:pointList ) {
            if( pp!=end && pp!=start) {
                double distance = distanceToSegment(pp,start,end);
                if( distance < 2*r ) {
//                    System.out.println("球袋："+start+",目标球："+end.getNumber()+",阻挡号码："+pp);
                    double maxx = Math.max(start.x,end.x);
                    double minx = Math.min(start.x,end.x);
                    double maxy = Math.max(start.y,end.y);
                    double miny = Math.min(start.y,end.y);
                    if( (pp.x<=maxx && pp.x>=minx) || (pp.y<=maxy && pp.y>=miny)) {
                        hasBlock = true;
                        break;
                    }
                }
            }
        }
        return hasBlock;
    }

    /**
     * 判断开始点到目标点中间是否有障碍，还要排除目标球
     * @param pointList    所有球
     * @param start 开始点
     * @param end   目标点
     * @Param target 目标球
     * @return
     */
    private static boolean hasBlock(List<GameBall> pointList, GameBall start, GameBall end,GameBall target) {
        boolean hasBlock = false;
        for (GameBall pp:pointList ) {
            if( pp!=end && pp!=start && pp!=target) {
                double distance = distanceToSegment(pp,start,end);
                if( distance < 2*r ) {
//                    System.out.println("球袋："+start+",目标球："+end.getNumber()+",阻挡号码："+pp);
                    double maxx = Math.max(start.x,end.x);
                    double minx = Math.min(start.x,end.x);
                    double maxy = Math.max(start.y,end.y);
                    double miny = Math.min(start.y,end.y);
                    if( (pp.x<=maxx && pp.x>=minx) || (pp.y<=maxy && pp.y>=miny)) {
                        hasBlock = true;
                        break;
                    }
                }
            }
        }
        return hasBlock;
    }

}
