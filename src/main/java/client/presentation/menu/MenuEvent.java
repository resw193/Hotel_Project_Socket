package client.presentation.menu;

public interface MenuEvent {
    void menuSelected(int index, int subIndex, MenuAction action);
}
