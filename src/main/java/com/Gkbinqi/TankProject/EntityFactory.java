package com.Gkbinqi.TankProject;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class EntityFactory implements com.almasb.fxgl.entity.EntityFactory {
    @Spawns("enemy, rectangle")
    public Entity newEnemy(SpawnData spawnData) {
        return FXGL.entityBuilder(spawnData)
                .type(GameType.ENEMY)
                .viewWithBBox(new Rectangle(30,30, Color.LIGHTBLUE))
                //设置为可碰撞组件
                //.with(new CollidableComponent()) 这行代码与下面的代码等价
                .collidable()
                .buildAndAttach();
    }

    @Spawns("bullet")
    public Entity newBullet(SpawnData spawnData) {
        return FXGL.entityBuilder(spawnData)
                //指定子弹类型 以实现碰撞检测
                .type(GameType.BULLET)
                //view with BBox 用一个小正方形
                .viewWithBBox(new Rectangle(10,10))
                //with 关键字 可以跟一个子弹组件
                //第一个组件 子弹初始化 初始化速度和方向
                .with(new ProjectileComponent(
                        getDirection()//使用向量表示方向
                        , speed))
                //第二个组件 子弹的自动消除 子弹离开屏幕后自动移除该子弹实体
                .with(new OffscreenCleanComponent())
                //使得子弹可碰撞
                .collidable()
                //build and attach 直接自动加入游戏世界
                .buildAndAttach();
    }
}
