package ardust.client;

import ardust.shared.Constants;

import java.awt.*;
import java.util.Random;

public class Character {

    double betweenTilePosition, speed;
    Direction movementDirection = Direction.HALT;
    Direction nextDirection = Direction.HALT;
    Direction facingDirection;
    AnimatedSprite sprite;
    boolean flipAnimation, isMining;
    Point3 location;

    public enum Direction {UP, RIGHT, DOWN, LEFT, HALT}


    public Character(int x, int y, int z, double speed) {
        this.speed = speed;
        this.sprite = new AnimatedSprite();
        location = new Point3(x, y, z);
        faceRandomDirection();
    }

    public Point3 getLocation() {
        return location;
    }

    public void setMovingBasedOnTileDifferential(int x, int y, World world) {
        int xDiff = x - location.x;
        int yDiff = y - location.y;

        if (xDiff < 0 && Math.abs(xDiff) > Math.abs((yDiff))) setMoving(Direction.LEFT, world);
        else if (xDiff > 0 && Math.abs(xDiff) > Math.abs((yDiff))) setMoving(Direction.RIGHT, world);
        else if (yDiff > 0) setMoving(Direction.DOWN, world);
        else if (yDiff < 0) setMoving(Direction.UP, world);
    }


    public boolean isMoving() {
        return movementDirection != Direction.HALT;
    }

    public boolean hasReachedNextTile() {
        switch (movementDirection) {
            case UP:
            case DOWN:
                return betweenTilePosition >= Constants.TILE_BASE_HEIGHT;
            case LEFT:
            case RIGHT:
                return betweenTilePosition >= Constants.TILE_BASE_WIDTH;
        }
        return false;
    }

    public boolean changePosition(Direction direction, World world) {

        Point3 tmp = new Point3(location.x, location.y, location.z);
        switch (direction) {
            case UP:
                tmp.y--;
                break;
            case RIGHT:
                tmp.x++;
                break;
            case DOWN:
                tmp.y++;
                break;
            case LEFT:
                tmp.x--;
                break;
        }
        if (!world.isTileOccupied(tmp.x, tmp.y, tmp.z)) {
            location = tmp;
            return true;
        } else {
            byte terrainObject = world.clientWorld.readDirect(tmp.x, tmp.y, tmp.z);
            //see what kind of object it is and respond... right now just assume it's stone
            isMining = true;
        }
        return false;
    }

    public void faceRandomDirection() {
        Random r = new Random();
        switch (r.nextInt(4)) {
            case 0:
                facingDirection = Direction.UP;
                break;
            case 1:
                facingDirection = Direction.RIGHT;
                break;
            case 2:
                facingDirection = Direction.DOWN;
                break;
            case 3:
                facingDirection = Direction.LEFT;
                break;
        }
    }

    public void setMoving(Direction direction, World world) {
        nextDirection = direction;
        if (direction != Direction.HALT) facingDirection = direction;

        if (movementDirection == Direction.HALT) {
            movementDirection = direction;
            if (!changePosition(direction, world)) {
                movementDirection = Direction.HALT;
            }
        }

        flipAnimation = (facingDirection == Direction.RIGHT);
    }

    public void animateWalk() {
        switch (movementDirection) {
            case UP:
                sprite.animate(28, 4, Constants.DWARF_ANIMATION_SPEED);
                break;
            default:
                sprite.animate(24, 4, Constants.DWARF_ANIMATION_SPEED);
                break;
        }
    }

    public void animateMining() {
        switch (movementDirection) {
            case UP:
                sprite.animate(36, 4, Constants.DWARF_ANIMATION_SPEED / 2);
                break;
            default:
                sprite.animate(32, 4, Constants.DWARF_ANIMATION_SPEED / 2);
                break;
        }
    }

    public void halt() {
        movementDirection = Direction.HALT;
        betweenTilePosition = 0;
    }

    public void showStationarySprite() {
        switch (facingDirection) {
            case UP:
                sprite.currentFrame = 29;
                break;
            default:
                sprite.currentFrame = 25;
                break;
        }
    }


    public void tick(World world) {
        if (isMoving()) {
            betweenTilePosition += speed;
            if (hasReachedNextTile()) {
                betweenTilePosition = 0;
                if (nextDirection != movementDirection) setMoving(nextDirection, world);
                else if (changePosition(movementDirection, world)) ;
                else halt();
            }
            animateWalk();
        } else if (isMining) {
            animateMining();
        } else {
            showStationarySprite();
        }
    }

    public void draw(Painter p, Point viewportLocation, boolean selectedDwarf) {
        Point localPoint = new Point(0, 0);
        World.globalTileToLocalCoord(location.x, location.y, viewportLocation, localPoint);

        if (isMoving()) {
            switch (movementDirection) {
                case UP:
                    localPoint.y += (Constants.TILE_BASE_HEIGHT - (int) betweenTilePosition);
                    break;
                case RIGHT:
                    localPoint.x -= (Constants.TILE_BASE_WIDTH - (int) betweenTilePosition);
                    break;
                case LEFT:
                    localPoint.x += (Constants.TILE_BASE_WIDTH - (int) betweenTilePosition);
                    break;
                case DOWN:
                    localPoint.y -= (Constants.TILE_BASE_HEIGHT - (int) betweenTilePosition);
                    break;
            }
        }
        if (selectedDwarf)
            p.draw(localPoint.x, localPoint.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT), 96, 40, 43, 7, false);//sorry
        sprite.draw(p, localPoint.x, localPoint.y - Constants.TILE_BASE_HEIGHT - Constants.DWARF_OFFSET_ON_TILE, flipAnimation);
    }


}
