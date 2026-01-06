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

public class ExistDealerBuyerAdd {
    private WebDriver driver;
    private WebDriverWait wait;
    private Scanner scanner;
    private static String loginMobile;
    private static String otp;
    private static String pan;
    
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
    /* ================= LOGIN ================= */

    @Test(dependsOnMethods = "getLoginMobile")
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
    public void AddBid() {
        forceClick(By.xpath("//button[normalize-space()='Add Buyer']"));
    }

    @Test(dependsOnMethods = "AddBid")
    public void enterPan() {
        type(By.xpath("//*[@placeholder=\"Enter your PAN No\"]"), pan);
    }
    
    
    @Test(dependsOnMethods = "enterPan")
    public void selectCategory() {
        forceClick(By.xpath("//*[@aria-label=\"Select a Category\"]"));
      //  type(By.xpath("//*[@role=\"searchbox\"]"), "Light Motor Vehicle");
        forceClick(By.xpath("//*[@aria-label=\"LIGHT MOTOR VEHICLE\"]"));
    }
    
    @Test(dependsOnMethods = "selectCategory")
    public void selectMake() {
        forceClick(By.xpath("//*[@aria-label=\"Select Make\"]"));
        forceClick(By.xpath("//*[@aria-label=\"KIA\"]"));
    }

    @Test(dependsOnMethods = "selectMake")
    public void selectModel() {
        forceClick(By.xpath("//*[@aria-label=\"Select Model\"]"));
        forceClick(By.xpath("//*[@aria-label=\"SELTOS\"]"));
    }
    
    @Test(dependsOnMethods = "selectModel")
    public void selectConfirmBuyer() {
        forceClick(By.xpath("//button[normalize-space()='Confirm Buyer']"));
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
            String otp = userTable ? fetchUserOtp(mobile) : fetchUserOtp(mobile);
            if (otp != null && otp.length() == 6) return otp;
            sleep(2000);
        }
        Assert.fail("OTP not generated for: " + mobile);
        return null;
    }

    private static String fetchUserOtp(String mobile) {
        return fetchOtp("SELECT otp FROM common.user_mstr WHERE mobile_no = ?", mobile);
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
