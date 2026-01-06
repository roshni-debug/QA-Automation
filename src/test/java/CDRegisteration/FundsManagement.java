/*
==================================================================================
    ==================== TEST CASE STEPS: FUNDS MANAGEMENT – ADD FUNDS ===========================

     * 1. Launch Chrome browser and open DigiELV application.
     * 2. Enter valid 10-digit mobile number and click Login.
     * 3. Fetch OTP from the database and enter into the OTP input boxes.
     * 4. Handle optional KYC cancellation popup (if present).
     * 5. Navigate to "Funds Management" from sidebar.
     * 6. Click on "Add Funds".
     * 7. Select a payment method.
     * 8. Click on "Continue" to proceed.
     * 9. Enter amount (e.g., 15000) and remarks (e.g., "Bid Price").
     * 10. Click "Pay Now".
     * 11. Select "Net Banking" option.
     * 12. Choose a bank (e.g., Kotak Bank).
     * 13. Click on "Continue & Pay" to complete the transaction.

==================================================================================
*/
package CDRegisteration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class FundsManagement {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String mobileNumber;
    private static String otp;
    private Scanner scanner;

    @BeforeClass
    public void setup() {
        System.out.println("************ TEST: Funds Management (Add Funds via Net Banking) ************");
        try {
        	WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
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
            driver.get("https://digielv.mmcm.in/");
            Assert.assertNotNull(driver.getTitle(), "FAILED [Page Load]: Title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load DigiELV. " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testOpenDigielvApplication")
    public void testClickLoginRegister() {
        System.out.println("\n========== Step 3: Click Login/Register ==========");
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Button not found/clicked. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testClickLoginRegister")
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
                    By.xpath("//*[normalize-space()=\\\"Skip For Now\\\"]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
    }

    @Test(priority = 8, dependsOnMethods = "testHandleKycPopupIfPresent")
    public void testOpenFundsManagement() {
        System.out.println("\n========== Step 8: Navigate to 'Funds Management' ==========");
        try {
            WebElement sidebarLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Funds Management')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sidebarLink);
            System.out.println("Funds Management clicked successfully!");
        } catch (Exception e) {
            Assert.fail("FAILED [Sidebar Navigation]: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testOpenFundsManagement")
    public void testAddFundsClick() {
        System.out.println("\n========== Step 9: Click on 'Add Funds' ==========");
        try {
            WebElement addFund = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[1]/button")));
            addFund.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Add Funds]: " + e.getMessage());
        }
    }

    @Test(priority = 10, dependsOnMethods = "testAddFundsClick")
    public void testSelectPaymentMethod() {
        System.out.println("\n========== Step 10: Select a payment method ==========");
        try {
            WebElement payMethod = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[2]/div[2]/div/div/div/div[2]/div/div[1]/div/button")));
            payMethod.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Payment Method]: " + e.getMessage());
        }
    }

    @Test(priority = 11, dependsOnMethods = "testSelectPaymentMethod")
    public void testClickContinue() {
        System.out.println("\n========== Step 11: Click Continue ==========");
        try {
            WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[2]/div[2]/div/div/div/div[3]/button[2]")));
            continueBtn.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Continue Click]: " + e.getMessage());
        }
    }

    @Test(priority = 12, dependsOnMethods = "testClickContinue")
    public void testEnterAmountAndRemarks() {
        System.out.println("\n========== Step 12: Enter amount and remarks ==========");
        try {
            WebElement amount = wait.until(ExpectedConditions.elementToBeClickable(By.id("integeronly")));
            amount.click();
            amount.clear();
            amount.sendKeys("15000");

            WebElement remarks = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//textarea[@placeholder='Enter Your Remarks']")));
            remarks.click();
            remarks.clear();
            remarks.sendKeys("Bid Price");

            WebElement payNowButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='pay-btn']")));

            if (payNowButton.isEnabled()) {
                payNowButton.click();
                System.out.println("Clicked Pay Now button!");
            } else {
                Assert.fail("FAILED [Pay Now Button]: Button is disabled.");
            }
        } catch (Exception e) {
            Assert.fail("FAILED [Enter Amount/Remarks]: " + e.getMessage());
        }
    }

    @Test(priority = 13, dependsOnMethods = "testEnterAmountAndRemarks")
    public void testSelectNetBankingMethod() {
        System.out.println("\n========== Step 13: Select Net Banking method ==========");
        try {
            driver.switchTo().frame(0);
            WebElement payMethod = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/div[1]/div[1]/div[2]/div[2]/div/div/div/div/div[1]/div[1]/label[4]/div/div")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", payMethod);
            payMethod.click();

            WebElement KotakBank = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"main-stack-container\"]/div/div/div/div/div[2]/div/div/form[1]/div/label[2]/div")
            ));
            KotakBank.click();
            System.out.println("NetBanking Method selected successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [NetBanking Method]: " + e.getMessage());
        }
    }

    @Test(priority = 14, dependsOnMethods = "testSelectNetBankingMethod")
    public void testContinueAndPay() {
        System.out.println("\n========== Step 14: Click Continue & Pay ==========");
        try {
            WebElement payNow = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='fee-bearer-cta' and contains(normalize-space(.), 'Continue & Pay')]")
            ));
            payNow.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Continue & Pay]: " + e.getMessage());
        }
    }

    @Test(priority = 15, dependsOnMethods = "testContinueAndPay")
    public void testFinalContinueClick() throws InterruptedException {
        System.out.println("\n========== Step 15: Click final Continue button ==========");
        try {
            driver.switchTo().frame(0);
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(50)); // extended as you said
            WebElement payNow = longWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[@type='button' and normalize-space(text())='Continue'])[2]")
            ));
            Thread.sleep(100); // optional, for UI stability
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", payNow);
            System.out.println("Successfully clicked on final Continue button.");
        } catch (Exception e) {
            Assert.fail("FAILED [Final Continue]: " + e.getMessage());
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

