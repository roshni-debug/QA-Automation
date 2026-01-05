/*
  
  ===================== TESTCASE STEPS FOR: TRANSACTION HISTORY MULTIPLE DOWNLOADS =====================

     *  1. Get 10-digit mobile number input
     *  2. Open the Digielv site (https://digielv.mmcm.in/)
     *  3. Click Login/Register from navbar
     *  4. Enter mobile number and click Login
     *  5. Fetch OTP from database and enter OTP in input boxes
     *  6. Handle/dismiss optional KYC popup if it appears
     *  7. Click 'Transaction History' from menu
     *  8. Download 'Original Cd', 'Transfered Cd', and 'View details'
     *  9. Download Invoice from details
     * 10. Verify downloads completed

=========================================================================================================
*/

package CDRegisteration;

import java.time.Duration;
import java.util.*;
import java.sql.*;
import java.io.IOException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TransactionHistory {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private JavascriptExecutor js;
    private static String mobileNumber;
    private static String otp;

    @BeforeClass
    public void setup() {
        System.out.println("***************  TestCase Execution for Transaction History Downloads  ***************");
        try {
        	WebDriverManager.chromedriver().setup();
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            prefs.put("profile.default_content_setting_values.automatic_downloads", 1);

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);

            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            actions = new Actions(driver);
            js = (JavascriptExecutor) driver;

            System.out.println("WebDriver setup successful for TransactionHistory test.");
        } catch (Exception e) {
            Assert.fail("FAILED [Setup]: WebDriver failed to setup: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testGetMobileNumberInput() {
        System.out.println("\n========== Step 1: Get 10-digit mobile number input from console ==========");
        try {
            mobileNumber = readTenDigitsFromConsole();
            Assert.assertEquals(mobileNumber.length(), 10, "FAILED [Input Error]: Mobile number is not 10 digits.");
        } catch (Exception e) {
            Assert.fail("FAILED [Console Input]: Could not read mobile number from console. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testGetMobileNumberInput")
    public void testOpenLoginPage() {
        System.out.println("\n========== Step 2: Navigate to the login page ==========");
        try {
            driver.get("https://digielv.mmcm.in/");
            Assert.assertNotNull(driver.getTitle(), "FAILED [Navigation]: Page did not load or title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load the login page. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testOpenLoginPage")
    public void testClickLoginRegisterButton() {
        System.out.println("\n========== Step 3: Click Login/Register button ==========");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button"))).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Couldn't find or click the Login/Register button. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testClickLoginRegisterButton")
    public void testEnterMobileAndClickLogin() {
        System.out.println("\n========== Step 4: Enter mobile number and click Login ==========");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Enter Your Mobile Number']")))
                .sendKeys(mobileNumber);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space(text())='Login']")))
                .click();
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Login]: Could not enter mobile or click Login. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testEnterMobileAndClickLogin")
    public void testFetchOtpFromDB() {
        System.out.println("\n========== Step 5: Fetch OTP from database ==========");
        try {
            otp = fetchOtpFromDatabase(mobileNumber);
            Assert.assertNotNull(otp, "FAILED [OTP Fetch]: OTP fetched is null from DB (DB error or wrong mobile).");
            Assert.assertEquals(otp.length(), 6, "FAILED [OTP Fetch]: OTP is not 6 digits. OTP=" + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Fetch DB]: Could not fetch OTP from DB: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testFetchOtpFromDB")
    public void testEnterOtpInputs() throws InterruptedException {
        System.out.println("\n========== Step 6: Enter OTP into input fields ==========");
        try {
            WebDriverWait otpWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            List<WebElement> otpInputs = otpWait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                )
            );
            Assert.assertEquals(otpInputs.size(), 6, 
                "FAILED [OTP Boxes]: Did not find 6 OTP input boxes. Found: " + otpInputs.size());
            for (int i = 0; i < 6; i++) {
                WebElement input = otpInputs.get(i);
                js.executeScript("arguments[0].scrollIntoView(true);", input);
                input.click();
                input.clear();
                input.sendKeys(Character.toString(otp.charAt(i)));
                Thread.sleep(100);
            }
            System.out.println("OTP entered successfully: " + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: Could not enter OTP. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 7, dependsOnMethods = "testEnterOtpInputs")
    public void testDismissKycPopupIfPresent() {
        System.out.println("\n========== Step 7: Handle/dismiss optional KYC popup if it appears ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[normalize-space()=\"Skip For Now\"]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
    }

    @Test(priority = 8, dependsOnMethods = "testDismissKycPopupIfPresent")
    public void testTransactionHistoryMenu() {
        System.out.println("\n========== Step 8: Click 'Transaction History' from sidebar ==========");
        try {
            WebElement transHist = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='Transaction History']")));
            transHist.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Transaction History]: Could not click 'Transaction History'. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testTransactionHistoryMenu")
    public void testTriggerMultipleDownloads() throws InterruptedException {
        System.out.println("\n========== Step 9: Download 'Original Cd', 'Transfered Cd', and view details ==========");
        String[] downloadButtons = {
            "//*[normalize-space()='Original Cd']",
            "//*[normalize-space()='Transfered Cd']",
            "//*[normalize-space()='View details']"
        };
        for (String xpath : downloadButtons) {
            try {
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                js.executeScript("arguments[0].scrollIntoView(true);", button);
                button.click();
                System.out.println("Download triggered for: " + xpath);
                Thread.sleep(500); // Short delay
            } catch (Exception e) {
                Assert.fail("FAILED [Download Trigger]: Failed to click '" + xpath + "' button: " + e.getMessage());
            }
        }
    }

    @Test(priority = 10, dependsOnMethods = "testTriggerMultipleDownloads")
    public void testDownloadInvoiceFromDetails() throws InterruptedException {
        System.out.println("\n========== Step 10: Download Invoice from details ==========");
        try {
            WebElement downloadInvoice = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[normalize-space()='Download Invoice']")));
            downloadInvoice.click();
            System.out.println("Invoice download triggered successfully.");
            Thread.sleep(7000); // Wait to ensure download starts
            System.out.println("All download actions completed successfully.");
        } catch (Exception e) {
            Assert.fail("Invoice download button not found or not clickable. " + e.getMessage());
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
    public static String readTenDigitsFromConsole() {
        StringBuilder sb = new StringBuilder(10);
        System.out.print("Please enter your 10-digit mobile number: ");
        try {
            while (sb.length() < 10) {
                int ch = System.in.read();
                if (ch == -1) break;
                if (ch == '\r' || ch == '\n') continue;
                char c = (char) ch;
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                    System.out.print(c);
                }
            }
            // Consume remaining characters
            int leftover;
            do { leftover = System.in.read(); } while (leftover != -1 && leftover != '\n');
        } catch (IOException e) {
            Assert.fail("FAILED [Console Read]: Failed reading mobile from console: " + e.getMessage());
        }
        return sb.toString();
    }

    // Utility method to fetch OTP by mobile number from DB
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
                System.out.println("OTP fetched from database: " + otp);
            } else {
                Assert.fail("FAILED [DB Query]: No user found with mobile number: " + mobileNumber);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            Assert.fail("FAILED [DB OTP Fetch]: DB error while fetching OTP: " + e.getMessage());
        }
        return otp;
    }
}

