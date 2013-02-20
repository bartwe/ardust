import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Game {
    private Canvas display_parent;
    private Thread gameThread;
    private boolean running;
    private int width;
    private int height;
    private Thread sleeperThread;

    public void startLWJGL(final Canvas display_parent) {
        this.display_parent = display_parent;
        gameThread = new Thread() {
            public void run() {
                running = true;
                try {
                    Display.setParent(display_parent);
                    Display.setTitle("ardust");
                    Display.create();
                    width = display_parent.getWidth();
                    height = display_parent.getHeight();
                    GL11.glViewport(0, 0, width, height);
//                    initGL();
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
                    e.printStackTrace();
                }
            }
        };
        sleeperThread.start();
        start();
    }

    public void stopLWJGL() {
        stop();
        running = false;
        try {
            if (gameThread != null)
                gameThread.join();
            if (sleeperThread != null)      {
                sleeperThread.interrupt();
                sleeperThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init() {

    }

    private void start() {

    }

    private void stop() {

    }

    public void resized() {

    }

    private void gameLoop() {
    }

}
