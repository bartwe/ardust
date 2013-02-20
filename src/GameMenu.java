import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameMenu extends JPanel implements ActionListener {

             public JButton hostGameButton, joinGameButton, quitGameButton;

           public GameMenu() {
               hostGameButton = initializeButton("Host Game");
               joinGameButton = initializeButton("Join Game");
               quitGameButton = initializeButton("Quit Game");
               this.add(hostGameButton);
               this.add(joinGameButton);
               this.add(quitGameButton);
               this.setLayout(new CardLayout());
                            this.setVisible(true);
           }

    private JButton initializeButton(String buttonName) {
        JButton button = new JButton();
        button.setName(buttonName);
        button.addActionListener(this);
        return button;
    }


    public void actionPerformed(ActionEvent e) {

    }
}
