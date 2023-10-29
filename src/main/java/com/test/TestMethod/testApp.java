package com.test.TestMethod;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

import java.util.Map;

/*
* 可以指导每次操作会执行那些方法
* 比如
* 刚开始游戏会执行:
* testApp.main ==> main
* constructor. thread ==> main
* testApp.initSettings ==> main
*
* testApp.initInput ==> JavaFX Application Thread
* testApp.onPreInit ==> JavaFX Application Thread
*这些方法
*
* 每次newGame都会执行:
*   testApp.initGameVars ==> FXGL Background Thread 2
    testApp.initGame ==> FXGL Background Thread 2
    testApp.initPhysics ==> FXGL Background Thread 2
    testApp.initUI ==> FXGL Background Thread 2
*这些方法
*
* 需要注意 FXGL. 什么的方法, 在initinput后就可安全调用了 最好在initinput调用后再调用其他方法如FXGL.bgmLoop
* 没有强制性规定 只是最好划定好位置方便开发
* */

public class testApp extends GameApplication {
    public testApp() {
        //构造器
        System.out.println("constructor. thread ==> " + Thread.currentThread().getName() );
    }

    /*
    * 初始化游戏设置:
    * 宽高
    * 图标
    * 版本
    * 菜单
    * etc...
    * */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setMainMenuEnabled(true);
        System.out.println("testApp.initSettings ==> "+ Thread.currentThread().getName() );
    }

    /*
    * 游戏预处理
    * 预加载资源
    * 设置音量
    * */
    @Override
    protected void onPreInit() {
        System.out.println("testApp.onPreInit ==> "+ Thread.currentThread().getName() );
    }

    /*
    * 设置输入处理
    * */
    @Override
    protected void initInput() {
        System.out.println("testApp.initInput ==> "+ Thread.currentThread().getName() );
    }

    /*
    * 初始化游戏变量
    * */
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        System.out.println("testApp.initGameVars ==> "+ Thread.currentThread().getName() );
    }
    /*
    * 额外加入 简单输入
    * soutm 直接输出当前方法名
    * */

    /*
    * 初始化游戏 如设置一些音量(非建议)之类的东西
    * */
    @Override
    protected void initGame() {
        System.out.println("testApp.initGame ==> "+ Thread.currentThread().getName() );
    }

    /*
    * 初始化物理设置
    * 如 碰撞检测
    * */
    @Override
    protected void initPhysics() {
        System.out.println("testApp.initPhysics ==> "+ Thread.currentThread().getName() );
    }

    /*
    * 初始化界面组件
    * */
    @Override
    protected void initUI() {
        System.out.println("testApp.initUI ==> "+ Thread.currentThread().getName() );
        timer = FXGL.newLocalTimer();
    }


    private LocalTimer timer;
    /*
    * 游戏开始后 每一帧都会调用该方法
    * */
    @Override
    protected void onUpdate(double tpf) {
        if (timer.elapsed(Duration.seconds(2))) {
            timer.capture();
            System.out.println("testApp.onUpdate ==> "+ Thread.currentThread().getName() );
        }
    }

    public static void main(String[] args) {
        System.out.println("testApp.main ==> "+ Thread.currentThread().getName() );
        launch(args);
    }
}
