package kafofond.dto;

import lombok.Data;

@Data
public class DsiDashboardStats {
    private int totalUsers;
    private int disabledUsers;
    private int sharedDocuments;
    private double activeUsersPercentage;
    private double disabledUsersPercentage;
    private double documentsPercentage;
}