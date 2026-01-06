/*
    ==================== TEST CASE STEPS: CREATE BID (Offer Create) ===========================

     * 1. Launch Chrome browser with download preferences set.
     * 2. Navigate to DigiELV login page.
     * 3. Enter mobile number and click login.
     * 4. Fetch OTP from the database and input it on the screen.
     * 5. Handle optional KYC cancellation popup.
     * 6. Navigate to 'Transaction History'.
     * 7. Click 'View All Offers'.
     * 8. Select an offer and click 'Buy Offer'.
     * 9. Enter bid amount in the input field.
     * 10. Click 'Place Bid'.
     * 11. In the modal, click on 'Continue' to finalize bid submission.

==================================================================================
*/

package CDRegisteration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Offer_Create {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String mobileNumber;
    private static String otp;
    private Scanner scanner;

    @BeforeClass
    public void setup() {
        System.out.println("************ TEST: Buyer Flow (Create Bid) ************");
        try {
            WebDriverManager.chromedriver().setup();

            // Configure Chrome download preferences
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            prefs.put("profile.default_content_setting_values.automatic_downloads", 1);

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);
            options.addArguments("--no-first-run");
            options.addArguments("--no-default-browser-check");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--start-maximized");

            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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
            driver.get("https://digielv.mmcm.in/user-login");
            Assert.assertNotNull(driver.getTitle(), "FAILED [Page Load]: Title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load DigiELV. " + e.getMessage());
        }
    }

 /*   @Test(priority = 3, dependsOnMethods = "testOpenDigielvApplication")
    public void testClickLoginRegister() {
        System.out.println("\n========== Step 3: Click Login/Register ==========");
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Button not found/clicked. Reason: " + e.getMessage());
        }
    }*/

    @Test(priority = 4, dependsOnMethods = "testOpenDigielvApplication")
    public void testEnterMobileAndClickLogin() {
        System.out.println("\n========== Step 4: Enter mobile number and click Login ==========");
        try {
            driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
            driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Login]: Could not enter/click. " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testEnterMobileAndClickLogin")
    public void testFetchOtpFromDB() {
        System.out.println("\n========== Step 5: Fetch OTP from database ==========");
        try {
            otp = fetchOtpFromDatabase(mobileNumber);
            Assert.assertNotNull(otp, "FAILED [OTP Fetch]: OTP is null from DB.");
            Assert.assertEquals(otp.length(), 6, "FAILED [OTP Fetch]: OTP must be 6 digits. OTP=" + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Fetch]: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testFetchOtpFromDB")
    public void testEnterOtpInputs() throws InterruptedException {
        System.out.println("\n========== Step 6: Enter OTP ==========");
        try {
            List<WebElement> otpInputs = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                )
            );
            Assert.assertEquals(otpInputs.size(), 6, "FAILED [OTP Boxes]: Should be 6.");
            for (int i = 0; i < 6; i++) {
                WebElement input = otpInputs.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
                input.click();
                input.clear();
                input.sendKeys(Character.toString(otp.charAt(i)));
                Thread.sleep(100);
            }
            System.out.println("OTP entered successfully!");
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: " + e.getMessage());
        }
    }

    @Test(priority = 7, dependsOnMethods = "testEnterOtpInputs")
    public void testHandleKycPopupIfPresent() {
        System.out.println("\n========== Step 7: Handle optional KYC popup ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div[2]/div/div[3]/button[1]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            // No popup appeared; move to next step silently
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
    }

    @Test(priority = 8, dependsOnMethods = "testHandleKycPopupIfPresent")
    public void testNavigateToTransactionHistory() {
        System.out.println("\n========== Step 8: Click on 'Transaction History' ==========");
        try {
            WebElement TransHist = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"sidebar\"]/ul/li[3]/a"))
            );
            TransHist.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Transaction History]: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testNavigateToTransactionHistory")
    public void testClickViewAllOffers() {
        System.out.println("\n========== Step 9: Click on 'View All Offers' ==========");
        try {
            WebElement AllOffer = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-view-all-offer/section/div/div/div[2]/div/div/div[2]/div/button"))
            );
            AllOffer.click();
        } catch (Exception e) {
            Assert.fail("FAILED [View All Offers]: " + e.getMessage());
        }
    }

    @Test(priority = 10, dependsOnMethods = "testClickViewAllOffers")
    public void testClickBuyOffer() {
        System.out.println("\n========== Step 10: Click on 'Buy Offer' ==========");
        try {
            WebElement BuyOffer = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-categorised-offers/section/div[1]/div/div[2]/div/div/div[3]/div/button"))
            );
            BuyOffer.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Buy Offer]: " + e.getMessage());
        }
    }

    @Test(priority = 11, dependsOnMethods = "testClickBuyOffer")
    public void testEnterBidPrice() {
        System.out.println("\n========== Step 11: Enter Bid Amount ==========");
        try {
            WebElement BidEnter = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"integeronly\"]"))
            );
            BidEnter.sendKeys("20000");
        } catch (Exception e) {
            Assert.fail("FAILED [Bid Input]: " + e.getMessage());
        }
    }

    @Test(priority = 12, dependsOnMethods = "testEnterBidPrice")
    public void testPlaceBidButtonClick() {
        System.out.println("\n========== Step 12: Click 'Place Bid' ==========");
        try {
            WebElement BidPlace = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-categorised-offers/section/div[3]/div/div/div[3]/button[2]"))
            );
            BidPlace.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Place Bid]: " + e.getMessage());
        }
    }

    @Test(priority = 13, dependsOnMethods = "testPlaceBidButtonClick")
    public void testContinueOnModalPopup() {
        System.out.println("\n========== Step 13: Click 'Continue' in confirmation Modal ==========");
        try {
            WebElement Continue = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-categorised-offers/section/app-registration-modal[4]/section/div/div/div/div[3]/button"))
            );
            Continue.click();
            System.out.println("Create Bid flow completed successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [Continue Modal]: " + e.getMessage());
        }
    }

    @AfterClass
    public void teardown() {
        System.out.println("Test Execution Completed.");
        if (driver != null) {
            try {
                System.out.println("Logged Out Successfully!");
            } finally {
                driver.quit();
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

    // Util: validate mobile number from console
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

    // Util: Fetch OTP by mobile number from DB
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        String query1 = "SELECT otp FROM common.user_mstr WHERE mobile_no = " + mobileNumber;
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(query1);
            if (rs1.next()) {
                otp = rs1.getString("otp");
            } else {
                System.out.println("No OTP found for mobile number: " + mobileNumber);
            }
            rs1.close();
            stmt1.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error fetching OTP: " + e.getMessage());
        }
        return otp;
    }

}
