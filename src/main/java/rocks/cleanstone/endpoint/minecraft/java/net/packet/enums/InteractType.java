package rocks.cleanstone.endpoint.minecraft.java.net.packet.enums;

public enum InteractType {
    INTERACT(0),
    ATTACK(1),
    INTERACT_AT(2);

    private final int typeID;

    InteractType(int typeID) {
        this.typeID = typeID;
    }

    public static InteractType fromTypeID(int typeID) {
        for (InteractType interactType : InteractType.values()) {
            if (interactType.getTypeID() == typeID) {
                return interactType;
            }
        }

        return null;
    }

    public int getTypeID() {
        return typeID;
    }
}
