package dte.masteriot.mdp.suertees;

public enum MenuAction {
    LOGOUT(R.id.item_logout); // Add more actions as needed

    private final int id;

    MenuAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MenuAction fromId(int id) {
        for (MenuAction action : values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        return null; // Or throw an exception if preferred
    }
}
