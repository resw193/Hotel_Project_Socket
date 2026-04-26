package client.presentation.menu;

public class MenuAction {
    private boolean cancel = false;

    protected boolean isCancel() {
        return cancel;
    }

    public void cancel() {
        this.cancel = true;
    }
}
