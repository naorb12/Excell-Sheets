package console;

import engine.manager.SheetManager;

public class Program {
    public static void main(String[] args) {
        UserInterface userInterface = new UserInterface(new SheetManager());
        userInterface.run();

    }
}
