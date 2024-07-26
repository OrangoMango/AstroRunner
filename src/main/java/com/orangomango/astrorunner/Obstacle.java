package com.orangomango.astrorunner;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class Obstacle{
	private ArrayList<Point3D[]> polygons;
	private double rotAngle, startAngle;
	private Color color;
	private boolean rotates;

	public static final int OBSTACLE_COUNT = 10;
	public static final int OBSTACLE_DISTANCE = 4;
	public static final double OBSTACLE_SIZE = 0.7;

	public Obstacle(ArrayList<Point3D[]> pol){
		this.polygons = pol;
		Random random = new Random();
		this.startAngle = random.nextInt(360);
		this.rotAngle = this.startAngle;
		this.color = Color.color(Math.random(), Math.random(), Math.random());
		this.rotates = Math.random() < 0.3;
	}

	public void setCameraRotation(int angle){
		this.rotAngle = this.startAngle+angle;
	}

	public double getDepth(){
		return this.polygons.get(0)[0].getZ();
	}

	public boolean update(){
		for (Point3D[] points : this.polygons){
			for (int i = 0; i < points.length; i++){
				points[i] = points[i].add(0, 0, -0.1);
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
			this.polygons = newObstacle.polygons;
			this.rotAngle = newObstacle.rotAngle;
			this.startAngle = newObstacle.startAngle;
			this.color = newObstacle.color;
			this.rotates = newObstacle.rotates;
		}

		return passed;
	}

	public boolean renderAndCheck(GraphicsContext gc, Player player){
		boolean collided = false;

		for (Point3D[] points : this.polygons){
			ArrayList<Point2D> projected = projectPoints(points);

			if (collided(projected.toArray(new Point2D[projected.size()]), player.getBoundingBox())){
				collided = true;
			}

			double[] xPoints = new double[projected.size()];
			double[] yPoints = new double[projected.size()];

			for (int i = 0; i < projected.size(); i++){
				Point2D a = mapPoint(projected.get(i));
				xPoints[i] = a.getX();
				yPoints[i] = a.getY();
			}

			//gc.setStroke(Color.WHITE);
			//gc.setLineWidth(2);
			//gc.strokePolygon(xPoints, yPoints, projected.size());
			gc.setFill(this.color);
			gc.fillPolygon(xPoints, yPoints, projected.size());
		}

		return collided;
	}

	private static boolean collided(Point2D[] aPoints, Point2D[] bPoints){
		ArrayList<Point2D[]> edges = new ArrayList<>();

		for (int i = 0; i < aPoints.length; i++){
			Point2D a = aPoints[i];
			Point2D b = aPoints[(i+1) % aPoints.length];
			edges.add(new Point2D[]{a, b});
		}

		for (int i = 0; i < bPoints.length; i++){
			Point2D a = bPoints[i];
			Point2D b = bPoints[(i+1) % bPoints.length];
			edges.add(new Point2D[]{a, b});
		}

		for (Point2D[] edge : edges){
			Point2D vector = edge[1].subtract(edge[0]);
			Point2D axis = new Point2D(-vector.getY(), vector.getX());
			ArrayList<Double> proj1 = new ArrayList<>();
			ArrayList<Double> proj2 = new ArrayList<>();
			for (Point2D v : aPoints){
				proj1.add(axis.dotProduct(v));
			}
			for (Point2D v : bPoints){
				proj2.add(axis.dotProduct(v));
			}
			double min1 = Collections.min(proj1);
			double min2 = Collections.min(proj2);
			double max1 = Collections.max(proj1);
			double max2 = Collections.max(proj2);

			if (!(min1 <= max2 && max1 >= min2)){
				return false;
			}
		}

		return true;
	}

	private static Point2D mapPoint(Point2D point){
		final double ratio = (double)MainApplication.HEIGHT/MainApplication.WIDTH;
		double x = (point.getX()*ratio+1)/2*MainApplication.WIDTH;
		double y = (point.getY()+1)/2*MainApplication.HEIGHT;

		return new Point2D(x, y);
	}

	private Point3D rotate(Point3D point){
		final double alpha = Math.toRadians(this.rotAngle);

		double x = point.getX()*Math.cos(alpha)-point.getY()*Math.sin(alpha);
		double y = point.getX()*Math.sin(alpha)+point.getY()*Math.cos(alpha);

		return new Point3D(x, y, point.getZ());
	}

	private Point2D project(Point3D point){
		final double fov = Math.PI/2;

		double x = point.getX()/(point.getZ()*Math.tan(fov/2));
		double y = point.getY()/(point.getZ()*Math.tan(fov/2));

		return new Point2D(x, y);
	}

	private ArrayList<Point2D> projectPoints(Point3D[] points){
		ArrayList<Point2D> output = new ArrayList<>();
		for (Point3D point : points){
			output.add(project(rotate(point)));
		}

		return output;
	}

	public static Obstacle createObstacle(int type, double zPos){
		ArrayList<Point3D[]> polygons = new ArrayList<>();

		if (type == 0){
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE, -OBSTACLE_SIZE*0.2, zPos), new Point3D(OBSTACLE_SIZE, -OBSTACLE_SIZE*0.2, zPos), new Point3D(OBSTACLE_SIZE, OBSTACLE_SIZE*0.2, zPos), new Point3D(-OBSTACLE_SIZE, OBSTACLE_SIZE*0.2, zPos)});
		} else if (type == 1){
			polygons.add(new Point3D[]{new Point3D(-OBSTACLE_SIZE, -OBSTACLE_SIZE, zPos), new Point3D(0, 0, zPos), new Point3D(-OBSTACLE_SIZE, OBSTACLE_SIZE, zPos)});
			polygons.add(new Point3D[]{new Point3D(OBSTACLE_SIZE, -OBSTACLE_SIZE, zPos), new Point3D(0, 0, zPos), new Point3D(OBSTACLE_SIZE, OBSTACLE_SIZE, zPos)});
		} else if (type == 2){
			Point3D[] points = new Point3D[10];
			for (int i = 0; i < points.length; i++){
				double angle = Math.PI*i/points.length;
				double x = OBSTACLE_SIZE*Math.cos(angle);
				double y = OBSTACLE_SIZE*Math.sin(angle);
				points[i] = new Point3D(x, y, zPos);
			}

			polygons.add(points);
		}

		Obstacle obstacle = new Obstacle(polygons);
		return obstacle;
	}

	public static Obstacle createRandomObstacle(double zPos){
		Random random = new Random();
		return createObstacle(random.nextInt(3), zPos);
	}
}