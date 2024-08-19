package com.orangomango.astrorunner;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

public class Player{
	private Point2D[] points;

	public Player(Point2D... p){
		this.points = p;
	}

	public Point2D[] getBoundingBox(){
		return this.points;
	}

	public void render(GraphicsContext gc, boolean shield){
		double[] xPoints = new double[this.points.length];
		double[] yPoints = new double[this.points.length];
		final double aspectRatio = (double)MainApplication.HEIGHT/MainApplication.WIDTH;

		for (int i = 0; i < this.points.length; i++){
			xPoints[i] = (this.points[i].getX()*aspectRatio+1)/2*MainApplication.WIDTH;
			yPoints[i] = (this.points[i].getY()+1)/2*MainApplication.HEIGHT;
		}

		gc.setStroke(Color.WHITE);
		gc.setLineWidth(2);
		gc.strokePolygon(xPoints, yPoints, this.points.length);
	}
}