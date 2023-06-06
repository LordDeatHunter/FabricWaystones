package wraith.fwaystones.screen;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerAccess;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.mixin.ClientPlayerEntityAccessor;
import wraith.fwaystones.mixin.ServerPlayerEntityAccessor;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.SearchType;
import wraith.fwaystones.util.Utils;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class Universalmenu extends AbstractContainerMenu {

	protected final Player player;
	protected ArrayList<String> sortedWaystones = new ArrayList<>();
	protected ArrayList<String> filteredWaystones = new ArrayList<>();
	protected String filter = "";

	protected Universalmenu(
			MenuType<? extends Universalmenu> type, int syncId,
			Player player) {
		super(type, syncId);
		this.player = player;
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				this.addSlot(
						new Slot(this.player.getInventory(), x + y * 9 + 9, 2000000000, 2000000000));
			}
		}

		for (int x = 0; x < 9; ++x) {
			this.addSlot(new Slot(this.player.getInventory(), x, 2000000000, 2000000000));
		}
	}

	public void updateWaystones(Player player) {
		if (!player.level.isClientSide) {
			return;
		}
		this.sortedWaystones = new ArrayList<>();
		if (((PlayerEntityMixinAccess) player).shouldViewDiscoveredWaystones()) {
			this.sortedWaystones.addAll(((PlayerAccess) player).getHashesSorted());
		}
		if (((PlayerEntityMixinAccess) player).shouldViewGlobalWaystones()) {
			for (String waystone : Waystones.WAYSTONE_STORAGE.getGlobals()) {
				if (!this.sortedWaystones.contains(waystone)) {
					this.sortedWaystones.add(waystone);
				}
			}
		}
		this.sortedWaystones.sort(Comparator.comparing(a -> Waystones.WAYSTONE_STORAGE.getName(a)));
		filterWaystones();
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (!player.level.isClientSide) {
			return false;
		}
		Waystones.LOGGER.warn("BUTTON CLICKED 1");
		int waystoneID = Math.floorDiv(id, 2);
		if (waystoneID >= this.filteredWaystones.size()) {
			return false;
		}
		Waystones.LOGGER.warn("BUTTON CLICKED 2");

		String waystone = this.filteredWaystones.get(waystoneID);
		if (waystone == null) {
			return false;
		}
		Waystones.LOGGER.warn("BUTTON CLICKED 3");

		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
		CompoundTag tag = new CompoundTag();
		tag.putString("waystone_hash", waystone);
		data.writeNbt(tag);

		Waystones.LOGGER.warn("BUTTON CLICKED 4");
		if (id % 2 != 0) {
			this.sortedWaystones.remove(waystone);
			this.filteredWaystones.remove(waystone);
			onForget(waystone);
			((PlayerEntityMixinAccess) player).forgetWaystone(waystone);
			updateWaystones(player);
			NetworkManager.sendToServer(PacketHandler.FORGET_WAYSTONE, data);
		} else {
			Waystones.LOGGER.warn("BUTTON CLICKED 5");
			if (Utils.canTeleport(player, waystone, false)) {
				Waystones.LOGGER.warn("BUTTON CLICKED 6");
				NetworkManager.sendToServer(PacketHandler.TELEPORT_TO_WAYSTONE, data);
			}
			closeScreen();
		}
		Waystones.LOGGER.warn("BUTTON CLICKED 7");
		return true;
	}

	protected void closeScreen() {
		if (player == null) {
			return;
		}
		if (player.level.isClientSide) {
			closeOnClient();
		} else {
			((ServerPlayerEntityAccessor) player).getNetworkHandler()
					.send(new ClientboundContainerClosePacket(this.containerId));
			player.containerMenu.removed(player);
			player.containerMenu = player.inventoryMenu;
		}
	}

	protected void closeOnClient() {
		((ClientPlayerEntityAccessor) player).getNetworkHandler()
				.send(new ServerboundContainerClosePacket(this.containerId));

		setCarried(ItemStack.EMPTY);
		player.containerMenu = player.inventoryMenu;
		Minecraft.getInstance().setScreen(null);
	}

	public abstract void onForget(String waystone);

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	public int getWaystonesCount() {
		return this.filteredWaystones.size();
	}

	public ArrayList<String> getSearchedWaystones() {
		return this.filteredWaystones;
	}

	public void setFilter(String filter) {
		this.filter = filter.toLowerCase();
	}

	public void filterWaystones() {
		this.filteredWaystones.clear();
		var searchType = ((PlayerEntityMixinAccess) player).getSearchType();
		for (String waystone : this.sortedWaystones) {
			String name = Waystones.WAYSTONE_STORAGE.getName(waystone).toLowerCase();
			if ("".equals(this.filter) || searchType.match(name, filter)) {
				filteredWaystones.add(waystone);
			}
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	public void toggleSearchType() {
		var playerAccess = (PlayerEntityMixinAccess) player;
		var searchType = playerAccess.getSearchType();
		var searchValues = SearchType.values();
		playerAccess.setSearchType(searchValues[(searchType.ordinal() + 1) % searchValues.length]);
		filterWaystones();
	}

	public Component getSearchTypeTooltip() {
		return Component.translatable("fwaystones.gui." + (((PlayerEntityMixinAccess) player).getSearchType().name().toLowerCase()));
	}
}
