package ardust.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class Input {
    private boolean[] mouseButtons;
    private boolean[] consumedMouseButton;
    private boolean[] keys;
    private boolean[] consumedKeys;
    private int x, y, dx, dy;
    private int height;
    private Point[] mostRecentClicks;

    public Input() {
        mouseButtons = new boolean[2];        //the old way wasn't working right for me
        mostRecentClicks = new Point[mouseButtons.length];
        for (int i = 0; i < mostRecentClicks.length; i++)
            mostRecentClicks[i] = new Point();
        consumedMouseButton = new boolean[mouseButtons.length];
        keys = new boolean[Keyboard.getKeyCount()];
        consumedKeys = new boolean[keys.length];
    }

    public void tick() {
        int l = mouseButtons.length;
        for (int i = 0; i < l; i++) {
            boolean b = Mouse.isButtonDown(i);
            if (b && !mouseButtons[i]) mostRecentClicks[i] = new Point(Mouse.getX(), Mouse.getY());
            mouseButtons[i] = b;
            if (!b)
                consumedMouseButton[i] = false;
        }
        while (Mouse.next()) {
            int key = Mouse.getEventButton();
            if (key == -1)
                continue;
            boolean state = Mouse.getEventButtonState();
            if (state)
                mouseButtons[key] = true;
            else
                consumedMouseButton[key] = false;
        }

        l = keys.length;
        for (int i = 0; i < l; i++) {
            boolean b = Keyboard.isKeyDown(i);
            keys[i] = b;
            if (!b)
                consumedKeys[i] = false;
        }
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();
            if ((key < 0) || (key >= keys.length))
                continue;
            boolean state = Keyboard.getEventKeyState();
            if (state)
                keys[key] = true;
            else
                consumedKeys[key] = false;
        }
        dx = Mouse.getDX();
        dy = -Mouse.getDY();
        x = Mouse.getX();
        y = (height - 1) - Mouse.getY();
    }

    public Point getMostRecentClick(int button) {
        if (button >= 0 && button < mostRecentClicks.length) {
            return mostRecentClicks[button];
        }
        return new Point(0, 0);
    }

    public boolean isMouseButtonDown(int button, boolean consume) {
        if (mouseButtons[button]) {
            if (consumedMouseButton[button])
                return false;
            if (consume)
                consumedMouseButton[button] = true;
            return true;
        }
        return false;
    }

    public boolean isKeyDown(int key, boolean consume) {
        if (keys[key]) {
            if (consumedKeys[key])
                return false;
            if (consume)
                consumedKeys[key] = true;
            return true;
        }
        return false;
    }

    public int getKeyByName(String shortcut) {
        if (shortcut.equals(""))
            return Keyboard.KEY_NONE;
        int key = Keyboard.getKeyIndex(shortcut);
        if (key != 0)
            return key;
        System.err.println("Unknown key: " + shortcut);
        return key;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getDX() {
        return dx;
    }

    public int getDY() {
        return dy;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
