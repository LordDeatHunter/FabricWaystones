package wraith.fwaystones.screen;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.registry.MenuReg;
import wraith.fwaystones.util.PacketHandler;

import java.util.UUID;
import java.util.function.Function;

public class WaystoneMenu extends Universalmenu{
	private final boolean isClient;
	private String name;
	private String hash;
	private UUID owner;
	private boolean isGlobal;
	private Function<Player, Boolean> canUse = null;
	private String ownerName = "";

	public WaystoneMenu(int syncId, WaystoneBlockEntity waystoneEntity, Player player) {
		super(MenuReg.WAYSTONE_MENU.get(), syncId, player);
		this.hash = waystoneEntity.getHash();
		this.name = waystoneEntity.getWaystoneName();
		this.owner = waystoneEntity.getOwner();
		this.isGlobal = waystoneEntity.isGlobal();
		this.canUse = waystoneEntity::canAccess;
		this.isClient = player.level.isClientSide;
		this.ownerName = waystoneEntity.getOwnerName();
		updateWaystones(player);
	}

	public WaystoneMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
		super(MenuReg.WAYSTONE_MENU.get(), syncId, playerInventory.player);
		this.isClient = playerInventory.player.level.isClientSide;
		CompoundTag tag = buf.readNbt();
		if (tag != null) {
			this.hash = tag.getString("waystone_hash");
			this.name = tag.getString("waystone_name");
			if (tag.contains("waystone_owner")) {
				this.owner = tag.getUUID("waystone_owner");
			}
			if (tag.contains("waystone_owner_name")) {
				this.ownerName = tag.getString("waystone_owner_name");
			}
			this.isGlobal = tag.getBoolean("waystone_is_global");
		}
		updateWaystones(player);
	}

	@Override
	public void onForget(String waystone) {
		if (this.hash.equals(waystone)) {
			closeScreen();
		}
	}

	@Override
	public void updateWaystones(Player player) {
		super.updateWaystones(player);
		if (!player.level.isClientSide) {
			return;
		}
		if (!Waystones.WAYSTONE_STORAGE.containsHash(this.hash)) {
			closeScreen();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return canUse != null ? canUse.apply(player) : true;
	}

	public String getWaystone() {
		return this.hash;
	}

	public void toggleGlobal() {
		if (!isClient) {
			return;
		}
		FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
		CompoundTag tag = new CompoundTag();
		tag.putString("waystone_hash", this.hash);
		if (this.owner != null) {
			tag.putUUID("waystone_owner", this.owner);
		}
		packet.writeNbt(tag);
		NetworkManager.sendToServer(PacketHandler.TOGGLE_GLOBAL_WAYSTONE, packet);
		this.isGlobal = !this.isGlobal;
	}

	public boolean isOwner(Player player) {
		return this.owner != null && this.owner.equals(player.getUUID());
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isGlobal() {
		return this.isGlobal;
	}

	public UUID getOwner() {
		return this.owner;
	}

	public String getOwnerName() {
		return this.ownerName == null ? "" : this.ownerName;
	}

	public void removeOwner() {
		this.owner = null;
		this.ownerName = null;
	}

	public boolean hasOwner() {
		return this.owner != null;
	}
}
