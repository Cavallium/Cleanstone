package rocks.cleanstone.io.vanilla.nbt.type;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rocks.cleanstone.io.vanilla.nbt.TagType;
import rocks.cleanstone.io.vanilla.nbt.VanillaTagType;

public class IntTag extends AbstractTag<Integer> {

    public IntTag(byte[] rawData) {
        super(rawData);
    }

    @Override
    public Integer get() {
        return ByteBuffer.wrap(this.rawData).order(ByteOrder.BIG_ENDIAN).asIntBuffer().get();
    }

    @Override
    public TagType getType() {
        return VanillaTagType.INT;
    }
}
