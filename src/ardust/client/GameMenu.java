package ardust.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameMenu extends JPanel implements ActionListener {

    private JButton hostGameButton, joinGameButton, quitGameButton;

    public GameMenu() {
        hostGameButton = initializeButton("Host Game");
        joinGameButton = initializeButton("Join Game");
        quitGameButton = initializeButton("Quit Game");
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridheight = 1;
        c.gridwidth = 3;
        c.insets = new Insets(8, 8, 8, 8);
        this.add(hostGameButton, c);
        this.add(joinGameButton, c);
        this.add(quitGameButton, c);
        this.setVisible(true);
        this.setPreferredSize(new Dimension(100, 200));
        this.setBackground(Color.LIGHT_GRAY);
      //  this.setBounds(frame.getWidth() / 2 - getWidth() / 2, frame.getHeight() / 2 - getHeight() / 2, 100, 200);
    }


    private JButton initializeButton(String buttonName) {
        JButton button = new JButton();
        button.setName(buttonName);
        button.setText(buttonName);
        button.addActionListener(this);
        button.setBackground(Color.GRAY);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4, false));
        button.setPreferredSize((new Dimension(100, 50)));
        return button;
    }


    public void actionPerformed(ActionEvent e) {
         if (e.getSource() == hostGameButton) {
                       this.setEnabled(false);
             this.setVisible(false);
             GameLoop.setGameState(GameLoop.GameState.SERVER_STATE);
         }    else if (e.getSource() == joinGameButton) {
             this.setEnabled(false);
             this.setVisible(false);
             GameLoop.setGameState(GameLoop.GameState.CLIENT_STATE);
        }  else if (e.getSource() == quitGameButton) {
             //Does openGL need to do something here?
            System.exit(0);
        }
    }
}
