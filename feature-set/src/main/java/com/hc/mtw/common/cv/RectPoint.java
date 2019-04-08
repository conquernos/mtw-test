package com.hc.mtw.common.cv;

import org.opencv.core.Point;


public class RectPoint {

    private int x;
    private int y;
    private int width;
    private int height;

    public RectPoint(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Point getStartPoint() {
        return new Point(getX(), getY());
    }

    public Point getEndPoint() {
        return new Point(getX() + getWidth(), getY() + getHeight());
    }

}
