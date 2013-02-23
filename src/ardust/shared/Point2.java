package ardust.shared;

public class Point2 {
    public int x, y;

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point2() {
        x = 0;
        y = 0;
    }

    public void set(Point2 point) {
        this.x = point.x;
        this.y = point.y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //todo, make fast
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point2 point3 = (Point2) o;

        if (x != point3.x) return false;
        if (y != point3.y) return false;

        return true;
    }

    //todo, make fast
    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }

    public int manhattanDistance(int otherX, int otherY)
    {
        return Math.abs(otherX - x) + Math.abs(otherY - y);
    }

    public void move(Orientation orientation) {
        switch (orientation) {
            case NORTH:
                y -= 1;
                break;
            case EAST:
                x += 1;
                break;
            case SOUTH:
                y += 1;
                break;
            case WEST:
                x -= 1;
                break;
            default:
                break;
        }

    }
}
