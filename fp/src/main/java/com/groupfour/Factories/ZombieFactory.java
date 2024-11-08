package com.groupfour.Factories;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.texture;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.components.AutoRotationComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import com.groupfour.Components.ZombieComponents.ZombieComponent;
import com.groupfour.mygame.EntityTypes.EntityType;

import javafx.geometry.Point2D;

public class ZombieFactory implements EntityFactory {

    private static final int SPAWN_DISTANCE = 500;


    private static final Point2D[] spawnPoints = new Point2D[] {
            new Point2D(SPAWN_DISTANCE, SPAWN_DISTANCE),
            new Point2D(getAppWidth() - SPAWN_DISTANCE, SPAWN_DISTANCE),
            new Point2D(getAppWidth() - SPAWN_DISTANCE, getAppHeight() - SPAWN_DISTANCE),
            new Point2D(SPAWN_DISTANCE, getAppHeight() - SPAWN_DISTANCE)
    };

    // just some code i needed to test

    private static Point2D getRandomSpawnPoint() {
        var players = getGameWorld().getEntitiesByType(EntityType.PLAYER);
        int attempts = 0;
        int maxAttempts = 1000;

        while (attempts < maxAttempts) {
            Point2D potentialSpawnPoint = spawnPoints[FXGLMath.random(0, spawnPoints.length - 1)];
            boolean isSafe = players.stream().allMatch(player -> 
                potentialSpawnPoint.distance(player.getPosition()) >= SPAWN_DISTANCE
            );

            if (isSafe) {
                return potentialSpawnPoint;
            }
            attempts++;
        }

        //if player is in vicinity of spawn area, spawn at a new location
        return new Point2D(SPAWN_DISTANCE, SPAWN_DISTANCE);
    }

    private EntityBuilder commonZombieSetup(SpawnData data, int health) {
        return entityBuilder(data)
                .type(EntityType.ZOMBIE)
                .at(getRandomSpawnPoint())
                .collidable()
                .with(new NetworkComponent())
                .with(new ZombieComponent(health));
    }
    
    @Spawns("zombie")
    public Entity newZombie(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }

    @Spawns("chef_zombie")
    public Entity newChefZombie(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }

    @Spawns("doctor_zombie")
    public Entity newDoctorZombie(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }

    @Spawns("guard_zombie")
    public Entity newGuardZombie(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }

    @Spawns("hazmat_zombie")
    public Entity newHazmatZombie(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }

    @Spawns("welder_zombie")
    public Entity newWelderZombie(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }

    @Spawns("warden")
    public Entity newWarden(SpawnData data) {
        return commonZombieSetup(data, 60)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(new CellMoveComponent(40, 40, 150))
                .build();
    }



}
