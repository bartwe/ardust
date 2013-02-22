package ardust.client;


import java.util.ArrayList;

public class DwarfActionMenu {

    Character dwarf;

    public enum MenuState
    {
       NO_BUTTON,
       WALK,
       MINE,
       HALT
    }

    public DwarfActionMenu(Character dwarf)
    {
       this.dwarf = dwarf;
    }

    public MenuState isButtonHere(int x, int y )
    {

    }

}
