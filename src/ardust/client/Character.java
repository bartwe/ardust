package ardust.client;

import ardust.shared.Constants;

import java.awt.*;
import java.security.PublicKey;
import java.util.Random;

public class Character {

    int speed, betweenTilePosition;
    Direction movementDirection = Direction.HALT;
    Direction nextDirection = Direction.HALT;
    Direction facingDirection;
    AnimatedSprite sprite;
    boolean  flipAnimation;
    Point3 location;

    public enum Direction { UP, RIGHT, DOWN, LEFT, HALT}



    public Character(int x, int y, int z, int speed) {
        this.speed = speed;
        this.sprite = new AnimatedSprite();
        location = new Point3(x, y, z);
        faceRandomDirection();
    }

    public Point3 getLocation()
    {
        return location;
    }



    public boolean isMoving() {return movementDirection != Direction.HALT;}

    public boolean hasReachedNextTile()
    {
        switch (movementDirection)
        {
            case UP:
            case DOWN: return betweenTilePosition >= Constants.TILE_BASE_HEIGHT;
            case LEFT:
            case RIGHT: return betweenTilePosition >= Constants.TILE_BASE_WIDTH;
        }
        return false;
    }

    public boolean changePosition(Direction direction, World world)
    {

        Point3 tmp = new Point3(location.x, location.y, location.z);
        switch (direction)
        {
            case UP: tmp.y--; break;
            case RIGHT: tmp.x++; break;
            case DOWN: tmp.y++; break;
            case LEFT: tmp.x--; break;
        }
        if (!world.isTileOccupied(tmp.x, tmp.y, tmp.z))
        {
            location = tmp;
        }
        return false;
    }

    public void faceRandomDirection()
    {
        Random r = new Random();
        switch (r.nextInt(4))
        {
            case 0: facingDirection = Direction.UP;break;
            case 1: facingDirection = Direction.RIGHT; break;
            case 2: facingDirection = Direction.DOWN; break;
            case 3: facingDirection = Direction.LEFT; break;
        }
    }

    public void setMoving(Direction direction, World world)
    {
         nextDirection = direction;
         if (direction != Direction.HALT) facingDirection = direction;

         if (movementDirection == Direction.HALT)
         {
             movementDirection = direction;
             if (!changePosition(direction, world))
             {
                 movementDirection = Direction.HALT;
             }
         }

         flipAnimation = (facingDirection == Direction.RIGHT);
    }

    public void animateWalk()
    {
         switch (movementDirection)
         {
             case UP: sprite.animate(28,4,Constants.DWARF_ANIMATION_SPEED); break;
             default: sprite.animate(24,4,Constants.DWARF_ANIMATION_SPEED);break;
         }
    }

    public void showStationarySprite()
    {
        switch (facingDirection)
        {
            case UP: sprite.currentFrame = 29; break;
            default: sprite.currentFrame = 25; break;
        }
    }


    public void tick(World world)
    {
          if (isMoving())
          {
              betweenTilePosition += speed;
              if (hasReachedNextTile())
              {
                   betweenTilePosition = 0;
                  if (nextDirection != movementDirection) setMoving(nextDirection, world);
              }
              animateWalk();
          } else {
              showStationarySprite();
          }
    }

    public void draw(Painter p, Point viewportLocation)
    {
        Point localPoint = new Point(0,0); 
        World.globalTileToLocalCoord(location.x, location.y,viewportLocation, localPoint );
        sprite.draw(p,localPoint.x, localPoint.y - Constants.TILE_BASE_HEIGHT - Constants.DWARF_OFFSET_ON_TILE, flipAnimation);
    }





}
