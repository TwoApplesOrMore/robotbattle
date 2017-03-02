package Dictators;

/**
 * Created by Thomas on 27-02-17.
 */
public class Teammate {
    String name;
    double x;
    double y;

    public Teammate(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
