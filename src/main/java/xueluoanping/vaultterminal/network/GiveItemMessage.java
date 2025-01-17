package xueluoanping.vaultterminal.network;


import net.minecraft.network.FriendlyByteBuf;

public class GiveItemMessage {
    public final int slotIndex;
    public final int containerId;
    public final boolean isShiftPressed;

    public GiveItemMessage(int containerId, int slotIndex, boolean isShiftPressed) {
        this.slotIndex = slotIndex;
        this.containerId = containerId;
        this.isShiftPressed = isShiftPressed;
    }

    public GiveItemMessage(FriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        slotIndex = buf.readVarInt();
        isShiftPressed = buf.readBoolean();
    }


    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slotIndex);
        buf.writeBoolean(isShiftPressed);
    }


}
