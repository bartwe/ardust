package ardust.shared;

public class Point3 {
    public int x, y, z;

    public Point3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3() {
        x = 0;
        y = 0;
        z = 0;
    }

    public void set(Point3 point) {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    //todo, make fast
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point3 point3 = (Point3) o;

        if (x != point3.x) return false;
        if (y != point3.y) return false;
        if (z != point3.z) return false;

        return true;
    }

    //todo, make fast
    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
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
