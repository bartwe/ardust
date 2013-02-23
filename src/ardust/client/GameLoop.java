package ardust.client;

import ardust.server.Server;
import ardust.shared.Constants;
import ardust.shared.NetworkConnection;
import ardust.shared.Settings;
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
    private GameState gameState = GameState.MENU_STATE;
    private int width;
    private int height;
    private Point viewportLocation = new Point();
    private int currentMouseCursor = Constants.DEFAULT_CURSOR;
    private Canvas display_parent;
    private Thread gameThread;
    private boolean running;
    private Thread sleeperThread;
    private boolean requestResetViewPort;
    private Input input;
    private Painter painter;
    private NetworkConnection network;
    private GameCore core;
    private GameMenu menu;
    private Server server;
    private long prevT;

    public GameLoop() {
        menu = new GameMenu(this);
    }

    public void startServer() {
        server = new Server();
        server.start();
        try {
            Thread.sleep(200); // give it time to start.
        } catch (InterruptedException e) {
        }
    }

    public void fail(String message) {
        System.err.println("Failure: " + message);
        setGameState(GameState.FAIL_STATE);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState newState) {
        if (running)
            if (newState == GameState.CLIENT_STATE)
                setupCore();
        gameState = newState;
    }

    private void setupCore() {
        try {
            network = new NetworkConnection(new Socket(Settings.hostname, Constants.PORT));
            network.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        core = new GameCore(this, network, input, painter);
        core.start();
    }

    public Point getViewportLocation() {
        return viewportLocation;
    }

    public void setViewportLocation(Point p) {
        viewportLocation.setLocation(p);
    }

    public Point getCurrentMouseCursorTileSheetPoint() {
        return new Point(Constants.CURSOR_X_IN_TILESHEET + (currentMouseCursor * (Constants.TILE_BASE_WIDTH / 2) % Constants.TILE_BASE_WIDTH),
                Constants.CURSOR_Y_IN_TILESHEET + (currentMouseCursor * (Constants.TILE_BASE_WIDTH / 2) / Constants.TILE_BASE_WIDTH) * Constants.TILE_BASE_WIDTH);
    }

    public void setCurrentMouseCursor(int which) {
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
        input = new Input();
        painter = new Painter();
        painter.setScale(Constants.PIXEL_SCALE);
        painter.init();
        if (gameState == GameState.CLIENT_STATE)
            setupCore();
    }

    private void stop() {
        if (server != null)
            server.stop();
        if (core != null)
            core.stop();
        if (network != null)
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
                    System.err.println("fps: " + f + " viewport: " + (getViewportLocation().x / Constants.TILE_BASE_WIDTH) + " , " + (getViewportLocation().y / Constants.TILE_BASE_HEIGHT));
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

                long currentT = Sys.getTime();
                if (prevT == 0)
                    prevT = currentT;
                int deltaT = (int) (((currentT - prevT) * 1000) / Sys.getTimerResolution());
                prevT += (deltaT * Sys.getTimerResolution()) / 1000;


                switch (gameState) {

                    case MENU_STATE:
                        break;

                    case CLIENT_STATE:

                        core.tick(deltaT);

                        //soundmanager.tick();

                        //clear
                        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT + GL11.GL_COLOR_BUFFER_BIT); // assuming we need one

                        //render

                        core.render();
                        break;

                    case FAIL_STATE:

                        GL11.glClearColor(1f, 0, 0, 1f);
                        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT + GL11.GL_COLOR_BUFFER_BIT); // assuming we need one

                }


                painter.start();

                painter.draw(input.getX() / Constants.PIXEL_SCALE, input.getY() / Constants.PIXEL_SCALE, getCurrentMouseCursorTileSheetPoint().x,
                        getCurrentMouseCursorTileSheetPoint().y, Constants.TILE_BASE_WIDTH / 2, Constants.TILE_BASE_WIDTH / 2, false);

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

    public enum GameState {
        MENU_STATE,
        CLIENT_STATE,
        FAIL_STATE
    }
}
