package wraith.waystones.item;

public class VoidTotem extends LocalVoidItem {

    public VoidTotem(Settings settings) {
        super(settings);
        this.canTeleport = false;
        this.translationName = "void_totem";
    }

}
