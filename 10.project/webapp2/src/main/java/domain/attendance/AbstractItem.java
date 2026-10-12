package domain.attendance;

/**
 * 摘要マスタアイテム (abstract_master)
 */
public class AbstractItem {
    private final int id;
    private final String name;
    private final boolean isWork;
    private final boolean isPaid;
    private final boolean isLegalHoliday;
    private final boolean isPrescribedHoliday;

    public AbstractItem(int id, String name, boolean isWork, boolean isPaid, boolean isLegalHoliday, boolean isPrescribedHoliday) {
        this.id = id;
        this.name = name;
        this.isWork = isWork;
        this.isPaid = isPaid;
        this.isLegalHoliday = isLegalHoliday;
        this.isPrescribedHoliday = isPrescribedHoliday;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isWork() { return isWork; }
    public boolean isPaid() { return isPaid; }
    public boolean isLegalHoliday() { return isLegalHoliday; }
    public boolean isPrescribedHoliday() { return isPrescribedHoliday; }
}
