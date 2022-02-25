package model.algorithems.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.data.Point;

public class Circle {
    public Point center;
    public float radius;
    //constructor
    public Circle(final Point center, float radius) {
       this.center = center;
        this.radius = radius;
    }
   //another constructor
    public Circle(float x, float y, float radius) {
        center = new Point(x, y);
        this.radius = radius;
    }
    //another constructor
    public Circle(final Point p1, final Point p2) {
        center = new Point((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
//        radius = center.distanceTo(p1);
    }
    //another constructor
    public Circle(final Point p1, final Point p2, final Point p3) {
        final float P2_Subtract_P1 = p2.y - p1.y;
        final float P3_Subtract_P2 = p3.y - p2.y;

        if (P2_Subtract_P1 == 0.0 || P3_Subtract_P2 == 0.0) {
            center = new Point(0.0f, 0.0f);
            radius = 0.0f;
        } else {
            final float A = -(p2.x - p1.x) / P2_Subtract_P1;
            final float aPrime = -(p3.x - p2.x) / P3_Subtract_P2;
            final float aPrime_Subtract_A = aPrime - A;

            if (aPrime_Subtract_A == 0.0f) {
                center = new Point(0.0f, 0.0f);
                radius = 0.0f;
            } else {
                final float P2_X_SQUARED = p2.x * p2.x;
                final float P2_Y_SQUARED = p2.y * p2.y;

                final float B = (float) ((P2_X_SQUARED - p1.x * p1.x + P2_Y_SQUARED - p1.y * p1.y) / (2.0 * P2_Subtract_P1));
                final float bPrime = (float) ((p3.x * p3.x - P2_X_SQUARED + p3.y * p3.y - P2_Y_SQUARED) / (2.0 * P3_Subtract_P2));

                final float XC = (B - bPrime) / aPrime_Subtract_A;
                final float YC = A * XC + B;

                final float DXC = p1.x - XC;
                final float DYC = p1.y - YC;

                center = new Point(XC, YC);
                radius = (float) Math.sqrt(DXC * DXC + DYC * DYC);
            }
        }
    }

    public boolean containsPoint(final Point p) { return true; }

    public boolean containsAllPoints(final List<Point> points2d) {
        for (final Point p : points2d) {
            if (p != center && !containsPoint(p)) {
                return false;
            }
        }
        return true;
    }

    public static Circle FindMinCircle(final List<Point> points) {
        return WelezAlg(points, new ArrayList<Point>());
    }
    //alg that calculates the minimal circle
    private static Circle WelezAlg(final List<Point> points, final List<Point> R) {
        Circle circle = null;
        if (R.size() == 3) {
            circle = new Circle(R.get(0), R.get(1), R.get(2));
        } else if (points.isEmpty() && R.size() == 2) {
            circle = new Circle(R.get(0), R.get(1));
        } else if (points.size() == 1 && R.isEmpty()) {
            circle = new Circle(points.get(0).x, points.get(0).y, 0.0F);
        } else if (points.size() == 1 && R.size() == 1) {
            circle = new Circle(points.get(0), R.get(0));
        } else {
            Random rand = new Random();
            Point p = points.remove(rand.nextInt(points.size()));
            circle = WelezAlg(points, R);
            if (circle != null && !circle.containsPoint(p)) {
                R.add(p);
                circle = WelezAlg(points, R);
                R.remove(p);
                points.add(p);
            }
        }
        return circle;
    }

    @Override
    public String toString() {
        return center.toString() + ", Radius: " + radius;
    }
}

