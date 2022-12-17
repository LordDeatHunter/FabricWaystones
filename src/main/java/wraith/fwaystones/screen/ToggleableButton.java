package wraith.fwaystones.screen;

public class ToggleableButton extends Button {

    protected boolean toggled = false;
    protected int toggleU;
    protected int toggleV;

    public ToggleableButton(int x, int y, int width, int height, int u, int v, int toggleU, int toggleV) {
        super(x, y, width, height, u, v);
        this.toggleU = toggleU;
        this.toggleV = toggleV;
    }

    @Override
    public int getU() {
        if (toggled) {
            return toggleU;
        }
        return super.getU();
    }

    @Override
    public int getV() {
        if (toggled) {
            return toggleV;
        }
        return super.getV();
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    @Override
    public void onClick() {
        this.toggle();
    }

    public boolean isToggled() {
        return this.toggled;
    }

}
