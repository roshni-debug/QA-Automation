/*
 * =================== TEST CASE STEPS: CD SELLER REGISTRATION ====================
 *
 * 1. Get user input (mobile, PAN, CD No, GST status)
 * 2. Launch Chrome, navigate to login page
 * 3. Click Login/Register
 * 4. Click Register Here
 * 5. Enter CD No and click Verify
 * 6. Only allow GST Registered (Yes); fail if Non-GST
 * 7. Enter PAN number
 * 8. Handle "Got it" popup
 * 9. Enter Phone Number and Address
 * 10. Enter PIN code & select first suggestion
 * 11. Enter Address again (org_address)
 * 12. Select GST dropdown (MANDATORY)
 * 13. Enter contact person name & number
 * 14. Upload required file & Submit
 * 15. Fetch OTP & enter OTP
 * 16. Click declaration checkboxes and complete
 * ================================================================================
 */


package UserRegistration;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.File;
import java.sql.*;
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

public class Seller_Registration {

    private WebDriver driver;
    private WebDriverWait wait;																																																									
    private static String mobileNumber;
    private static String panNumber;
    private static String cdNo;
    private static String gstStatus;
    private Scanner scanner;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        scanner = new Scanner(System.in);
        driver.manage().window().maximize();
        System.out.println("Launching CD Seller Registration Test...");
    }

    @Test(priority = 1)
    public void testInputUserDetails() {
        // Step 1
        System.out.println("Step 1: User input (mobile, PAN, CD No, GST status)");
        mobileNumber = promptForMobileNumber(scanner);
        panNumber = promptForPanNumber(scanner);
        System.out.print("Enter CD No: ");
        cdNo = scanner.nextLine().trim();
        Assert.assertFalse(cdNo.isEmpty(), "CD No cannot be empty.");
        System.out.print("Are you GST registered? (yes/no): ");
        gstStatus = scanner.nextLine().trim().toLowerCase();
        Assert.assertTrue(gstStatus.equals("yes") || gstStatus.equals("no"), "GST status must be 'yes' or 'no'");
    }

    @Test(priority = 2)
    public void testSiteNavigation() {
        // Step 2-4
        System.out.println("Step 2: Navigating to registration page");
        try {
            driver.get("https://digielv.mmcm.in/");
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Login/Register')]"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Register Here')]"))).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Site Navigation]: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    public void testCdNoAndGstSelection() {
        // Step 5-6
        System.out.println("Step 3: Entering CD No and GST Registered check");
        try {
            WebElement cdInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Enter Your CD No\"]")));
            cdInput.sendKeys(cdNo);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space(text())='Verify' and contains(@class,'btn-primary')]"))).click();

            if (gstStatus.equals("yes")) {
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//label[contains(text(), 'Yes')]"))).click();
                System.out.println("GST Registration selected.");
            } else {
                // Unlike UI, we do NOT allow Non-GST for Seller/Dealer, so hard fail.
                System.out.println("Non-GST user cannot register as Dealer.");
                Assert.fail("Non-GST user cannot register as Dealer.");
            }
        } catch (Exception e) {
            Assert.fail("FAILED [CD No/GST selection]: " + e.getMessage());
        }
    }

    @Test(priority = 4)
    public void testEnterPanNoAndPopup() {
        // Step 7-8
        System.out.println("Step 4: PAN No and Got it popup");
        try {
            WebElement panNoInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Enter Your PAN No\"]")));
            panNoInput.sendKeys(panNumber);

            List<WebElement> buttons = driver.findElements(
                    By.xpath("//button[@class='btn btn-primary w-100 rounded-pill']")
            );

            // Loop through all, click the one that is displayed
            for (WebElement btn : buttons) {
                try {
                    if (btn.isDisplayed() && btn.isEnabled()) {
                        new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.elementToBeClickable(btn));
                        btn.click();
                        System.out.println("Popup button clicked.");
                        break;
                    }
                } catch (Exception e) {
                    // ignore if any specific element not interactable
                }
            }

        } catch (Exception e) {
        	System.out.println("FAILED [PAN No/Popup]: " + e.getMessage());
        }
    }

    @Test(priority = 5)
    public void testEnterContactDetails() {
        // Step 9
        System.out.println("Step 5: Entering contact details (phone/address)");
        try {
            WebElement phoneInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\"]")));
            phoneInput.sendKeys(mobileNumber);

            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Address\"]")).sendKeys("ADDRESS");
        } catch (Exception e) {
            Assert.fail("FAILED [Contact details]: " + e.getMessage());
        }
    }

    @Test(priority = 6)
    public void testEnterPinAndOrgAddress() {
        // Step 10-11
        System.out.println("Step 6: Enter PIN code and organization address");
        try {
            driver.findElement(By.xpath("//input[@placeholder='Type to search Pincode']")).sendKeys("401107");
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                WebElement firstDropdownOption = shortWait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//li[@role='option' and contains(@class, 'p-autocomplete-item')]")
                        )
                );
                System.out.println("PIN code suggestion loaded.");
                firstDropdownOption.click();
            } catch (Exception e) {
                System.out.println("PIN code auto-suggestion failed: " + e.getMessage());
            Assert.fail("FAILED [PIN/org_address]: " + e.getMessage());
        }
        
        finally {}
            
        }
            finally {}       
    }

    @Test(priority = 7)
    public void testSelectGstDropdownAndContactPerson() throws InterruptedException {
        // Step 12-13
        System.out.println("Step 7: GST dropdown and contact person");
        try {
            selectGSTDropdownOption(driver);
            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"contact_name\"]"))
                    .sendKeys("Person Name");
            driver.findElement(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\" and @formcontrolname=\"contact_person_no\"]"))
                    .sendKeys("9023789409");
        } catch (Exception e) {
            Assert.fail("FAILED [GST dropdown/Contact Person]: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    public void testUploadFileAndSubmit() {
        // Step 14
        System.out.println("Step 8: File upload, declaration, and submit");
        try {
            uploadFile(driver, "formFile0", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
        } catch (Exception e) {
            Assert.fail("FAILED [File Upload or Submit]: " + e.getMessage());
        }
    }

    // OTP and declaration handled inside uploadFile for this flow:
    /* @AfterClass
      public void tearDown() {
        if (driver != null)
            driver.quit();
        System.out.println("Test execution finished.");
    } */

    // ============= Helper/Utility Methods =============

    // Only accept exactly 10-digit mobile number
    public static String promptForMobileNumber(Scanner scanner) {
        String mobileNumber;
        while (true) {
            System.out.print("Enter 10-digit Mobile Number: ");
            mobileNumber = scanner.nextLine().replaceAll("\\D", "");
            if (mobileNumber.length() == 10) return mobileNumber;
            System.out.println("Invalid Mobile Number. Only 10 digits allowed, try again.");
        }
    }

    // Only accept exactly 10-char PAN
    public static String promptForPanNumber(Scanner scanner) {
        String panNumber;
        while (true) {
            System.out.print("Enter 10-character PAN Number: ");
            panNumber = scanner.nextLine().trim();
            if (panNumber.length() == 10) return panNumber;
            System.out.println("Invalid PAN. Only 10 characters allowed, try again.");
        }
    }

    // GST dropdown utility
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

    // File upload + Click checkboxes & submit button
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

        // Declaration checkboxes
        String[] checkboxIds = {"inlineCheckbox1", "inlineCheckbox2", "inlineCheckbox3"};
        WebDriverWait wait11 = new WebDriverWait(driver, Duration.ofSeconds(20));
        for (String cid : checkboxIds) {
            try {
                WebElement label = wait11.until(ExpectedConditions.elementToBeClickable(By.cssSelector("label[for='" + cid + "']")));
                label.click();
            } catch (Exception e1) {
                try {
                    WebElement input = wait11.until(ExpectedConditions.presenceOfElementLocated(By.id(cid)));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
                } catch (Exception e2) {
                    System.out.println("Failed to click checkbox with id " + cid);
                    e2.printStackTrace();
                }
            }
        }

        // Submit Button
        WebDriverWait wait4 = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            WebElement submitButton = wait4.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[normalize-space(text())='Submit']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
            System.out.println("Submit button clicked");

            // --- (OTP Handling) ---
            String otp = fetchOtpFromDatabase(mobileNumber);
            Assert.assertNotNull(otp, "OTP not found in DB.");
            Assert.assertEquals(otp.length(), 6, "OTP is not 6 digits.");
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

            // You can add Action button/Continue here if needed for completion.

        } catch (Exception e) {
            System.out.println("Failed to click Submit button or OTP entry failed");
            e.printStackTrace();
        }
    }

    // Fetch OTP from PostgreSQL
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