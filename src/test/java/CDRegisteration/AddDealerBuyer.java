
// CONTINUE Button has to include as for now it is not submitting the OTP


package CDRegisteration;

import java.time.Duration;
import java.util.*;
import java.sql.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class AddDealerBuyer {

    private WebDriver driver;
    private WebDriverWait wait;
    private Scanner scanner;
    private static String loginMobile;
    private static String buyerMobile;
    private static String otp;
    private static String pan;
    private static String gstStatus;
    private static final String ADDRESS = "Andheri East, Mumbai";

    /* ================= SETUP ================= */

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        scanner = new Scanner(System.in);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications", "--start-maximized", "--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(40));
    }

    /* ================= INPUT ================= */

    @Test
    public void getLoginMobile() {
        loginMobile = readMobile(scanner);
    }

    @Test(dependsOnMethods = "getLoginMobile")
    public void getPan() {
        pan = readPAN(scanner);
    }

    @Test(dependsOnMethods = "getPan")
    public void getGSTStatus() {
        while (true) {
            System.out.print("GST Registered (yes/no): ");
            gstStatus = scanner.nextLine().trim().toLowerCase();
            if (gstStatus.equals("yes") || gstStatus.equals("no")) break;
        }
    }

    @Test(dependsOnMethods = "getGSTStatus")
    public void getBuyerMobile() {
        buyerMobile = readMobile(scanner);
    }

    /* ================= LOGIN ================= */

    @Test(dependsOnMethods = "getBuyerMobile")
    public void openSite() {
        driver.get("https://digielv.mmcm.in/");
        waitForPageLoad();
    }

    @Test(dependsOnMethods = "openSite")
    public void clickLogin() {
        forceClick(By.xpath("//*[@id='navbarNav']/ul/li[5]/a/button"));
    }

    @Test(dependsOnMethods = "clickLogin")
    public void enterLoginMobile() {
        type(By.xpath("//input[@placeholder='Enter Your Mobile Number']"), loginMobile);
        forceClick(By.xpath("//button[normalize-space()='Login']"));
    }

    @Test(dependsOnMethods = "enterLoginMobile")
    public void fetchLoginOtp() {
        otp = fetchOtpWithWait(loginMobile, 60, true);
        enterOTP(otp);
    }

    @Test(dependsOnMethods = "fetchLoginOtp")
    public void skipKYC() {
        System.out.println("\n========== Step 6: Handle optional KYC popup ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(2));
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

    /* ================= ADD BUYER ================= */

    @Test(dependsOnMethods = "skipKYC")
    public void openAddBuyer() {
        forceClick(By.xpath("//a[normalize-space()='Add Dealer Buyer']"));
    }

    @Test(dependsOnMethods = "openAddBuyer")
    public void enterPan() {
        type(By.xpath("//*[@placeholder='Enter Your Pan No']"), pan);
        forceClick(By.xpath("//button[contains(text(),'Got it')]"));
    }

    @Test(dependsOnMethods = "enterPan")
    public void enterBuyerMobile() {
        type(By.xpath("(//*[@placeholder='Please Enter 10-digit mobile number'])[1]"), buyerMobile);
    }

    @Test(dependsOnMethods = "enterBuyerMobile")
    public void enterOrganizationName() {
        driver.findElement(By.xpath("(//*[@placeholder=\"Enter Your Name\"])[2]")).sendKeys("Organization Name");
    }

    @Test(dependsOnMethods = "enterOrganizationName")
    public void enterEmail() {
        type(By.xpath("//*[@placeholder='Enter Your Email']"), "test@mmcm.in");
    }

    @Test(dependsOnMethods = "enterEmail")
    public void enterAddress() {
        WebElement addr = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("(//*[@placeholder='Enter Your Address'])[1]")));
        scroll(addr);
        addr.sendKeys(ADDRESS);
    }

    @Test(dependsOnMethods = "enterAddress")
    public void enterPincode() {
        driver.findElement(By.xpath("//*[@name=\"undefined\"]")).sendKeys("401107");

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement firstDropdownOption = shortWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[@role='option' and contains(@class, 'p-autocomplete-item')]")));
            System.out.println("PIN code suggestion loaded.");
            firstDropdownOption.click();
        } catch (Exception e) {
            System.out.println("PIN code auto-suggestion failed: " + e.getMessage());
        }
        driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Address\" and @formcontrolname=\"org_address\"]"))
                .sendKeys("Address");
    }

    @Test(dependsOnMethods = "enterPincode")
    public void gstDetails() {
        if (gstStatus.equals("yes")) {
            forceClick(By.xpath("//p-dropdown[@formcontrolname='org_gst']//div[contains(@class,'p-dropdown-trigger')]"));
            forceClick(By.xpath("//li[contains(@class,'p-dropdown-item')]"));
            type(By.xpath("//*[@formcontrolname='contact_name']"), "Test User");
            type(By.xpath("//*[@formcontrolname='contact_person_no']"), "9123456789");
        }
    }

    @Test(dependsOnMethods = "gstDetails")
    public void checkbox() {
        if (gstStatus.equals("yes")) {
            forceClick(By.xpath("(//*[@class='form-check-input mt-1 ng-untouched ng-pristine ng-valid'])[1]"));
        } else if (gstStatus.equals("no")) {
            forceClick(By.xpath("(//*[@class='form-check-input mt-1 ng-untouched ng-pristine ng-valid'])[1]"));
            List<WebElement> checkboxes = driver.findElements(By.xpath("(//*[@class='form-check-input mt-1 ng-untouched ng-pristine ng-valid'])[2]"));
            if (!checkboxes.isEmpty() && checkboxes.get(0).isDisplayed()) {
                forceClick((By) checkboxes.get(0)); 
            }
        }
    }

    @Test(dependsOnMethods = "checkbox")
    public void submitForm() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Register')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);
    }

    /* ================= BUYER OTP ================= */

    @Test(dependsOnMethods = "submitForm")
    public void verifyOtpScreen() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[contains(@class,'p-inputotp-input')]")));
    }

    @Test(dependsOnMethods = "verifyOtpScreen")
    public void fetchBuyerOtp() {
        otp = fetchOtpWithWait(buyerMobile, 90, false);
        enterOTP(otp);
    }

    @Test(dependsOnMethods = "fetchBuyerOtp")
    public void finalOtp() {
        otp = fetchOtpWithWait(buyerMobile, 90, false);
        enterOTP(otp);
    }

    @Test(dependsOnMethods = "finalOtp")
    public void continueFlow() {
        forceClick(By.id("actionBtn"));
        forceClick(By.xpath("//button[normalize-space()='Verify OTP']"));
    }
    
    

    /* ================= TEARDOWN ================= */

    @AfterClass
    public void teardown() {
        if (driver != null) driver.quit();
        updateIsLoggedIn(loginMobile);
    }

    /* ================= UTILITIES ================= */

    private void forceClick(By by) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                return;
            } catch (Exception e) {
                attempts++;
                sleep(500);
            }
        }
        Assert.fail("Click failed: " + by);
    }

    private void type(By by, String val) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.sendKeys(val);
    }

    private void enterOTP(String otp) {
        wait.until(d -> d.findElements(By.xpath("//input[contains(@class,'p-inputotp-input')]")).size() == 6);
        List<WebElement> inputs = driver.findElements(By.xpath("//input[contains(@class,'p-inputotp-input')]"));
        for (int i = 0; i < 6; i++) {
            inputs.get(i).sendKeys(String.valueOf(otp.charAt(i)));
        }
    }

    private void scroll(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
    }

    private void waitForPageLoad() {
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }

    /* ================= OTP DB ================= */

    private static String fetchOtpWithWait(String mobile, int timeoutSec, boolean userTable) {
        long end = System.currentTimeMillis() + timeoutSec * 1000;
        while (System.currentTimeMillis() < end) {
            String otp = userTable ? fetchUserOtp(mobile) : fetchLatestOtp(mobile);
            if (otp != null && otp.length() == 6) return otp;
            sleep(2000);
        }
        Assert.fail("OTP not generated for: " + mobile);
        return null;
    }

    private static String fetchUserOtp(String mobile) {
        return fetchOtp("SELECT otp FROM common.user_mstr WHERE mobile_no = ?", mobile);
    }

    private static String fetchLatestOtp(String mobile) {
        return fetchOtp(
            "SELECT otp FROM common.otp_mstr WHERE mobile_no = ?", mobile
        );
    }

    private static String fetchOtp(String query, String mobile) {
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat",
                "uatuser", "password@123");
             PreparedStatement ps = c.prepareStatement(query)) {

            ps.setLong(1, Long.parseLong(mobile.trim()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String otp = rs.getString("otp");
                System.out.println("OTP fetched from database: " + otp);  // Log the OTP
                return otp;
            } else {
                System.out.println("No OTP found for mobile: " + mobile);  // Log if OTP not found
            }
        } catch (Exception e) {
            e.printStackTrace();  // Print exception stack trace for debugging
        }
        return null;
    }

    private static void updateIsLoggedIn(String mobile) {
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat",
                "uatuser", "password@123");
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE common.user_mstr SET is_logged_in = 0 WHERE mobile_no = ?")) {

            ps.setLong(1, Long.parseLong(mobile));
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    /* ================= INPUT ================= */

    private static String readMobile(Scanner sc) {
        String m;
        do {
            System.out.print("Enter 10 digit mobile: ");
            m = sc.nextLine();
        } while (!m.matches("\\d{10}"));
        return m;
    }

    private static String readPAN(Scanner sc) {
        String p;
        do {
            System.out.print("Enter PAN: ");
            p = sc.nextLine();
        } while (p.length() != 10);
        return p;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
}
