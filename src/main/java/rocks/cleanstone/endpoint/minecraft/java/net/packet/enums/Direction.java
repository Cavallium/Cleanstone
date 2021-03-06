package rocks.cleanstone.endpoint.minecraft.java.net.packet.enums;

public enum Direction {
    SOUTH(0),
    WEST(1),
    NORTH(2),
    EAST(3);

    private final int directionID;

    Direction(int directionID) {
        this.directionID = directionID;
    }

    public static Direction fromDirectionID(int directionID) {
        for (Direction direction : Direction.values()) {
            if (direction.getDirectionID() == directionID) {
                return direction;
            }
        }

        return null;
    }

    public int getDirectionID() {
        return directionID;
    }
}
