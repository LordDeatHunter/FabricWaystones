package wraith.fwaystones.screen;

import net.minecraft.text.Text;

public class Button {

    protected Text tooltip = null;
    private int x;
    private int y;
    private int u;
    private int v;
    private int height;
    private int width;

    public Button(int x, int y, int width, int height, int u, int v) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.height = height;
        this.width = width;
    }

    public boolean isInBounds(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getU() {
        return u;
    }

    public void setU(int u) {
        this.u = u;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void onClick() {}

    public void setup() {}

    public boolean isVisible() {
        return true;
    }

    public boolean hasToolTip() {return this.tooltip != null;}

    public Text tooltip() {return this.tooltip;}

}
