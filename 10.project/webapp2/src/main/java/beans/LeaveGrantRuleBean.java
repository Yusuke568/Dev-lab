package beans;

public class LeaveGrantRuleBean {
    private int id; // Added to match schema
    private int yearsOfService;
    private int grantDays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    public void setYearsOfService(int yearsOfService) {
        this.yearsOfService = yearsOfService;
    }

    public int getGrantDays() {
        return grantDays;
    }

    public void setGrantDays(int grantDays) {
        this.grantDays = grantDays;
    }
}
