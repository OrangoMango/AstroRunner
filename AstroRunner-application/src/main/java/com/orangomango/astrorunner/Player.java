package com.orangomango.astrorunner;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;

public class Player{
	private Point2D[] points;
	public int frameIndex;

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

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < this.points.length; i++){
			minX = Math.min(xPoints[i], minX);
			minY = Math.min(yPoints[i], minY);
			maxX = Math.max(xPoints[i], maxX);
			maxY = Math.max(yPoints[i], maxY);
		}

		gc.drawImage(AssetLoader.getInstance().getImage("player.png"), 1+34*(shield ? this.frameIndex+3 : this.frameIndex), 1, 32, 32, minX, minY, maxX-minX, maxY-minY);
	}
}