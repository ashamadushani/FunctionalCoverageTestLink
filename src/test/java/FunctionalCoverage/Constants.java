package FunctionalCoverage;

public class Constants {
    private static final String testLinkUrl = "https://testlink.wso2.com/lib/api/xmlrpc/v1/xmlrpc.php";
    private static final String devKey = "226ad5ec4cc8bac9f3b080aba4fe62fc";
    private static final String dbUrl = "jdbc:mysql://localhost:3306/";
    private static final String dbName = "product_quality_dashboard";
    private static final String dbUserName= "root";
    private static final String dbPassword = "mysql";

    public static String getTestLinkUrl() {
        return testLinkUrl;
    }

    public static String getDevKey() {
        return devKey;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getDbUserName() {
        return dbUserName;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static final String GET_FUNCCOVERAGE_SNAPSHOT_ID="SELECT snapshot_id FROM functional_coverage_snapshot  " +
                                                            "ORDER BY snapshot_id DESC LIMIT 1";
    public static final String INSERT_FUNCCOVERAGE_SNAPSHOT_DETAILS="INSERT INTO functional_coverage_snapshot (date)" +
                                                                    " VALUES(?)";
    public static final String INSERT_DAILY_FUNCCOVERAGE_DETAILS="NSERT INTO daily_functional_coverage (snapshot_id," +
                    "date,project_name,test_plan_name,total_features,passed_features,failed_features,blocked_features," +
                    "not_run_features,functional_coverage) VALUES (?,?,?,?,?,?,?,?,?,?)";

}
