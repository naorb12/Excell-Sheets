package console;

import engine.Engine;

public class Program {
    public static void main(String[] args) {
        UserInterface userInterface = new UserInterface(new Engine());

        userInterface.loadNewXML();
        userInterface.presentSheet();

        userInterface.setCell();
        userInterface.presentSheet();
    }
}
