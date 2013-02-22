package ardust.client;

import ardust.entities.Entity;
import ardust.shared.Constants;
import ardust.shared.Orientation;
import ardust.shared.Point3;

import java.awt.*;

public class Character {

    double modeProgress;
    AnimatedSprite sprite = new AnimatedSprite();

    public Point3 location = new Point3();
    public Point3 targetLocation = new Point3();

    private final Entity entity;
    Entity.Mode prevMode = Entity.Mode.IDLE;

    public Character(Entity entity) {
        this.entity = entity;
    }

    public void animateWalk() {
        modeProgress = 1d - (double)entity.countdown / (double)Constants.WALKING_COUNTDOWN;
        switch (entity.orientation) {
            case NORTH:
                sprite.animate(28, 4, Constants.DWARF_ANIMATION_SPEED);
                break;
            default:
                sprite.animate(24, 4, Constants.DWARF_ANIMATION_SPEED);
                break;
        }
    }

    public void animateMining() {
        switch (entity.orientation) {
            case NORTH:
                sprite.animate(36, 4, Constants.DWARF_ANIMATION_SPEED / 2);
                break;
            default:
                sprite.animate(32, 4, Constants.DWARF_ANIMATION_SPEED / 2);
                break;
        }
    }

    public void showStationarySprite() {
        switch (entity.orientation) {
            case NORTH:
                sprite.currentFrame = 29;
                break;
            default:
                sprite.currentFrame = 25;
                break;
        }
    }

    public void tick(int deltaT, ClientWorld world) {
        entity.countdown -= deltaT;

        if (entity.countdown < 0)
            entity.countdown = 0;

        // detect if just started moving
        boolean setCountdown = prevMode != entity.mode;
        prevMode = entity.mode;
        location.set(entity.position);
        switch (entity.mode) {
            case WALKING:
                if (setCountdown)
                    entity.countdown = Constants.WALKING_COUNTDOWN;
                animateWalk();
                break;
//            case MINING:
//                animateMining();
//                break;
            default:
                showStationarySprite();
        }
    }

    public Point getLocalDrawPoint(Point viewportLocation)
    {
        Point localPoint = new Point(0, 0);
        World.globalTileToLocalCoord(location.x, location.y, location.z, viewportLocation, localPoint);


        boolean flipAnimation = entity.orientation == Orientation.EAST;
        if (entity.mode == Entity.Mode.WALKING) {
            switch (entity.orientation) {
                case NORTH:
                    localPoint.y -= (int) (Constants.TILE_BASE_HEIGHT * modeProgress);
                    break;
                case EAST:
                    localPoint.x -= (int) (Constants.TILE_BASE_WIDTH * modeProgress);
                    localPoint.x += (int) (Constants.TILE_BASE_WIDTH * modeProgress);
                    break;
                case WEST:
                    localPoint.x -= (int) (Constants.TILE_BASE_WIDTH * modeProgress);
                    break;
                case SOUTH:
                    localPoint.y += (int) (Constants.TILE_BASE_HEIGHT * modeProgress);
                    break;
            }
        }
        return localPoint;
    }

    public void draw(Painter p, Point viewportLocation, boolean selectedDwarf) {
        Point localPoint = getLocalDrawPoint(viewportLocation);
        boolean flipAnimation = (entity.orientation == Orientation.EAST);
        if (selectedDwarf)
            p.draw(localPoint.x, localPoint.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT), 96, 40, 43, 7, false);//sorry
        sprite.draw(p, localPoint.x, localPoint.y - Constants.TILE_BASE_HEIGHT - Constants.DWARF_OFFSET_ON_TILE, flipAnimation);
    }


    public Integer id() {
        return entity.id;
    }
}
