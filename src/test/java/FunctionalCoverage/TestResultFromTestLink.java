package FunctionalCoverage;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import junit.framework.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;


public class TestResultFromTestLink {

    public static int[] getTestResult(String testPlanName, String testProjectName){
        TestLinkAPI api=getTestLinkAPI();
        TestPlan testPlan=null;
        try{
            testPlan=api.getTestPlanByName(testPlanName,testProjectName);
        }catch (TestLinkAPIException tex){

        }

        int testPlanId=testPlan.getId();

        TestSuite testSuite[]=api.getTestSuitesForTestPlan(testPlanId);
        int lengthofTestSuite= testSuite.length;

        TestCaseDetails simpleTestCaseDetails=TestCaseDetails.SIMPLE;
        int PASSED=0;
        int FAILED=0;
        int BLOCKED=0;
        int NOT_RUN=0;
        int mismatched=0;
        int total=0;
        int parentIds[]=new int[lengthofTestSuite];
        int testSuiteIds[]=new int[lengthofTestSuite];
        ArrayList<Integer> parentTestSuiteIdxs=new ArrayList<Integer>();
        for (int i = 0; i < lengthofTestSuite; i++) {
            parentIds[i]=testSuite[i].getParentId();
            testSuiteIds[i]=testSuite[i].getId();
        }

        for (int i = 0; i < lengthofTestSuite; i++) {
            boolean found=false;
            for (int j = 0; j < lengthofTestSuite; j++) {
                if(parentIds[i]==testSuiteIds[j]){
                    found=true;
                    break;
                }
            }
            if (!found){
                parentTestSuiteIdxs.add(i);
            }
        }


        for (int i = 0; i < parentTestSuiteIdxs.size(); i++) {
            TestCase simpleDetailsofTestCases[] = api.getTestCasesForTestSuite(testSuiteIds[parentTestSuiteIdxs.get(i)], true, simpleTestCaseDetails);
            for (int j = 0; j < simpleDetailsofTestCases.length; j++) {
                int testCaseId=simpleDetailsofTestCases[j].getId();
                int externalId=Integer.parseInt(simpleDetailsofTestCases[j].getFullExternalId().replaceAll("[^0-9.]", ""));
                Execution currentTestCaseExecution=null;
                try {
                    currentTestCaseExecution = api.getLastExecutionResult(testPlanId, testCaseId, externalId);
                }catch (TestLinkAPIException tex){
                    mismatched++;
                }
                String status="Not Defined";
                try {
                    status=currentTestCaseExecution.getStatus().name();
                }catch (NullPointerException ne){
                    NOT_RUN++;
                }

                if("PASSED".equals(status)){
                    PASSED++;
                }else if ("FAILED".equals(status)){
                    FAILED++;
                }else if("BLOCKED".equals(status)){
                    BLOCKED++;
                }
            }
        }

        NOT_RUN=NOT_RUN-mismatched;
        total=PASSED+FAILED+BLOCKED+NOT_RUN;
        int[] returnArray={PASSED,FAILED,BLOCKED,NOT_RUN,total};
        return returnArray;
    }
    public static List<TestPlanDetails> getTestPlanDetails(){
        TestLinkAPI api=getTestLinkAPI();
        ArrayList<String[]> testPlanList=new ArrayList<String[]>();
        List<TestPlanDetails> testPlanDetailsList=new ArrayList<TestPlanDetails>();

        TestProject testProjects[]=api.getProjects();
        for (int i = 0; i <testProjects.length ; i++) {
            TestPlan testPlans[]=api.getProjectTestPlans(testProjects[i].getId());
            for (int j = 0; j <testPlans.length ; j++) {
                String projectandPlan[]={testProjects[i].getName(),testPlans[j].getName()};
                testPlanList.add(projectandPlan);
            }
        }
        int lengthoftestPlanList=testPlanList.size();
        System.out.println("There are "+lengthoftestPlanList+" test plans for today.");
        IntStream.range(0,lengthoftestPlanList).parallel().forEach(i->{
            int returnVal[]=TestResultFromTestLink.getTestResult(testPlanList.get(i)[1],testPlanList.get(i)[0]);
            System.out.println(i+" Fetching data for test plan "+testPlanList.get(i)[1]+" of test project "+testPlanList.get(i)[0]);
            testPlanDetailsList.add(new TestPlanDetails(testPlanList.get(i)[0],testPlanList.get(i)[1],returnVal[0],
                    returnVal[1],returnVal[2],returnVal[3],returnVal[4]));
        });


        return testPlanDetailsList;
    }
    public static void main(String [] args){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        final String todayDate=dateFormat.format(date);
        List<TestPlanDetails> testPlanDetailsList= getTestPlanDetails();
        String query1=Constants.INSERT_FUNCCOVERAGE_SNAPSHOT_DETAILS;
        try {
            int ret=MysqlConnect.insertIntoSnapshotTable(query1,todayDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String query2=Constants.GET_FUNCCOVERAGE_SNAPSHOT_ID;
        try {
            final int snapshot_id=MysqlConnect.getLastSnapshotId(query2);
            final String query3=Constants.INSERT_DAILY_FUNCCOVERAGE_DETAILS;
            IntStream.range(0,testPlanDetailsList.size()).parallel().forEach(i->{
                String project_name=testPlanDetailsList.get(i).getProjectName();
                String test_plan_name=testPlanDetailsList.get(i).getTestPlanName();
                int total_features=testPlanDetailsList.get(i).getTotalNumberofTestCases();
                int passed_features=testPlanDetailsList.get(i).getNumberofPassedTestCases();
                int failed_features=testPlanDetailsList.get(i).getNumberofFailedTestCases();
                int blocked_features=testPlanDetailsList.get(i).getNumberofBlockedTestCases();
                int not_run_features=testPlanDetailsList.get(i).getNumberofNotRunTestCases();
                float functional_coverage= ((float)passed_features/(float)total_features)*100;
                try {
                    int ret=MysqlConnect.insertIntoDailyFunctionalCoverageTable(query3,snapshot_id,todayDate,project_name,test_plan_name,
                            total_features,passed_features,failed_features,blocked_features,not_run_features,functional_coverage);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static TestLinkAPI getTestLinkAPI(){
        String url = Constants.getTestLinkUrl();
        String devKey = Constants.getDevKey();
        TestLinkAPI api = null;

        URL testlinkURL = null;

        try {
            testlinkURL = new URL(url);
        } catch (MalformedURLException mue) {
            mue.printStackTrace(System.err);
            Assert.fail(mue.getMessage());
        }

        try {
            api = new TestLinkAPI(testlinkURL, devKey);
        } catch (TestLinkAPIException te) {
            te.printStackTrace(System.err);
            Assert.fail(te.getMessage());
        }
        return api;
    }
}
