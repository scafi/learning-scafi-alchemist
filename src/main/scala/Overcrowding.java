
public enum Overcrowding {
    NONE,
    AT_RISK,
    OVERCROWDED;

    public static Overcrowding overcrowded() { return OVERCROWDED; }
    public static Overcrowding atRisk() { return AT_RISK; }
    public static Overcrowding none() { return NONE; }

    public int toInt(){
        if(this == NONE) return 0;
        if(this == AT_RISK) return 1;
        return 2;
    }
}

