package ardust.client;

import ardust.entities.Entity;
import ardust.packets.DwarfRequestPacket;
import ardust.shared.*;

import java.awt.*;
import java.util.Random;


public class Character {

    double modeProgress;
    AnimatedSprite sprite = new AnimatedSprite();

    public Point3 location = new Point3();
    public Point3 targetLocation = new Point3();

    private final Entity entity;
    Entity.Mode prevMode = Entity.Mode.IDLE;

    CharacterAIMode aiMode = CharacterAIMode.IDLE;
    Point3 pathingTarget = new Point3();

    Random random = new Random();
    private int pathingFailStrike;

    public Character(Entity entity) {
        this.entity = entity;
    }

    public void animateWalk() {
        modeProgress = 1d - (double) entity.countdown / (double) Constants.WALKING_COUNTDOWN;
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
        int currentFrame = sprite.currentFrame;
        switch (entity.orientation) {
            case NORTH:
                sprite.animate(36, 4, Constants.DWARF_ANIMATION_SPEED / 2);
                break;
            default:
                sprite.animate(32, 4, Constants.DWARF_ANIMATION_SPEED / 2);
                break;
        }
        if (sprite.currentFrame != currentFrame && sprite.currentFrame % 3 == 0) {
            GameLoop.soundBank.playSound(SoundBank.pickaxeSound);
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

    public void tick(int deltaT, ClientWorld world, NetworkConnection network, GameCore core) {
        entity.countdown -= deltaT;

        if (entity.countdown < 0)
            entity.countdown = 0;

        boolean setCountdown = (prevMode != entity.mode) || (!location.equals(entity.position));
        prevMode = entity.mode;


        // detect if just started moving
        location.set(entity.position);
        targetLocation.set(location);
        switch (entity.mode) {
            case WALKING:
                if (setCountdown)
                    entity.countdown = Constants.WALKING_COUNTDOWN;
                animateWalk();
                targetLocation.move(entity.orientation);
                break;
            case MINING:
                animateMining();
                break;
            default:
                showStationarySprite();
        }

        switch (aiMode) {
            case WALK:
                if (!pathTowards(world, network))
                    aiMode = CharacterAIMode.IDLE;

                break;
            case USE:
                if (!pathTowards(world, network)) {

                    // if reached, use
                    aiMode = CharacterAIMode.IDLE;
                }
                break;
            case IDLE:
                break;
        }
    }

    Point3 tempPoint = new Point3();

    private boolean pathTowards(ClientWorld world, NetworkConnection network) {
        if (targetLocation.equals(pathingTarget))
            return false;
        Orientation ew;
        if (pathingTarget.x == targetLocation.x)
            ew = random.nextBoolean() ? Orientation.EAST : Orientation.WEST;
        else
            ew = (pathingTarget.x > targetLocation.x) ? Orientation.EAST : Orientation.WEST;

        Orientation ns;
        if (pathingTarget.y == targetLocation.y)
            ns = random.nextBoolean() ? Orientation.NORTH : Orientation.SOUTH;
        else
            ns = (pathingTarget.y > targetLocation.y) ? Orientation.SOUTH : Orientation.NORTH;

        double eww = Math.abs(pathingTarget.x - targetLocation.x);
        double nsw = Math.abs(pathingTarget.y - targetLocation.y);

        Orientation orientation;
        Orientation otherOrientation;
        double w = eww + nsw;
        eww /= w;
        if (random.nextFloat() < eww) {
            orientation = ew;
            otherOrientation = ns;
        } else {
            orientation = ns;
            otherOrientation = ew;
        }

        tempPoint.set(targetLocation);
        tempPoint.move(ew);
        if (!Constants.isWalkable(world.readDirect(tempPoint)))
            orientation = otherOrientation;

        tempPoint.set(targetLocation);
        tempPoint.move(orientation);
        if (!Constants.isWalkable(world.readDirect(tempPoint))) {
            pathingFailStrike -= 1;
            if (pathingFailStrike <= 0)
                return false;
            return true;
        }

        if (location.equals(tempPoint)) {
            pathingFailStrike -= 1;
            if (pathingFailStrike <= 0)
                return false;
        }
        else
            pathingFailStrike = Constants.WALK_LOOP_LIMIT;

        network.send(new DwarfRequestPacket(entity.id, DwarfRequest.Walk, orientation));
        return true;
    }

    public void getLocalDrawPoint(Point viewportLocation, Point result) {
        World.globalTileToLocalCoord(location.x, location.y, location.z, viewportLocation, result);

        if (entity.mode == Entity.Mode.WALKING) {
            switch (entity.orientation) {
                case NORTH:
                    result.y -= (int) (Constants.TILE_BASE_HEIGHT * modeProgress);
                    break;
                case EAST:
                    result.x += (int) (Constants.TILE_BASE_WIDTH * modeProgress);
                    break;
                case WEST:
                    result.x -= (int) (Constants.TILE_BASE_WIDTH * modeProgress);
                    break;
                case SOUTH:
                    result.y += (int) (Constants.TILE_BASE_HEIGHT * modeProgress);
                    break;
            }
        }
    }

    Point localPoint = new Point();

    public void draw(Painter p, Point viewportLocation, boolean selectedDwarf) {
        getLocalDrawPoint(viewportLocation, localPoint);
        boolean flipAnimation = (entity.orientation == Orientation.EAST);
        if (selectedDwarf)
            p.draw(localPoint.x + Constants.DWARF_HEART_CENTER_OFFSET, localPoint.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT), 96, 40, 9, 9, false);
        sprite.draw(p, localPoint.x, localPoint.y - Constants.TILE_BASE_HEIGHT - Constants.DWARF_OFFSET_ON_TILE, flipAnimation);
    }

    public Integer id() {
        return entity.id;
    }

    public void halt() {
        aiMode = CharacterAIMode.IDLE;
    }

    public void walkTo(Point3 target) {
        aiMode = CharacterAIMode.WALK;
        pathingTarget.set(target);
        pathingFailStrike = Constants.WALK_LOOP_LIMIT;
    }

    public void use(Point3 target) {
        aiMode = CharacterAIMode.USE;
        pathingTarget.set(target);
        pathingFailStrike = Constants.WALK_LOOP_LIMIT;
    }
}
