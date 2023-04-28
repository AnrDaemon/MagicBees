package magicbees.main.utils.net;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import magicbees.main.MagicBees;
import magicbees.main.utils.ChunkCoords;
import magicbees.main.utils.LogHelper;
import magicbees.main.utils.VersionInfo;
import magicbees.main.utils.error.InvalidEventTypeIndexException;
import magicbees.tileentity.AuraCharges;
import magicbees.tileentity.ITileEntityAuraCharged;

public class NetworkEventHandler {

    public static final String CHANNEL_NAME = VersionInfo.ModName;

    public static NetworkEventHandler getInstance() {
        return MagicBees.object.netHandler;
    }

    public enum EventType {
        UNKNOWN,
        INVENTORY_UPDATE,
        FLAGS_UPDATE,
        AURA_CHARGE_UPDATE;
    }

    private FMLEventChannel channel;

    public NetworkEventHandler() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL_NAME);
        channel.register(this);
    }

    @SubscribeEvent
    public void onPacket(ServerCustomPacketEvent event) {
        this.parseAndDispatchPacket(event.packet.payload(), ((NetHandlerPlayServer) event.handler).playerEntity);
    }

    @SubscribeEvent
    public void onPacket(ClientCustomPacketEvent event) {
        this.parseAndDispatchPacket(event.packet.payload(), null);
    }

    public void sendInventoryUpdate(TileEntity entity, int slotIndex, ItemStack itemStack) {
        EventInventoryUpdate event = new EventInventoryUpdate(new ChunkCoords(entity), slotIndex, itemStack);
        FMLProxyPacket packet = event.getPacket();

        sendPacket(packet);
    }

    public void sendFlagsUpdate(TileEntity entity, int[] flags) {
        EventFlagsUpdate event = new EventFlagsUpdate(new ChunkCoords(entity), flags);
        FMLProxyPacket packet = event.getPacket();

        sendPacket(packet);
    }

    public <T extends TileEntity & ITileEntityAuraCharged> void sendAuraChargeUpdate(T entity,
            AuraCharges auraCharges) {
        EventAuraChargeUpdate event = new EventAuraChargeUpdate(new ChunkCoords(entity), auraCharges);
        FMLProxyPacket packet = event.getPacket();

        sendPacket(packet);
    }

    private void sendPacket(FMLProxyPacket packet) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            channel.sendToAll(packet);
        } else {
            channel.sendToServer(packet);
        }
    }

    private void parseAndDispatchPacket(ByteBuf packetPayload, EntityPlayerMP player) {
        try {
            DataInputStream data = new DataInputStream(new ByteBufInputStream(packetPayload));

            processEventData(data, player);

            data.close();
        } catch (IOException e) {
            LogHelper.info("DataInputStream had a problem closing. Good for it.");
            // e.printStackTrace();
        }
    }

    private void processEventData(DataInputStream data, EntityPlayerMP player) throws IOException {
        int eventId = data.readInt();

        if (eventId == EventType.INVENTORY_UPDATE.ordinal()) {
            EventInventoryUpdate eventData = new EventInventoryUpdate(data);
            eventData.process(player);
        } else if (eventId == EventType.FLAGS_UPDATE.ordinal()) {
            EventFlagsUpdate eventData = new EventFlagsUpdate(data);
            eventData.process(player);
        } else if (eventId == EventType.AURA_CHARGE_UPDATE.ordinal()) {
            EventAuraChargeUpdate evenData = new EventAuraChargeUpdate(data);
            evenData.process(player);
        } else {
            throw new InvalidEventTypeIndexException("");
        }
    }
}
