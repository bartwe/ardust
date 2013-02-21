import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Standalone {
    static Canvas display_parent;

    public static void main(String[] args) {
        int width = 910;
        int height = 512;

        JFrame f = new JFrame("Frame");
        f.setFocusTraversalKeysEnabled(false);
        f.getContentPane().setPreferredSize(new Dimension(width, height));
        f.setLocation(300, 200);
        f.setLayout(new BorderLayout());
        f.pack();
        f.setVisible(true);

        try {
            final Game game = new Game();
            game.init();
            display_parent = new Canvas() {
                public final void addNotify() {
                    super.addNotify();
                    game.startLWJGL(display_parent);
                }

                public final void removeNotify() {
                    game.stopLWJGL();
                    super.removeNotify();
                }
            };
            display_parent.setSize(width, height);
            f.add(display_parent);;
            display_parent.setFocusTraversalKeysEnabled(false);
            display_parent.setFocusable(true);
            display_parent.requestFocus();
            display_parent.setIgnoreRepaint(true);
            game.menu = new GameMenu(f.getContentPane());
            f.getContentPane().add(game.menu, BorderLayout.CENTER, 0);
            f.setVisible(true);
            f.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    game.resized();
                }
            });
            f.addWindowListener(new WindowListener() {
                public void windowOpened(WindowEvent e) {
                }

                public void windowClosing(WindowEvent e) {
                    game.stopLWJGL();
                    System.exit(0);
                }

                public void windowClosed(WindowEvent e) {
                }

                public void windowIconified(WindowEvent e) {
                }

                public void windowDeiconified(WindowEvent e) {
                }

                public void windowActivated(WindowEvent e) {
                }

                public void windowDeactivated(WindowEvent e) {
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
