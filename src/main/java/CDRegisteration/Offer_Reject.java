/*
                ========== TEST CASE STEPS: BID REJECTION FLOW ==========
     1. Launch Chrome browser and open DigiELV application.
     2. Enter valid 10-digit mobile number and click Login.
     3. Fetch OTP from the database and enter into the OTP input boxes.
     4. Handle optional KYC cancellation popup (if present).
     5. Navigate to "List of CDs" via sidebar.
     6. Click on "View Bids" button.
     7. Find "Reject" button and click them (Reject bid).
==================================================================================
*/

package CDRegisteration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class Offer_Reject {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private static String mobileNumber;
    private static String otp;
    private Scanner scanner;

    @BeforeClass
    public void setup() {
        System.out.println("************ TEST: Bid Acceptance Flow ************");
        try {
        	WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            actions = new Actions(driver);
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            Assert.fail("FAILED [SETUP]: Could not initialize WebDriver: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testGetMobileNumberInput() {
        System.out.println("\n========== Step 1: Get 10-digit mobile number input ==========");
        try {
            mobileNumber = getMobileNumberFromConsole(scanner);
            Assert.assertEquals(mobileNumber.length(), 10, "FAILED [Input]: Mobile number should be 10 digits.");
        } catch (Exception e) {
            Assert.fail("FAILED [Console Input]: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testGetMobileNumberInput")
    public void testOpenDigielvApplication() {
        System.out.println("\n========== Step 2: Open DigiELV application ==========");
        try {
            driver.get("https://digielv.mmcm.in/");
            Assert.assertTrue(driver.getTitle() != null, "FAILED [Page Load]: Title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load DigiELV. " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testOpenDigielvApplication")
    public void testLoginFlow() {
        System.out.println("\n========== Step 3: Click Login/Register & Enter mobile ==========");
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
            driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
            driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Button/Field not found/clicked. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testLoginFlow")
    public void testFetchOtpFromDB() {
        System.out.println("\n========== Step 4: Fetch OTP from database ==========");
        try {
            otp = fetchOtpFromDatabase(mobileNumber);
            Assert.assertNotNull(otp, "FAILED [OTP Fetch]: OTP is null from DB.");
            Assert.assertEquals(otp.length(), 6, "FAILED [OTP Fetch]: OTP must be 6 digits. OTP=" + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Fetch]: " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testFetchOtpFromDB")
    public void testEnterOtpInputs() throws InterruptedException {
        System.out.println("\n========== Step 5: Enter OTP ==========");
        try {
            List<WebElement> otpInputs = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(
                            By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                    )
            );
            Assert.assertEquals(otpInputs.size(), 6, "FAILED [OTP Boxes]: Should be 6.");
            for (int i = 0; i < 6; i++) {
                WebElement input = otpInputs.get(i);
                input.clear();
                input.sendKeys(Character.toString(otp.charAt(i)));
                Thread.sleep(100);
            }
            System.out.println("OTP entered successfully!");
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testEnterOtpInputs")
    public void testHandleKycPopupIfPresent() {
        System.out.println("\n========== Step 6: Handle optional KYC popup ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[normalize-space()='Skip For Now']")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC popup appeared and was dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC popup appeared. Continuing to next step.");
        }
    }

    @Test(priority = 7, dependsOnMethods = "testHandleKycPopupIfPresent")
    public void testNavigateToListOfCDs() {
        System.out.println("\n========== Step 7: Navigate to 'List of CDs' ==========");
        try {
            WebElement listCDs = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(normalize-space(.), 'List of CDs')]")));
            listCDs.click();
            System.out.println("'List of CDs' clicked.");
        } catch (Exception e) {
            Assert.fail("FAILED [Navigate/List of CDs]: " + e.getMessage());
        }
    }

    @Test(priority = 8, dependsOnMethods = "testNavigateToListOfCDs")
    public void testClickViewBids() {
        System.out.println("\n========== Step 8: Click 'View Bids' ==========");
        try {
            WebElement viewBids = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class,'btn-danger') and contains(normalize-space(text()), 'View Bids')]")));
            viewBids.click();
            System.out.println("'View Bids' clicked.");
        } catch (Exception e) {
            Assert.fail("FAILED [View Bids Click]: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testClickViewBids")
    public void testClickAllRejectButtons() {
        System.out.println("\n========== Step 9: Reject all Bids ==========");
        try {
            List<WebElement> acceptButtons = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(
                            By.xpath("//button[contains(@class,'btn-danger') and contains(normalize-space(text()), 'Reject')]")
                    )
            );
            System.out.println("Found " + acceptButtons.size() + " Reject buttons. Clicking...");
            for (WebElement btn : acceptButtons) {
                actions.moveToElement(btn).click().perform();
            }
            System.out.println("All Reject buttons clicked successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [Reject Buttons]: " + e.getMessage());
        }
    }

    @AfterClass
    public void teardown() {
        System.out.println("Test Execution Completed.");

        if (driver != null) {
            try {
                System.out.println("Logged Out Successfully!");
            } finally {
                driver.quit();   //
            }
        }

        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            updateIsLoggedInInDB(mobileNumber);
        }
    }


    // === Utility Methods ===
    	public static String updateIsLoggedInInDB(String mobileNumber) {
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        String update = "UPDATE common.user_mstr SET is_logged_in = 0 WHERE mobile_no = ?";

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement pstmt = conn.prepareStatement(update);
            pstmt.setLong(1, Long.parseLong(mobileNumber));  
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("is_logged_in reset in DB for mobile: " + mobileNumber);
            } else {
                System.out.println("No row updated for mobile: " + mobileNumber);
            }
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error updating is_logged_in in DB: " + e.getMessage());
        }
		return update;  
    }
    
    	// Utility method to read exactly 10 digits from the console
        public static String getMobileNumberFromConsole(Scanner scanner) {
        String mobileNumber;
        while (true) {
            System.out.print("Please enter your 10-digit mobile number: ");
            mobileNumber = scanner.nextLine();
            if (mobileNumber.length() == 10 && mobileNumber.matches("[0-9]+")) {
                break;
            } else {
                System.out.println("Invalid mobile number. Please enter exactly 10 digits.");
            }
        }
        return mobileNumber;
    }

    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        String query = "SELECT otp FROM common.user_mstr WHERE mobile_no = " + mobileNumber;
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                otp = rs.getString("otp");
            } else {
                System.out.println("No user found with mobile number: " + mobileNumber);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("DB Error: " + e.getMessage());
        }
        return otp;
    }
}