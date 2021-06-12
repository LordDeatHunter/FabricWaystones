package wraith.waystones;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

import java.util.HashSet;

public final class ClientStuff {

    public static void playSound(SoundEvent sound, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, pitch));
    }

    public static int getWaystoneCount() {
        if (WaystonesClient.WAYSTONE_STORAGE != null) {
            return WaystonesClient.WAYSTONE_STORAGE.getHashCount();
        }
        return -1;
    }

    public static HashSet<String> getWaystoneHashes() {
        if (WaystonesClient.WAYSTONE_STORAGE != null) {
            return WaystonesClient.WAYSTONE_STORAGE.getAllHashes();
        }
        return null;
    }

}
