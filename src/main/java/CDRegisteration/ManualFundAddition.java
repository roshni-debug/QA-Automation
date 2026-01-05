// Improved, robust test case suite with smooth execution & each step as a separate, well-guarded testcase. If a failure occurs, explicit reason is shown using Assert.fail. All user input (mobile/UTR) is taken before test execution (recommended testNG way).

package CDRegisteration;

import java.time.Duration;
import java.util.Scanner;
import java.io.File;
import java.sql.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ManualFundAddition {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String loginMobile;
    private static String utrNo;
    private static String otp;

    // Paths and selectors (easy to change here)
    private static final String baseUrl = "https://digielv.mmcm.in/";
    private static final String fileUploadPath = "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png";
    private static final String fileUploadXpath = "//*[@type=\"file\"]";


    @BeforeClass
    public void setUp() {
        // Take input before all tests
        Scanner sc = new Scanner(System.in);
        do {
            System.out.print("Enter 10 digit mobile: ");
            loginMobile = sc.nextLine().trim();
        } while (!loginMobile.matches("\\d{10}"));
        System.out.print("Enter UTR No: ");
        utrNo = sc.nextLine().trim();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications", "--start-maximized");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(40));
    }

    @Test(priority = 1)
    public void testOpenSite() {
        try {
            driver.get(baseUrl);
            waitForPageLoad();
        } catch (Exception e) {
            Assert.fail("Site open failed: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testOpenSite")
    public void testClickLogin() {
        try {
            forceClick(By.xpath("//*[@id='navbarNav']/ul/li[5]/a/button"));
        } catch (Exception e) {
            Assert.fail("Login button not clickable/found: " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testClickLogin")
    public void testEnterMobile() {
        try {
            type(By.xpath("//input[@placeholder='Enter Your Mobile Number']"), loginMobile);
            forceClick(By.xpath("//button[normalize-space()='Login']"));
        } catch (Exception e) {
            Assert.fail("Mobile number entry/submit failed: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testEnterMobile")
    public void testFetchAndEnterOTP() {
        try {
            otp = fetchOtpWithWait(loginMobile, 90, true);
            enterOTP(otp);
        } catch (Exception e) {
            Assert.fail("OTP fetch/enter failed: " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testFetchAndEnterOTP")
    public void testSkipKYCPopup() {
        try {
            skipKYCIfDisplayed();
        } catch (Exception e) {
            Assert.fail("KYC popup skip logic failed: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testSkipKYCPopup")
    public void testOpenManualFundAddition() {
        try {
            forceClick(By.xpath("//a[normalize-space()='Manual Fund Addition']"));
        } catch (Exception e) {
            Assert.fail("\"Manual Fund Addition\" tab click failed: " + e.getMessage());
        }
    }

    @Test(priority = 7, dependsOnMethods = "testOpenManualFundAddition")
    public void testSelectDepositType() {
        try {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder='Select a Type']")));
            dropdown.click();

            // Try to select 'Offline Payment' (refresh DOM in-between to avoid stale elements)
            By offlineXpath = By.xpath("//*[contains(@aria-label, 'Offline Payment') or contains(text(),'Offline Payment')]");
            WebElement offlineOption = wait.until(ExpectedConditions.elementToBeClickable(offlineXpath));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", offlineOption);
        } catch (TimeoutException te) {
            Assert.fail("Deposit Type dropdown or 'Offline Payment' option not found/clickable. Try to inspect the actual DOM in this state: " + te.getMessage());
        } catch (Exception e) {
            Assert.fail("Deposit Type select failed: " + e.getMessage());
        }
    }

    @Test(priority = 8, dependsOnMethods = "testSelectDepositType")
    public void testEnterUTRNo() {
        try {
            WebElement utrInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder='Enter UTR No']")));
            utrInput.clear();
            utrInput.sendKeys(utrNo);
        } catch (Exception e) {
            Assert.fail("UTR No entry failed: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testEnterUTRNo")
    public void testEnterAmountAndWords() {
        try {
            type(By.xpath("//*[@placeholder='Enter Amount']"), "1000");
            type(By.xpath("//*[@placeholder='Enter Amount in words']"), "Test Thousand");
        } catch (Exception e) {
            Assert.fail("Amount/Amount in words entry failed: " + e.getMessage());
        }
    }

    @Test(priority = 10, dependsOnMethods = "testEnterAmountAndWords")
    public void testDatePickerToday() {
        try {
            WebElement dateInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@placeholder='DD/MM/YYYY']")
                )
            );

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // ✅ Format today's date as dd/MM/yyyy
            java.time.LocalDate today = java.time.LocalDate.now();
            String formattedDate = today.format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );

            // ✅ DIRECTLY set value (bypasses click & navbar issue)
            js.executeScript(
                "arguments[0].value = arguments[1];" +
                "arguments[0].dispatchEvent(new Event('input'));" +
                "arguments[0].dispatchEvent(new Event('change'));",
                dateInput,
                formattedDate
            );

        } catch (Exception e) {
            Assert.fail("Date selection failed (JS set value): " + e.getMessage());
        }
    }


    @Test(priority = 11, dependsOnMethods = "testDatePickerToday")
    public void testFileUpload() {
        try {
            File file = new File(fileUploadPath);
            if (!file.exists()) {
                Assert.fail("Upload file not found at: " + fileUploadPath);
            }
            // Wait for input and make sure it's interactable
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(fileUploadXpath)));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block'; arguments[0].style.visibility='visible'; arguments[0].removeAttribute('disabled');",
                fileInput
            );
            fileInput.sendKeys(fileUploadPath);
        } catch (TimeoutException te) {
            Assert.fail("File input field unavailable on the page: " + te.getMessage());
        } catch (Exception e) {
            Assert.fail("File upload failed: " + e.getMessage());
        }
    }
    
    @Test(priority = 12, dependsOnMethods = "testFileUpload")
    public void testClickAddFunds() {
        try {
            forceClick(By.xpath("//button[normalize-space()='Add Fund']"));
        } catch (Exception e) {
            Assert.fail("\"Fund Add button\" click failed: " + e.getMessage());
        }
    }


    // ------ Utility Methods below ------
  /*  @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    } */

    private void forceClick(By by) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                return;
            } catch (Exception e) {
                sleep(500);
                attempts++;
            }
        }
        Assert.fail("Click failed after 3 attempts for locator: " + by.toString());
    }

    private void type(By by, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.clear();
        el.sendKeys(value);
    }

    private void enterOTP(String otp) {
        wait.until(d -> d.findElements(By.xpath("//input[contains(@class,'p-inputotp-input')]")).size() == 6);
        java.util.List<WebElement> inputs = driver.findElements(By.xpath("//input[contains(@class,'p-inputotp-input')]"));
        for (int i = 0; i < 6; i++) {
            inputs.get(i).clear();
            inputs.get(i).sendKeys(String.valueOf(otp.charAt(i)));
        }
    }

    private void waitForPageLoad() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private void skipKYCIfDisplayed() {
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            WebElement skip = popupWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[normalize-space()='Skip For Now']"))
            );
            skip.click();
        } catch (Exception ignore) { }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }

    // ===== OTP DB Fetcher methods. Fail hard if not found =====
    private static String fetchOtpWithWait(String mobile, int timeoutSec, boolean userTable) {
        long end = System.currentTimeMillis() + timeoutSec * 1000;
        while (System.currentTimeMillis() < end) {
            String otp = userTable ? fetchUserOtp(mobile) : fetchLatestOtp(mobile);
            if (otp != null && otp.length() == 6) return otp;
            sleep(2000);
        }
        Assert.fail("OTP not generated/found for: " + mobile);
        return null;
    }
    private static String fetchUserOtp(String mobile) {
        return fetchOtp("SELECT otp FROM common.user_mstr WHERE mobile_no = ?", mobile);
    }
    private static String fetchLatestOtp(String mobile) {
        return fetchOtp("SELECT otp FROM common.otp_mstr WHERE mobile_no = ?", mobile);
    }
    private static String fetchOtp(String query, String mobile) {
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat",
                "uatuser", "password@123");
             PreparedStatement ps = c.prepareStatement(query)) {
            ps.setLong(1, Long.parseLong(mobile.trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("otp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


