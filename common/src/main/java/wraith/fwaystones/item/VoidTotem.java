package wraith.fwaystones.item;

public class VoidTotem extends LocalVoidItem {
    public VoidTotem(Properties properties) {
        super(properties);
        this.canTeleport = false;
        this.translationName = "void_totem";
    }
}
