package ardust.client;

import ardust.shared.NetworkConnection;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;


public class GameLoop {
    private static final int PIXEL_SCALE = 3;
    public static final int TILE_BASE_WIDTH = 32;
    public static final int TILE_BASE_HEIGHT = 16; //the height of an in-game tile.
    public static final int TILE_DRAW_HEIGHT = 40; //the actual height of the graphic assets
    public static final int FLOOR_TILE_THICKNESS = 4;
    public static final int MAP_PAN_MAX_SPEED = 2;
    public static final int MAP_PAN_SENSITIVITY = 128; //distance to drag away from click to reach max panning speed

    //mouse cursors
    public static final int CURSOR_X_IN_TILESHEET = 64;
    public static final int CURSOR_Y_IN_TILESHEET = 40;
    public static final int DEFAULT_CURSOR = 0;
    public static final int PANNING_CURSOR = 1;

    private static GameState gameState = GameState.MENU_STATE;
    private static int width;
    private static int height;
    private static Point viewportLocation = new Point();
    private static int currentMouseCursor = DEFAULT_CURSOR;
    private Canvas display_parent;
    private Thread gameThread;
    private boolean running;
    private Thread sleeperThread;
    private boolean requestResetViewPort;
    private Input input;
    private Painter painter;
    private NetworkConnection network;
    private GameCore core;
    private GameMenu menu = new GameMenu();

    public enum GameState {
        MENU_STATE,
        CLIENT_STATE,
        SERVER_STATE;
    }

    public static GameState getGameState() {
        return gameState;
    }

    public static void setGameState(GameState newState) {
        gameState = newState;
    }

    public static Point getViewportLocation() {
        return viewportLocation;
    }

    public static void setViewportLocation(Point p) {
        viewportLocation = p;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getCurrentMouseCursor() {
        return currentMouseCursor;
    }

    public static Point getCurrentMouseCursorTileSheetPoint() {
        return new Point(CURSOR_X_IN_TILESHEET + (currentMouseCursor * (TILE_BASE_WIDTH / 2) % TILE_BASE_WIDTH),
                CURSOR_Y_IN_TILESHEET + (currentMouseCursor * (TILE_BASE_WIDTH / 2) / TILE_BASE_WIDTH) * TILE_BASE_WIDTH);
    }

    public static void setCurrentMouseCursor(int which) {
        currentMouseCursor = which;
    }

    public GameMenu getMenu() {
        return menu;
    }

    public void startLWJGL(final Canvas display_parent) {
        this.display_parent = display_parent;

        final GameLoop self = this;

        gameThread = new Thread() {
            public void run() {
                running = true;
                try {
                    Display.setParent(display_parent);
                    Display.setTitle("ardust");
                    Display.create();
                    Mouse.setNativeCursor(new org.lwjgl.input.Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null));
                    self.start();
                    resetViewPort();
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
                gameLoop();
            }
        };


        gameThread.start();

        sleeperThread = new Thread("High precision sleep workaround") {
            public void run() {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                }
            }
        };
        sleeperThread.setDaemon(true);
        sleeperThread.start();
    }

    public void stopLWJGL() {
        stop();
        running = false;
        try {
            if (gameThread != null)
                gameThread.join();
            if (sleeperThread != null) {
                sleeperThread.interrupt();
                sleeperThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            network = new NetworkConnection(new Socket("localhost", 53421));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        input = new Input();
        painter = new Painter();
        painter.setScale(PIXEL_SCALE);
        painter.init();
        core = new GameCore(network, input, painter);
        core.start();
    }

    private void stop() {
        core.stop();
        network.stop();
    }

    public void resized() {
        requestResetViewPort = true;
    }

    void resetViewPort() {
        width = display_parent.getParent().getWidth();
        height = display_parent.getParent().getHeight();
        display_parent.setSize(width, height);
        input.setHeight(height);
        painter.setScreenDimensions(width, height);
        GL11.glViewport(0, 0, width, height);
        setupRenderMode();
    }

    private void setupRenderMode() {
        Display.setVSyncEnabled(true);
        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.8f); // cutoff ?

        // for cursor
        GL11.glLogicOp(GL11.GL_XOR);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        GL11.glShadeModel(GL11.GL_SMOOTH);


        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // flat zero based surface, 0x0 at the top left
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    private void gameLoop() {
        try {
            long deadline = Sys.getTime();
            long timerResolution = Sys.getTimerResolution();
            long fpsTimer = Sys.getTime() + timerResolution;
            int frames = 0;
            while (running) {
                if (fpsTimer <= Sys.getTime()) {
                    double f = frames;
                    frames = 0;
                    double duration = (Sys.getTime() - fpsTimer) + timerResolution;
                    duration /= timerResolution;
                    f /= duration;
                    System.err.println("fps: " + f);
                    fpsTimer = Sys.getTime() + timerResolution;
                }
                frames++;

                int error = GL11.glGetError();
                if (error != GL11.GL_NO_ERROR) {
                    System.err.println("OpenGL Error: (" + error + ") ");
                }

                if (requestResetViewPort) {
                    requestResetViewPort = false;
                    resetViewPort();
                }

                input.tick();
                //update

                switch (gameState) {

                    case MENU_STATE:
                        break;

                    case CLIENT_STATE:

                        core.tick();

                        //soundmanager.tick();

                        //clear
                        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT + GL11.GL_COLOR_BUFFER_BIT); // assuming we need one

                        //render

                        core.render();
                        break;

                    case SERVER_STATE:
                        break;

                }


                painter.start();

                painter.draw(input.getX() / PIXEL_SCALE, input.getY() / PIXEL_SCALE, getCurrentMouseCursorTileSheetPoint().x,
                        getCurrentMouseCursorTileSheetPoint().y, TILE_BASE_WIDTH / 2, TILE_BASE_WIDTH / 2);

                painter.flush();

                //flip

                Thread.yield();
                Display.update(false);
                if (Display.isCloseRequested()) {
                    running = false;
                }

                // sync
                long nextDeadline = Sys.getTime() + (timerResolution * 16) / 1000;
                while (true) {
                    long delta = deadline - Sys.getTime();
                    if (delta < 5)
                        break;
                    Thread.sleep(1);
                }
                while (true) {
                    long delta = deadline - Sys.getTime();
                    if (delta < 2)
                        break;
                    Thread.sleep(0);
                }
                // spinwait
                while (true) {
                    if (deadline - Sys.getTime() <= 0)
                        break;
                }
                deadline = nextDeadline;

                Display.processMessages();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
