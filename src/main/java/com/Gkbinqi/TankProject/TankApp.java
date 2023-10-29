package com.Gkbinqi.TankProject;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;


import static com.almasb.fxgl.dsl.FXGL.*;
//静态导入 之后使用FXGL.xxx 时可以直接使用了


/* 规则:
        assets 所有资源的父目录
            textures 专门用于存储图片文件的目录
            music 存放背景音乐信息 MP3 格式
            sounds 存储游戏音效 wav 格式
            levels 放置关卡地图信息相关文件 关卡常用 Tiled 制作
            ui 放置界面相关的文件
                fonts
                css
                cursors 光标文件
            dialogues 放置对话框文件
*/

public class TankApp extends GameApplication{ //TankApp这样的命名是一种规范 最好带着app表示这是一种application

    private Entity tankEntity;
    private LocalTimer shootTimer;
    private LocalTimer aimTimer;
    /*
    * Local Configuration
    * */
    private double currentAngle = 0;
    private final double pi = Math.PI;
    private double angleStepSize = 0.5;
    private final double stepSize = 2;
    private boolean isUsingExclusiveKey = false;//用于在使用某些单独按键时锁住其他单独案件 互斥信号量
    private final Duration shootDelay = Duration.seconds(0.25);

    private boolean achievements[];
    @Override
    protected void initSettings(GameSettings gameSettings) {
        //Basic setting
        gameSettings.setTitle("TankWar");
        gameSettings.setVersion("0.1 alpha");
        //View
        gameSettings.setWidth(800);
        gameSettings.setHeight(800);
        /*
          关于设置app的ICON 只要放在了assets/textures中后,可以直接用图片名字来寻址 无需写出全部路径
          注意包一定不能错 assets textures 这两个名字不能打错了
           */
        //Icon
        gameSettings.setAppIcon("newIcon.png");//string 类型的路径
    }

    /*
    * 预加载资源 希望实现加载动画
    * */
    @Override
    protected void onPreInit() {
        //设置游戏初始化音量
        FXGL.getSettings().setGlobalMusicVolume(0.5);
        FXGL.getSettings().setGlobalSoundVolume(0.8);
        FXGL.loopBGM("Adventure.mp3");
    }

    /*
    *  重写UI
    * */
    @Override
    protected void initUI() {
        //Text ourText = FXGL.addVarText("score", 20, 20);
        //ourText.setFill(Color.rgb(31,124,124));
        //ourText.fontProperty().unbind();
        //ourText.setFont(Font.font(35));
        //FXGL.addUINode(ourText) //这一步多余了 会报错
        //另一种绑定方式
        Text text = FXGL.getUIFactoryService().newText(FXGL.getip("score")
                .asString("score: %d"));
        text.setFill(Color.rgb(31,124,124));
        text.setLayoutX(30);
        text.setLayoutY(30);
        FXGL.addUINode(text);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("x", 0.0);
        vars.put("list", new ArrayList<>());
        vars.put("mostValuablePlayerName", "");
    }

    /**
     * ctrl + o 调用override菜单
     * 重写方法initGame 以实现游戏
     * */
    @Override
    protected void initGame() {
        achievements = new boolean[10];
        FXGL.getip("score").addListener((ob,ov,nv) ->{
            if ( nv.intValue() > 30 && !achievements[0]) {
                FXGL.getNotificationService().pushNotification("取得成就: 入门成功");
                achievements[0] = true;
            }
        });


        shootTimer = FXGL.newLocalTimer();
        aimTimer = FXGL.newLocalTimer();

        Canvas canvas = new Canvas(100,100);
        GraphicsContext g2d = canvas.getGraphicsContext2D();
        /*
        * Draw A Tank
        * */
        g2d.setFill(Color.rgb(236,173,11));
        g2d.fillRect(0,0,80,30);

        g2d.setFill(Color.rgb(206,90,8));
        g2d.fillRect(15,30,50,40);

        g2d.setFill(Color.rgb(236,173,11));
        g2d.fillRect(0,70,80,30);

        g2d.setFill(Color.rgb(134,131,131));
        g2d.fillRect(40,40,60,20);
        /* END */

        //FXGL.addUINode(canvas);不能这样 我们需要游戏里的对象

        /*我们要加入游戏里的对象 也就是 entity 类似于JAVA的object
        * 游戏中的对象 子弹等都是entity
        * */
        //先介绍用最繁琐的方式来创建
        tankEntity = FXGL.entityBuilder()
                //view 只会决定实体的外观 坦克的样子
                //.view(canvas)
                // b box 决定游戏实体在运行中的, 可检测的真实大小
                //.b box(BoundingShape.box(100,100))
                //viewWithBBox 简写 直接将画布的大小当作实体的大小
                .viewWithBBox(canvas)
                .type(GameType.TANK)
                //.view(new Text("Player Name: GKbinqi"))
                .build();

        //设置旋转中心点 否则默认以(0, 0)为中心旋转 会很奇怪
        tankEntity.setRotationOrigin(tankEntity.getCenter());

        FXGL.getGameWorld().addEntities(tankEntity);
        //Tank创建完毕

        //Point2D center = tankEntity.getCenter();
        //System.out.println(center);
        //System.out.println(tankEntity.getHeight());
        //System.out.println(tankEntity.getWidth());

        getGameWorld().addEntityFactory(new EntityFactory());

        //开始创建敌人
        createEnemy();
    }

    private void createEnemy() {
        getGameWorld().spawn("enemy", new SpawnData());
    }

    /*
    *  重写物理方法
    * */
    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {//现在 a 是bullet b是enemy
                FXGL.inc("score", 10);
                //产生爆炸效果
                FXGL.play("boom.wav");
                enemy.removeFromWorld();
                Point2D position = bullet.getCenter();
                bullet.removeFromWorld();

                //传统方式 缩放动画 创建一个爆炸实体加入到游戏中
                Circle circle = new Circle(10, Color.RED);
                Entity boom = FXGL.entityBuilder()
                        .at(position)
                        .view(circle)
                        //为了实现自动移除该实体 添加过期移除组件 超过指定时间就会移除组件
                        .with(new ExpireCleanComponent(Duration.seconds(0.5)))
                        .buildAndAttach();
                ScaleTransition st = new ScaleTransition(Duration.seconds(.25), circle);
                st.setToX(10);
                st.setToY(10);

                FadeTransition ft = new FadeTransition(Duration.seconds(.35), circle);
                ft.setToValue(0);

                ParallelTransition pt = new ParallelTransition(st, ft);
                //pt.setOnFinished(event -> boom.removeFromWorld());
                pt.play();
                createEnemy();
            }
        });
    }

    //重写方法 获取用户输入
    /**
     * *-------------------------> X
     * |(0, 0)
     * |
     * |
     * |
     * |
     * |
     * v Y
     *
     * 注意角度换算 在Entity中的rotate角度是直接用整数代指角度
     * 而在math函数的三角函数中整数要换算为角度 pi = 180° 即 1 = (180 / pi)°
     * */
    @Override
    protected void initInput() {
        /* Reset */
        //文本参数注意 简单明了 不要重复混淆 这个文本可以出现在用户操作指南界面上
        FXGL.getInput().addAction(new UserAction("Reset") {
            @Override
            protected void onActionEnd() {
                if (isUsingExclusiveKey) return;
                currentAngle = 0;
                tankEntity.setRotation(currentAngle);
                tankEntity.setPosition(100,100);
            }
        }, KeyCode.R);

        /* Print Current Angle*/
        //经过测试 angle 是从0开始 顺时针增加
        FXGL.getInput().addAction(new UserAction("Print Current Angle") {
            @Override
            protected void onActionEnd() {
                if(isUsingExclusiveKey) return;
                System.out.println(currentAngle);
            }
        }, KeyCode.P);

        /* Move Forward*/
        FXGL.getInput().addAction(new UserAction("Move Forward") {
            @Override
            protected void onAction() {
                if (isUsingExclusiveKey) return;

                Point2D memoryPosition = tankEntity.getPosition();
                tankEntity.translateX(stepSize * Math.cos(currentAngle/180 * pi));
                tankEntity.translateY(stepSize * Math.sin(currentAngle/180 * pi));
                if(isOutOfRange(tankEntity.getX(), tankEntity.getY())) {
                    tankEntity.setPosition(memoryPosition);
                }
            }
        }, KeyCode.UP);

        /* Move Back*/
        FXGL.getInput().addAction(new UserAction("Move Back") {
            @Override
            protected void onAction() {
                if (isUsingExclusiveKey) return;

                Point2D memoryPosition = tankEntity.getPosition();
                tankEntity.translateX(- stepSize * Math.cos(currentAngle/180 * pi));
                tankEntity.translateY(- stepSize * Math.sin(currentAngle/180 * pi));
                if(isOutOfRange(tankEntity.getX(), tankEntity.getY())) {
                    tankEntity.setPosition(memoryPosition);
                }
            }
        }, KeyCode.DOWN);

        /* Rotate Left */
        FXGL.getInput().addAction(new UserAction("Rotate Left") {
            @Override
            protected void onAction() {
                if (isUsingExclusiveKey) return;

                currentAngle = (currentAngle - angleStepSize) % 360;
                tankEntity.setRotation(currentAngle);
            }
        }, KeyCode.LEFT);

        /* Rotate Right */
        FXGL.getInput().addAction(new UserAction("Rotate Right") {
            @Override
            protected void onAction() {
                if (isUsingExclusiveKey) return;

                currentAngle = (currentAngle + angleStepSize) % 360;
                tankEntity.setRotation(currentAngle);
            }
        }, KeyCode.RIGHT);

        /* Shoot*/
        FXGL.getInput().addAction(new UserAction("Cannon Shoot") {
            @Override
            protected void onAction() {
                if (isUsingExclusiveKey) return;

                //判断发射时间间隔 若小于0.25则不发射
                if (!shootTimer.elapsed(shootDelay)) return;
                shootTimer.capture();

                FXGL.play("shoot.wav");
                //创建子弹实体
                //Entity bullet = FXGL.entityBuilder().build();
                //FXGL.getGameWorld().addEntity(bullet);
                //这里不使用old fashion模式 直接 build And attach
                Entity bullet = createBullet(600);
            }
        }, KeyCode.S);

        FXGL.getInput().addAction(new UserAction("Aiming") {
            @Override
            protected void onAction() {
                if (!aimTimer.elapsed(Duration.seconds(.06))) return;
                aimTimer.capture();

                Color color = Color.RED;
                Entity aimLine = FXGL.entityBuilder()
                        .at(tankEntity.getCenter().getX() - 5 + 40 * Math.cos(currentAngle/180 * pi),
                                tankEntity.getCenter().getY() - 5 + 40 * Math.sin(currentAngle/180 * pi))
                        .with(new ProjectileComponent(
                                new Point2D(Math.cos(currentAngle/180 * pi)
                                        , Math.sin(currentAngle/180 * pi))//使用向量表示方向
                                , 2400))
                        .view(new Rectangle(1800,2, color))
                        .with(new OffscreenCleanComponent())
                        .with(new ExpireCleanComponent(Duration.seconds(.06)))
                        .buildAndAttach();
            }
        }, KeyCode.A);

        FXGL.getInput().addAction(new UserAction("DESTRUCTION LASER") {
            @Override
            protected void onAction() {
                Entity bullet = createBullet(1800);
            }
        }, KeyCode.D);

        /* SPEED UP */
        FXGL.getInput().addAction(new UserAction("SPEED UP") {
            @Override
            protected void onActionBegin() {
                angleStepSize = 1.5;
            }

            @Override
            protected void onActionEnd() {
                angleStepSize = 0.5;
            }
        }, KeyCode.SPACE);
    }

    //为了刷新一些变量 需要重写 onUpdate 方法
    //在游戏中 每一帧都会自动调用这个 onUpdate 方法
    // tpf: 每一帧消耗的时间 一般的游戏为60帧每秒
    @Override
    protected void onUpdate(double tpf) {
        System.out.println(FXGL.getGameWorld().getEntities().size());
        //System.out.println(tpf);
        isUsingExclusiveKey = false;
    }

    private boolean isOutOfRange(double x, double y) {
        return (x < 0 || x > 900 || y < 0 || y > 600);
    }

    private Entity createBullet(int speed) {
        SpawnData spawnData = new SpawnData(tankEntity.getCenter().getX() - 5 + 40 * Math.cos(currentAngle/180 * pi),
                tankEntity.getCenter().getY() - 5 + 40 * Math.sin(currentAngle/180 * pi));
        getGameWorld().spawn("bullet", spawnData);
    }



    public Point2D getDirection() {
        return new Point2D(Math.cos(currentAngle/180 * pi)
                , Math.sin(currentAngle/180 * pi));
    }
    /* launch game */
    public static void main(String[] args) {
        launch(args);
    }
}
