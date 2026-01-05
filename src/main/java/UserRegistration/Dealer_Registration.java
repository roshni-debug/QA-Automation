/*
 * =================== TEST CASE STEPS: DEALER REGISTRATION (GST ONLY) ====================
 *
 * 1. Get user input (mobile, PAN, email, GST status)
 * 2. Launch Chrome, navigate to login page
 * 3. Click Login/Register
 * 4. Click Register Here
 * 5. Click Dealer Registration Tab
 * 6. GST must be Yes (Non-GST blocked)
 * 7. Enter PAN number
 * 8. Handle popup
 * 9. Enter Email, Mobile, Pin, Address
 * 10. Enter/principal/FADA
 * 11. File Upload
 * 12. Submit form
 * 13. Fetch DB OTP + enter
 * 14. Complete registrationAAACB2894G
 * ========================================================================================
 */

package UserRegistration;

import java.io.File;
import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Dealer_Registration {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String mobileNumber;
    private static String panNumber;
    private static String emailID;
    private static String gstStatus;
    private Scanner scanner;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        scanner = new Scanner(System.in);
        driver.manage().window().maximize();
        System.out.println("Launching Dealer Registration...");
    }

    
    @Test(priority = 1)
    public void testInputMobileNumber() {
        System.out.println("Step 1: Enter Mobile Number (10-digits only)");
        mobileNumber = promptForMobileNumber(scanner);
        Assert.assertEquals(mobileNumber.length(), 10, "Mobile number is not 10 digits.");
        Assert.assertTrue(mobileNumber.matches("\\d{10}"), "Mobile number must be digits only.");
    }

    @Test(priority = 2)
    public void testInputPanNumber() {
        System.out.println("Step 2: Enter PAN Number (10-characters only)");
        panNumber = promptForPanNumber(scanner);
        Assert.assertEquals(panNumber.length(), 10, "PAN number is not 10 characters.");
    }

    @Test(priority = 3)
    public void testInputEmail() {
        System.out.println("Step 3: Enter Email ID");
        System.out.print("Enter correct email ID: ");
        emailID = scanner.nextLine().trim();
        Assert.assertTrue(emailID.length() > 5 && emailID.contains("@"), "Invalid Email ID.");
    }

    @Test(priority = 4)
    public void testInputGstRegistered() {
        System.out.println("Step 4: GST Registered (MUST be yes for Dealer!)");
        while (true) {
            System.out.print("Are you GST registered? (yes): ");
            gstStatus = scanner.nextLine().trim().toLowerCase();
            if ("yes".equals(gstStatus)) break;
            System.err.println("Non-GST user cannot register as Dealer. Registration blocked.");
            Assert.fail("Dealer registration allowed for GST registered users ONLY.");
        }
    }

    @Test(priority = 5)
    public void testOpenSiteAndBeginRegistration() {
        System.out.println("Step 5: Open site and click registration flows");
        try {
            driver.get("https://digielv.mmcm.in/");
            driver.findElement(By.xpath("//button[contains(text(),'Login/Register')]")).click();
            driver.findElement(By.xpath("//a[contains(text(),'Register Here')]")).click();
            driver.findElement(By.xpath("//div[contains(text(),'CD Buyer Registration')]")).click();
            // GST (must be Yes, per logic above)
            driver.findElement(By.xpath("//label[contains(text(), 'Yes')]")).click();
            System.out.println("GST Registration selected (ONLY GST allowed).");
            // Register as Dealer (always yes in this test)
            driver.findElement(By.xpath("//label[normalize-space(text())='Yes' and @for='dealerYesOption']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Site Navigation/DealerTab]: " + e.getMessage());
        }
    }

    @Test(priority = 6)
    public void testEnterPanAndEmail() {
        System.out.println("Step 6: Enter PAN and Email ID in form");
        try {
            WebElement panNo = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Pan No\"]"));
            panNo.sendKeys(panNumber);

            // Handle popup
            WebDriverWait popup = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement popclick = popup.until(
                    ExpectedConditions.elementToBeClickable(By.xpath(" //button[contains(text(), 'Got it')]")));
            popclick.click();

            // Enter Email
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Email\"]"))
                    .sendKeys(emailID);
        } catch (Exception e) {
            Assert.fail("FAILED [PAN/Email Entry]: " + e.getMessage());
        }
    }

    @Test(priority = 7)
    public void testEnterMobileAndPin() {
        System.out.println("Step 7: Enter Mobile Number and Pincode/Address");
        try {
            WebElement phoneInput = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your mobile number\"]"));
            phoneInput.sendKeys(mobileNumber);

            // Enter Pin Code
            driver.findElement(By.xpath("//*[@name=\"undefined\"]")).sendKeys("401107");

            // Select first auto-suggested pin option
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
            // Enter Address
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Address\" and @formcontrolname=\"org_address\"]"))
                    .sendKeys("Address");
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Pincode/Address Entry]: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    public void testFillDealerDetailsAndFileUpload() {
        System.out.println("Step 8: Fill Dealer Details & File Upload (GST ONLY)");
        try {
            // GST is always yes, so these steps should always run
            selectGSTDropdownOption(driver);

            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"contact_name\"]"))
                    .sendKeys("Person Name");

            driver.findElement(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\" and @formcontrolname=\"contact_person_no\"]"))
                    .sendKeys("9023789409");

            // Principal Details
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"principal_name\"]"))
                    .sendKeys("Person Name");
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Email\" and @formcontrolname=\"principal_email\"]"))
                    .sendKeys(emailID);
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your principal mobile number\" and @formcontrolname=\"principal_mobile\"]"))
                    .sendKeys(mobileNumber);

            // FADA
            driver.findElement(By.xpath("//span[@aria-label='Are you FADA Member ']")).click();
            driver.findElement(By.xpath("//li[@role='option' and @aria-label='Yes']")).click();
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your FADA Number\"]")).sendKeys("12374537");

            selectVehicleDropdownOption(driver);
            selectMakeDropdownOption(driver);

            // File Upload (dummy file path)
            try {
                uploadFile(driver, "formFile0", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
                uploadFile(driver, "formFile1", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
                uploadFile(driver, "formFile2", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
                uploadFile(driver, "formFile3", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
                uploadFile(driver, "formFile4", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
            } catch (Exception e) {
                System.out.println("File Upload Failed: " + e.getMessage());
            }
        } catch (Exception e) {
            Assert.fail("FAILED [Dealer Details/Upload]: " + e.getMessage());
        }
    }

    @Test(priority = 9)
    public void testSubmitFormAndEnterOTP() throws InterruptedException {
        System.out.println("Step 9: Submit form & enter OTP");
        try {
            WebDriverWait waitSubmit = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement submitButton = waitSubmit.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@class='btn btn-primary w-10']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
            System.out.println("Submit clicked. Fetching OTP from database...");

            // Fetch OTP from DB using user input mobile number
            String otp = fetchOtpFromDatabase(mobileNumber);
            Assert.assertNotNull(otp, "OTP not found after registration submit");
            Assert.assertEquals(otp.length(), 6, "OTP should be 6 digits");

            // Enter OTP
            try {
                WebDriverWait otpWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                List<WebElement> otpInputs = otpWait.until(
                        ExpectedConditions.visibilityOfAllElementsLocatedBy(
                                By.xpath("//input[contains(@class, 'p-inputotp-input')]")
                        ));
                for (int i = 0; i < 6 && i < otpInputs.size(); i++) {
                    WebElement input = otpInputs.get(i);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
                    input.click();
                    input.clear();
                    input.sendKeys(Character.toString(otp.charAt(i)));
                    Thread.sleep(100);
                }
                System.out.println("OTP entered: " + otp);
            } catch (Exception e) {
                Assert.fail("Error entering OTP: " + e.getMessage());
            }
        } catch (Exception e) {
            Assert.fail("FAILED [Submit/Fetch OTP]: " + e.getMessage());
        }
    }

    @Test(priority = 10)
    public void testClickActionAndContinue() {
        System.out.println("Step 10: Click Action button and Continue");
        try {
            WebDriverWait actionBtn = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement btn = actionBtn.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"actionBtn\"]")));
            btn.click();

            WebDriverWait Continue = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement continueBtn = Continue.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/app-registration-modal[2]/section/div/div/div/div[3]/button")));
            continueBtn.click();

            System.out.println("Dealer Registration (GST only) Test flow completed successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [Action Button/Continue]: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null)
            driver.quit();
        System.out.println("Test execution finished.");
        if (scanner != null) scanner.close();
    }

    // Helper: Only accept exactly 10 digits for mobile number from console
    public static String promptForMobileNumber(Scanner scanner) {
        String mobileNumber;
        while (true) {
            System.out.print("Enter 10-digit Mobile Number: ");
            mobileNumber = scanner.nextLine().replaceAll("\\D", "");
            if (mobileNumber.length() == 10) {
                return mobileNumber;
            }
            System.out.println("Invalid Mobile Number. Only 10 digits allowed, try again.");
        }
    }

    // Helper: Only accept exactly 10 chars for PAN from console
    public static String promptForPanNumber(Scanner scanner) {
        String panNumber;
        while (true) {
            System.out.print("Enter 10-character PAN Number: ");
            panNumber = scanner.nextLine().trim();
            if (panNumber.length() == 10) {
                return panNumber;
            }
            System.out.println("Invalid PAN. Only 10 characters allowed, try again.");
        }
    }

    // Enter Vehicle Details
    private static void selectVehicleDropdownOption(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement gstDropdownTrigger = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@aria-label=\"Select a Vehicle Category\"]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", gstDropdownTrigger);
            WebElement dropdownPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[@aria-label=\"Option List\"]")
            ));
            WebElement firstOption = dropdownPanel.findElement(By.xpath("//*[@aria-label=\"THREE WHEELER T\"]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstOption);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstOption);
            System.out.println("Selected THREE WHEELER T option: " + firstOption.getText());
        } catch (Exception e) {
            System.out.println("Error selecting vehicle type dropdown option: " + e.getMessage());
        }
    }

    // Enter Make Details
    private static void selectMakeDropdownOption(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement gstDropdownTrigger = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@aria-label=\"Select a Vehicle Make\"]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", gstDropdownTrigger);
            WebElement dropdownPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[@aria-label=\"Option List\"]")
            ));
            WebElement firstOption = dropdownPanel.findElement(By.xpath("//*[@aria-label=\"BAJAJ\"]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstOption);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstOption);
            System.out.println("Selected Make option: " + firstOption.getText());
        } catch (Exception e) {
            System.out.println("Error selecting make dropdown option: " + e.getMessage());
        }
    }

    // File Uploading
    public static void uploadFile(WebDriver driver, String inputId, String filePath) throws Exception {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(inputId)));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].classList.remove('d-none'); arguments[0].style.display='block'; arguments[0].style.visibility='visible';",
                fileInput
        );
        fileInput.sendKeys(filePath);
        System.out.println("File uploaded: " + filePath);
    }

    // Method to select GST dropdown option
    private static void selectGSTDropdownOption(WebDriver driver) throws InterruptedException {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement gstDropdownTrigger = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-dropdown[@formcontrolname='org_gst']//div[contains(@class,'p-dropdown-trigger')]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", gstDropdownTrigger);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", gstDropdownTrigger);
            System.out.println("GST Dropdown opened.");
            WebElement dropdownPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'p-dropdown-panel') and contains(@class,'ng-star-inserted')]")
            ));
            List<WebElement> options = dropdownPanel.findElements(By.xpath(".//li[contains(@class,'p-dropdown-item')]"));
            if (options.isEmpty()) {
                System.out.println("No GST options available to select.");
                return;
            }
            WebElement firstOption = options.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstOption);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstOption);
            System.out.println("Selected GST option: " + firstOption.getText());
        } catch (org.openqa.selenium.TimeoutException te) {
            System.out.println("Timeout while waiting for GST dropdown: " + te.getMessage());
        } catch (NoSuchElementException ne) {
            System.out.println("Could not locate GST dropdown element: " + ne.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error selecting GST dropdown option: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fetch OTP from PostgreSQL DB
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        String query = "SELECT otp FROM common.user_mstr WHERE mobile_no = '" + mobileNumber + "'";
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to DB.");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                otp = rs.getString("otp");
                System.out.println("Fetched OTP: " + otp);
            } else {
                System.out.println("No user found with mobile number: " + mobileNumber);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error fetching OTP: " + e.getMessage());
            e.printStackTrace();
        }
        return otp;
    }
}