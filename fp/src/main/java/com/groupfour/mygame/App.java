package com.groupfour.mygame;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.multiplayer.*;
import com.almasb.fxgl.net.Connection;
import com.groupfour.Collisions.BulletZombieHandler;
import com.groupfour.Collisions.ZombiePlayerHandler;
import com.groupfour.Components.BulletComponent;
import com.groupfour.Components.PlayerComponent;
import com.groupfour.Components.ZombieComponent;
import com.groupfour.Factories.SpawnFactory;
import com.groupfour.Factories.ZombieFactory;
import com.groupfour.UI.LoadingScreen;
import com.groupfour.UI.MainUI;
import com.groupfour.UI.PlayerCountMenu;
import com.groupfour.mygame.EntityTypes.EntityType;


import java.util.ArrayList;
import java.util.List;

import com.almasb.fxgl.app.MenuItem;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class App extends GameApplication {
    
    private List<Entity> players= new ArrayList<>();
    private Entity player;
    private Entity playerPlaceHolder;
    private Entity zombie;
    private boolean isServer;
    private PhysicsWorld physics;
    private boolean gameStarted=false;
    private Input gameInput;
    private Connection<Bundle> connection;
    PlayerComponent placeholder;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setTitle("Flatline: Oregon");
        settings.setVersion("Alpha 0.3");
        settings.addEngineService(MultiplayerService.class);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);

        //implement later
        // settings.setEnabledMenuItems(EnumSet.of(MenuItem.EXTRA));
        // settings.getCredits().addAll(Arrays.asList(
        //         "PutCreditsHere- PlaceHolderName"
        // ));

        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newGameMenu() {
                return new PlayerCountMenu(
                    App.this::startGame1P,
                    App.this::startGame2P 
                );
            }
        });
    }

    @Override
    protected void initInput(){
        player = new  Entity();
        player.addComponent(new PlayerComponent(100));
        
        onKeyBuilder(getInput(), KeyCode.W).onAction(() ->player.getComponent(PlayerComponent.class).moveY(false));
        onKeyBuilder(getInput(), KeyCode.S).onAction(() ->player.getComponent(PlayerComponent.class).moveY(true));
        onKeyBuilder(getInput(), KeyCode.A).onAction(() ->player.getComponent(PlayerComponent.class).moveX(true));
        onKeyBuilder(getInput(), KeyCode.D).onAction(() ->player.getComponent(PlayerComponent.class).moveX(false));
        onKeyBuilder(getInput(), KeyCode.R).onActionBegin(() ->{player.getComponent(PlayerComponent.class).getCurrentWeapon().reload();});
        getInput().addAction(new UserAction("Start Shooting") {
            @Override
            protected void onActionBegin() {
               player.getComponent(PlayerComponent.class).getCurrentWeapon().fire(player);
               player.getComponent(PlayerComponent.class).setShooting(true);
            }
            protected void onActionEnd() {
               player.getComponent(PlayerComponent.class).setShooting(false);
            }
        }, MouseButton.PRIMARY);
    }
    
    @Override
    public void initGame() {
        getGameWorld().addEntityFactory(new SpawnFactory());
        getGameWorld().addEntityFactory(new ZombieFactory());
        player = spawn("player");
        player.getComponent(PlayerComponent.class).setUpPlayer();
    }

    @Override
    public void initPhysics() {
        physics = getPhysicsWorld();
        if (isServer) {
            physics.addCollisionHandler(new BulletZombieHandler());
            physics.addCollisionHandler(new ZombiePlayerHandler());
            getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());
            FXGL.run(() -> checkCollisions(), Duration.seconds(1));
        } else
            physics.addCollisionHandler(new BulletZombieHandler());
            physics.addCollisionHandler(new ZombiePlayerHandler());
            FXGL.run(() -> checkCollisions(), Duration.seconds(1));
    }
        
     @Override
     protected void initUI() {
        FXGL.runOnce(() -> FXGL.getSceneService().pushSubScene(new PlayerCountMenu(this::startGame1P, this::startGame2P)), Duration.seconds(.01));
        var ui = new MainUI();
        addUINode(ui, 30, 50);
    }
    
    public void startGame1P() {
        getSceneService().popSubScene();
        gameStarted=true;
        FXGL.run(() -> {
            zombie = spawn("zombie", player.getCenter().getX() + 20, player.getCenter().getY() + 20);
            zombie.getComponent(ZombieComponent.class).findClosestPlayer();
        }, Duration.seconds(1));

        FXGL.run(() -> updateFollower(), Duration.seconds(1));
        FXGL.run(()->{
            if(player.getComponent(PlayerComponent.class).isDead()){
                player.getComponent(PlayerComponent.class).setDeath(false);
                getDialogService().showMessageBox("You Died! Back to Main Menu?", () -> {
                getGameController().gotoMainMenu();
                FXGL.run(() -> {
                    resetGameWorld();
                }, Duration.seconds(5));
                });
            }
        },Duration.seconds(0.1));
    }

    public void resetGameWorld() {
        getGameWorld().getEntities().forEach(entity -> entity.removeFromWorld());
        zombie = null;
        gameStarted = false;
        getSceneService().pushSubScene(new PlayerCountMenu(this::startGame1P, this::startGame2P));
    }

    public void startGame2P() {
        // getDialogService().showConfirmationBox("Are you the host?", yes -> {
        //     isServer = yes;
        //     getGameWorld().addEntityFactory(new SpawnFactory());
        //     getGameWorld().addEntityFactory(new ZombieFactory());
        //     if (yes) {
        //         var server = getNetService().newTCPServer(55555);
        //         server.setOnConnected(conn -> {
        //             connection = conn;
        //             getExecutor().startAsyncFX(() -> {
        //                 onServer();
        //                 getSceneService().popSubScene();
        //                 getSceneService().popSubScene();
        //             });
        //         });
        //         server.startAsync();
        //         waitingForPlayers();
        //     } else {
        //         var client = getNetService().newTCPClient("localhost", 55555);
        //         client.setOnConnected(conn -> {
        //             connection = conn;
        //             getExecutor().startAsyncFX(() -> {
        //                 onClient();
        //                 getSceneService().popSubScene();
        //             });
        //         });
        //         client.connectAsync();
        //     }
        // });
    }

    private void waitingForPlayers() {
        // LoadingScreen loadingScreen = new LoadingScreen("Waiting for players...");
        // FXGL.getSceneService().pushSubScene(loadingScreen);
    }

    private void onServer() {
        // player1 = spawn("player");
        // getService(MultiplayerService.class).spawn(connection, player1, "player");
        // if (player1 == null) {
        //     System.out.println("Failed to spawn player1!");
        // } else {
        //     System.out.println("player1 spawned successfully!");
        //     getService(MultiplayerService.class).spawn(connection, player1, "player");
        // }
        // player2 = spawn("player");

        // getService(MultiplayerService.class).spawn(connection, player2, "player");
        
        // FXGL.run(() -> {
        //     zombie = spawn("zombie", player1.getCenter().getX() + 5, player1.getCenter().getY() + 5);
        //     getService(MultiplayerService.class).spawn(connection, zombie, "zombie");
        // }, Duration.seconds(1));

        // getService(MultiplayerService.class).addInputReplicationReceiver(connection, gameInput);
        // FXGL.run(() -> updateFollower(), Duration.seconds(1));
    }

    private void onClient() {
        // players.set(0,spawn("player"));
        // getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());
        // getService(MultiplayerService.class).addInputReplicationSender(connection, getInput());
    }

    private void updateFollower() {
        if (zombie.hasComponent(ZombieComponent.class)) {
            zombie.getComponent(ZombieComponent.class).onUpdate(0);
        } else {
            System.out.println("No more Zombies Left ");
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if(!gameStarted){
            return;
        }
        if (isServer) {
            gameInput.update(tpf);
        }

        //dont need this for now because m9 is semi auto. we add it once we decide on auto guns - padua

        // if (players.get(0).getComponent(PlayerComponent.class).isShooting()) {
        //     players.get(0).getComponent(PlayerComponent.class).setTimeSinceLastShot(players.get(0).getComponent(PlayerComponent.class).getTimeSinceLastShot() + tpf);
        //     if (players.get(0).getComponent(PlayerComponent.class).getTimeSinceLastShot() >= shootInterval) {
        //         shoot(getInput().getMousePositionWorld(), players.get(0)); 
        //         players.get(0).getComponent(PlayerComponent.class).setTimeSinceLastShot(0);
        //     }
        // }

        // if (isShootingP2) {
        //     timeSinceLastShotP2 += tpf;
        //     if (timeSinceLastShotP2 >= shootInterval) {
        //         shoot(getInput().getMousePositionWorld(), player2); 
        //         timeSinceLastShotP2 = 0;
        //     }
        // }
    }

    private void checkCollisions() {
        getGameWorld().getEntitiesByType(EntityType.ZOMBIE).forEach(zombie -> {
            getGameWorld().getEntitiesByType(EntityType.PLAYER).forEach(player -> {
                if (zombie.isColliding(player)) {
                    new ZombiePlayerHandler().inflictDamage(zombie, player);
                }
            });
        });    
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
