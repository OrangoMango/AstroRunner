package com.orangomango.astrorunner;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

public class Obstacle{
	private ArrayList<Point3D[]> polygons;
	private double rotAngle, startAngle, dropAngle, startDropAngle;
	private Color color;
	private boolean rotates;
	private Point3D[] powerUp;
	private int type;

	public static final int OBSTACLE_COUNT = 10;
	public static final int OBSTACLE_DISTANCE = 4;
	public static final double OBSTACLE_SIZE = 0.7;
	public static final double POWERUP_SIZE = 0.1;

	public Obstacle(ArrayList<Point3D[]> pol, int type){
		this.type = type;
		this.polygons = pol;
		this.color = Color.color(Math.random(), Math.random(), Math.random());
		this.rotates = Math.random() < 0.3;

		Random random = new Random();
		this.startAngle = random.nextInt(360);
		this.rotAngle = this.startAngle;
		this.startDropAngle = random.nextInt(360);
		this.dropAngle = this.startDropAngle;
	}

	public void setCameraRotation(int angle){
		this.rotAngle = this.startAngle+angle;
		this.dropAngle = this.startDropAngle+angle;
	}

	public double getDepth(){
		return this.polygons.get(0)[0].getZ();
	}

	public boolean update(){
		final double travelSpeed = 0.1;

		for (Point3D[] points : this.polygons){
			for (int i = 0; i < points.length; i++){
				points[i] = points[i].add(0, 0, -travelSpeed); // Move the obstacles
			}
		}

		// Move the powerUp
		if (this.powerUp != null){
			for (int i = 0; i < this.powerUp.length; i++){
				this.powerUp[i] = this.powerUp[i].add(0, 0, -travelSpeed);
			}
		}

		if (this.rotates){
			this.startAngle += 3;
			this.rotAngle += 3;
		}

		boolean passed = false;
		if (getDepth() <= 0.5){
			passed = true;

			// Reset
			Obstacle newObstacle = createRandomObstacle(5+(OBSTACLE_COUNT-1)*OBSTACLE_DISTANCE);
			this.type = newObstacle.type;
			this.polygons = newObstacle.polygons;
			this.rotAngle = newObstacle.rotAngle;
			this.startAngle = newObstacle.startAngle;
			this.color = newObstacle.color;
			this.rotates = newObstacle.rotates;
			this.powerUp = newObstacle.powerUp;
		}

		return passed;
	}

	public int renderAndCheck(GraphicsContext gc, Player player){
		int collided = 0; // 0 -> No collision, 1 -> obstacle collision, 2 -> powerUp collision

		// Render the texture
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (Point3D[] points : this.polygons){
			for (Point3D p : points){
				minX = Math.min(p.getX(), minX);
				minY = Math.min(p.getY(), minY);
				maxX = Math.max(p.getX(), maxX);
				maxY = Math.max(p.getY(), maxY);
			}
		}

		Point2D minPoint = Util.mapPoint(Util.project(new Point3D(minX, minY, getDepth())));
		Point2D maxPoint = Util.mapPoint(Util.project(new Point3D(maxX, maxY, getDepth())));

		gc.save();
		//gc.setStroke(this.color);
		//gc.setLineWidth(3);
		gc.translate(MainApplication.WIDTH*0.5, MainApplication.HEIGHT*0.5);
		gc.rotate(this.rotAngle);
		
		gc.drawImage(AssetLoader.getInstance().getImage("obstacle_type"+this.type+".png"), minPoint.getX()-MainApplication.WIDTH*0.5, minPoint.getY()-MainApplication.HEIGHT*0.5, maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());
		//gc.strokeRect(minPoint.getX()-MainApplication.WIDTH*0.5, minPoint.getY()-MainApplication.HEIGHT*0.5, maxPoint.getX()-minPoint.getX(), maxPoint.getY()-minPoint.getY());

		gc.restore();

		for (Point3D[] points : this.polygons){
			// Rotate points
			ArrayList<Point3D> rotated = new ArrayList<>();
			for (Point3D p : points){
				rotated.add(Util.rotate(p, this.rotAngle));
			}

			// Project points
			ArrayList<Point2D> projected = Util.projectPoints(rotated);

			if (Util.collided(projected.toArray(new Point2D[projected.size()]), player.getBoundingBox())){
				collided = 1;
			}

			double[] xPoints = new double[projected.size()];
			double[] yPoints = new double[projected.size()];

			for (int i = 0; i < projected.size(); i++){
				Point2D a = Util.mapPoint(projected.get(i));
				xPoints[i] = a.getX();
				yPoints[i] = a.getY();
			}

			//gc.setFill(this.color);
			//gc.fillPolygon(xPoints, yPoints, projected.size());
		}

		// Render the powerup
		if (this.powerUp != null){
			if (this.powerUp[0].getZ() >= 0){
				ArrayList<Point3D> rotated = new ArrayList<>();
				for (Point3D p : this.powerUp){
					rotated.add(Util.rotate(p, this.dropAngle));
				}

				ArrayList<Point2D> projected = Util.projectPoints(rotated);

				if (Util.collided(projected.toArray(new Point2D[projected.size()]), player.getBoundingBox())){
					collided = 2;
				}

				double[] xPoints = new double[projected.size()];
				double[] yPoints = new double[projected.size()];

				for (int i = 0; i < projected.size(); i++){
					Point2D a = Util.mapPoint(projected.get(i));
					xPoints[i] = a.getX();
					yPoints[i] = a.getY();
				}

				gc.setFill(this.color.darker());
				gc.fillPolygon(xPoints, yPoints, projected.size());
			}
		}

		return collided;
	}

	private static Point3D[] createRandomPowerUp(double zPos){
		if (Math.random() < 0.35){
			Point3D[] output = new Point3D[4];
			
			output[0] = new Point3D(-POWERUP_SIZE, 0.4-POWERUP_SIZE, zPos);
			output[1] = new Point3D(+POWERUP_SIZE, 0.4-POWERUP_SIZE, zPos);
			output[2] = new Point3D(+POWERUP_SIZE, 0.4+POWERUP_SIZE, zPos);
			output[3] = new Point3D(-POWERUP_SIZE, 0.4+POWERUP_SIZE, zPos);

			return output;
		} else {
			return null;
		}
	}

	private static Obstacle createObstacle(int type, double zPos){
		ArrayList<Point3D[]> polygons = new ArrayList<>();

		if (type == 0){ // Rectangle
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE, -OBSTACLE_SIZE*0.2, zPos), new Point3D(OBSTACLE_SIZE, -OBSTACLE_SIZE*0.2, zPos), new Point3D(OBSTACLE_SIZE, OBSTACLE_SIZE*0.2, zPos), new Point3D(-OBSTACLE_SIZE, OBSTACLE_SIZE*0.2, zPos)});
		} else if (type == 1){ // Double triangles
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE, -OBSTACLE_SIZE, zPos), new Point3D(0, 0, zPos), new Point3D(-OBSTACLE_SIZE, OBSTACLE_SIZE, zPos)});
			polygons.add(new Point3D[]{new Point3D(OBSTACLE_SIZE, -OBSTACLE_SIZE, zPos), new Point3D(0, 0, zPos), new Point3D(OBSTACLE_SIZE, OBSTACLE_SIZE, zPos)});
		} else if (type == 2){ // Half circle
			Point3D[] points = new Point3D[10];
			for (int i = 0; i < points.length; i++){
				double angle = Math.PI*i/(points.length-1);
				double x = OBSTACLE_SIZE*Math.cos(angle);
				double y = OBSTACLE_SIZE*Math.sin(angle);
				points[i] = new Point3D(x, y, zPos);
			}

			polygons.add(points);
		} else if (type == 3){ // Single triangle
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE, -OBSTACLE_SIZE, zPos), new Point3D(0, 0, zPos), new Point3D(-OBSTACLE_SIZE, OBSTACLE_SIZE, zPos)});
		} else if (type == 4){ // Cross
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE, -OBSTACLE_SIZE*0.2, zPos), new Point3D(OBSTACLE_SIZE, -OBSTACLE_SIZE*0.2, zPos), new Point3D(OBSTACLE_SIZE, OBSTACLE_SIZE*0.2, zPos), new Point3D(-OBSTACLE_SIZE, OBSTACLE_SIZE*0.2, zPos)});
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE*0.2, -OBSTACLE_SIZE, zPos), new Point3D(-OBSTACLE_SIZE*0.2, OBSTACLE_SIZE, zPos), new Point3D(OBSTACLE_SIZE*0.2, OBSTACLE_SIZE, zPos), new Point3D(OBSTACLE_SIZE*0.2, -OBSTACLE_SIZE, zPos)});
		}

		Obstacle obstacle = new Obstacle(polygons, type);
		obstacle.powerUp = createRandomPowerUp(zPos-OBSTACLE_DISTANCE*0.5);
		return obstacle;
	}

	public static Obstacle createRandomObstacle(double zPos){
		Random random = new Random();
		return createObstacle(random.nextInt(5), zPos);
	}
}