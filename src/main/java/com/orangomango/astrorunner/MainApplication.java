package com.orangomango.astrorunner;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import javafx.animation.*;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;

public class MainApplication extends Application{
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	private static final Font FONT = new Font("sans-serif", 22);

	private ArrayList<Obstacle> obstacles = new ArrayList<>();
	private int updateDelay = 50;
	private volatile int score = 0;
	private volatile int lastScore = 0;
	private HashMap<KeyCode, Boolean> keys = new HashMap<>();
	private int cameraRotAngle;
	private Player player;

	@Override
	public void start(Stage stage){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));

		for (int i = 0; i < Obstacle.OBSTACLE_COUNT; i++){
			this.obstacles.add(Obstacle.createRandomObstacle(5+i*Obstacle.OBSTACLE_DISTANCE));
		}

		this.player = new Player(new Point2D(-0.1, 0.7), new Point2D(0.1, 0.7), new Point2D(0.1, 0.9), new Point2D(-0.1, 0.9));

		AnimationTimer timer = new AnimationTimer(){
			private long lastTime = System.nanoTime();

			@Override
			public void handle(long time){
				final double deltaTime = (time-this.lastTime)/1000000.0; // in millis
				update(gc, deltaTime);
				this.lastTime = time;
			}
		};
		timer.start();

		Thread loop = new Thread(() -> {
			while (true){
				try {
					for (int i = 0; i < this.obstacles.size(); i++){
						Obstacle o = this.obstacles.get(i);
						o.setCameraRotation(this.cameraRotAngle);
						boolean passed = o.update();
						if (passed){
							this.score++;
						}
					}

					if (this.score != this.lastScore && this.score % 6 == 0){
						this.updateDelay = Math.max(this.updateDelay-6, 6);
						this.lastScore = this.score;
					}

					Thread.sleep(this.updateDelay);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		loop.setDaemon(true);
		loop.start();

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);

		stage.setScene(scene);
		stage.setTitle("Color Tunnel");
		stage.setResizable(false);
		stage.show();
	}

	private void update(GraphicsContext gc, double deltaTime){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		if (this.keys.getOrDefault(KeyCode.LEFT, false)){
			this.cameraRotAngle -= 5;
		} else if (this.keys.getOrDefault(KeyCode.RIGHT, false)){
			this.cameraRotAngle += 5;
		}

		this.obstacles.sort((o1, o2) -> -Double.compare(o1.getDepth(), o2.getDepth()));

		boolean collided = false;
		for (int i = 0; i < this.obstacles.size(); i++){
			Obstacle o = this.obstacles.get(i);
			boolean result = o.renderAndCheck(gc, this.player);
			if (result){
				collided = result;
			}
		}

		// GAME OVER
		if (collided){
			this.score = 0;
			this.lastScore = 0;
			this.updateDelay = 50;
		}

		this.player.render(gc, collided);

		gc.setFill(Color.LIME);
		gc.setFont(FONT);
		gc.fillText(String.format("Score: %d\nSpeed: %.2fkm/h", this.score, calculateSpeed(this.updateDelay)), 20, 40);
	}

	private static double calculateSpeed(int delay){
		final double minDelay = 5;
		final double maxDelay = 50;
		final double minSpeed = 40;
		final double maxSpeed = 130;

		return (1-(delay-minDelay)/(maxDelay-minDelay))*(maxSpeed-minSpeed)+minSpeed;
	}

	public static void main(String[] args){
		launch(args);
	}
}
