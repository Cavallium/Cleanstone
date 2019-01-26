package rocks.cleanstone.net.minecraft.entity.metadata;

public enum EntityMetadataType {
    BYTE(0),
    VAR_INT(1),
    FLOAT(2),
    STRING(3),
    CHAT(4),
    OPTIONAL_CHAT(5),
    SLOT(6),
    BOOLEAN(7),
    ROTATION(8),
    POSITION(9),
    OPTIONAL_POSITION(10),
    DIRECTION(11),
    OPTIONAL_UUID(12),
    OPTIONAL_BLOCK_ID(13),
    NBT(14),
    PARTICLE(15);

    private final int typeID;

    EntityMetadataType(int typeID) {
        this.typeID = typeID;
    }

    public int getTypeID() {
        return typeID;
    }
}