package com.orangomango.astrorunner;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.Collections;

public class Util{
	public static boolean collided(Point2D[] aPoints, Point2D[] bPoints){
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

	public static Point2D mapPoint(Point2D point){
		final double ratio = (double)MainApplication.HEIGHT/MainApplication.WIDTH;
		double x = (point.getX()*ratio+1)/2*MainApplication.WIDTH;
		double y = (point.getY()+1)/2*MainApplication.HEIGHT;

		return new Point2D(x, y);
	}

	public static Point3D rotate(Point3D point, double angle){
		final double alpha = Math.toRadians(angle);

		double x = point.getX()*Math.cos(alpha)-point.getY()*Math.sin(alpha);
		double y = point.getX()*Math.sin(alpha)+point.getY()*Math.cos(alpha);

		return new Point3D(x, y, point.getZ());
	}

	public static Point2D project(Point3D point){
		final double fov = Math.PI/2;

		double x = point.getX()/(point.getZ()*Math.tan(fov/2));
		double y = point.getY()/(point.getZ()*Math.tan(fov/2));

		return new Point2D(x, y);
	}

	public static ArrayList<Point2D> projectPoints(ArrayList<Point3D> points){
		ArrayList<Point2D> output = new ArrayList<>();
		for (Point3D point : points){
			output.add(project(point));
		}

		return output;
	}
}