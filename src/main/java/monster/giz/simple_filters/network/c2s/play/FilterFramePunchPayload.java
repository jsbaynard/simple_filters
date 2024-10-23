package monster.giz.simple_filters.network.c2s.play;

import monster.giz.simple_filters.SimpleFilters;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record FilterFramePunchPayload(BlockPos blockPos) implements CustomPayload {

    public static final CustomPayload.Id<FilterFramePunchPayload> ID = new CustomPayload.Id<>(SimpleFilters.idOf("filter_frame_punch"));

    public static final PacketCodec<RegistryByteBuf, FilterFramePunchPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            FilterFramePunchPayload::blockPos,
            FilterFramePunchPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

}