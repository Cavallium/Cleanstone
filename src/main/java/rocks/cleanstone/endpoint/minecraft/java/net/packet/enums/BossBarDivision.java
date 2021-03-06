package rocks.cleanstone.endpoint.minecraft.java.net.packet.enums;

public enum BossBarDivision {
    NO_DIVISION(0),
    SIX_NOTCHES(1),
    TEN_NOTCHES(2),
    TWELVE_NOTCHES(3),
    TWENTY_NOTCHES(4);

    private final int divisionID;

    BossBarDivision(int divisionID) {
        this.divisionID = divisionID;
    }

    public static BossBarDivision fromDivisionID(int divisionID) {
        for (BossBarDivision bossBarDivision : BossBarDivision.values()) {
            if (bossBarDivision.getDivisionID() == divisionID) {
                return bossBarDivision;
            }
        }

        return null;
    }

    public int getDivisionID() {
        return divisionID;
    }
}
