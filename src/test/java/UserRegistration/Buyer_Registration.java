/*
 * =================== TEST CASE STEPS: CD BUYER REGISTRATION ====================
 *
 * 1. Get user input (mobile, PAN, email, GST status)
 * 2. Launch Chrome, navigate to login page
 * 3. Click Login/Register
 * 4. Click Register Here
 * 5. Click Buyer Tab
 * 6. Set GST selection (Yes/No)
 * 7. Enter PAN number
 * 8. Handle "Got it" popup
 * 9. Enter Email
 * 10. Enter Phone Number
 * 11. Enter PIN code & select first suggestion
 * 12. Enter Address
 * 13. Select GST dropdown if applicable
 * 14. Enter contact person name & number (if GST)
 * 15. Submit Registration
 * 16. Fetch OTP & enter OTP
 * 17. Click Action button and Continue
 * =======================================================================
 */

package UserRegistration;

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
import java.sql.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Buyer_Registration {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String mobileNumber;
    private static String panNumber;
    private static String emailID;
    private static String Address;
    private static String gstStatus;
    private Scanner scanner;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        scanner = new Scanner(System.in);
        driver.manage().window().maximize();
        System.out.println("Launching CD Buyer Registration Test Case...");
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
        System.out.println("Step 4: GST Registered (yes/no)");
        while (true) {
            System.out.print("Are you GST registered? (yes/no): ");
            gstStatus = scanner.nextLine().trim().toLowerCase();
            if (gstStatus.equals("yes") || gstStatus.equals("no")) break;
            System.out.println("Invalid input. Please type 'yes' or 'no'.");
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

            // GST selection - always select 'No' as default, then override if 'yes'
            try {
                WebElement gstNoRadio = driver.findElement(By.xpath("//label[normalize-space(text())='No']"));
                gstNoRadio.click();
                System.out.println("GST Registration 'No' selected (default).");
            } catch (NoSuchElementException e) {
                System.out.println("Couldn't find GST 'No' radio. Continuing...");
            }

            if (gstStatus.equals("yes")) {
                try {
                    WebElement gstYesRadio = driver.findElement(By.xpath("//label[contains(text(), 'Yes')]"));
                    gstYesRadio.click();
                    System.out.println("GST Registration 'Yes' selected as per user.");
                } catch (NoSuchElementException e) {
                    System.out.println("Couldn't find GST 'Yes' radio. Maybe already set.");
                }
            }
        } catch (Exception e) {
            Assert.fail("FAILED [Site Navigation]: " + e.getMessage());
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
        driver.findElement(By.xpath("//*[@placeholder=\"Enter Your mobile number\"]")).sendKeys(mobileNumber);

        // Enter Pin Code
        driver.findElement(By.xpath("//*[@name=\"undefined\"]")).sendKeys("401107");

        // Select first auto-suggested pin option
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement firstDropdownOption = shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[@role='option' and contains(@class, 'p-autocomplete-item')]")));
            System.out.println("PIN code suggestion loaded.");
            firstDropdownOption.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Pincode Entry]: " + e.getMessage());
        }

        driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Address\"]")).sendKeys("Address");
    }

    @Test(priority = 8)
    public void testHandleGSTAutoFillAndSubmit() {
        System.out.println("Step 8: GST section handling and Registration submit");

        try {
            // Set GST - 'No' by default, 'Yes' if user input
            boolean isGST = gstStatus != null && gstStatus.equalsIgnoreCase("yes");

            // Always try to select "No" first, then set "Yes" if needed
            try {
                WebElement gstNoRadio = driver.findElement(By.xpath("//label[normalize-space(text())='No']"));
                if (gstNoRadio != null && gstNoRadio.isDisplayed()) {
                    gstNoRadio.click();
                    System.out.println("GST: 'No' selected as default.");
                }
            } catch (Exception ex) {
                System.out.println("GST 'No' radio not present or could not be clicked.");
            }

            if (isGST) {
                try {
                    WebElement gstYesRadio = driver.findElement(By.xpath("//label[contains(text(), 'Yes')]"));
                    if (gstYesRadio != null && gstYesRadio.isDisplayed()) {
                        gstYesRadio.click();
                        System.out.println("GST: 'Yes' selected as per user.");
                    }
                } catch (Exception ex) {
                    System.out.println("GST 'Yes' radio not present or could not be clicked.");
                }
            }

            // If GST = 'Yes', try to find GST field blocks
            boolean gstDropdownExists = false, gstContactExists = false;
            try {
                WebElement gstDropdown = driver.findElement(By.xpath("//p-dropdown[@formcontrolname='org_gst']"));
                if (gstDropdown != null && gstDropdown.isDisplayed() && gstDropdown.isEnabled()) {
                    gstDropdownExists = true;
                }
            } catch (NoSuchElementException ex) {
                gstDropdownExists = false;
            }

            try {
                WebElement nameField = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"contact_name\"]"));
                WebElement phoneField = driver.findElement(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\" and @formcontrolname=\"contact_person_no\"]"));
                if (nameField != null && nameField.isDisplayed() && phoneField != null && phoneField.isDisplayed()) {
                    gstContactExists = true;
                }
            } catch (NoSuchElementException ex) {
                gstContactExists = false;
            }

            if (isGST && (gstDropdownExists || gstContactExists)) {
                if (gstDropdownExists) {
                    try {
                        selectGSTDropdownOption(driver);
                    } catch (InterruptedException ie) {
                        System.out.println("Interrupted during GST dropdown: " + ie.getMessage());
                    }
                }
                if (gstContactExists) {
                    try {
                        WebElement nameField = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"contact_name\"]"));
                        nameField.clear();
                        nameField.sendKeys("Person Name");
                    } catch (Exception ex) {
                        System.out.println("GST contact name field could not be filled: " + ex.getMessage());
                    }
                    try {
                        WebElement phoneField = driver.findElement(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\" and @formcontrolname=\"contact_person_no\"]"));
                        phoneField.clear();
                        phoneField.sendKeys("9023789409");
                    } catch (Exception ex) {
                        System.out.println("GST contact number field could not be filled: " + ex.getMessage());
                    }
                }
                System.out.println("GST details were present and filled.");
            } else {
                System.out.println("GST details skipped (either GST is 'No' or fields not found/needed).");
            }

            // Submit the form
            try {
                WebDriverWait waitSubmit = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement submitButton = waitSubmit.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@class='btn btn-primary w-10']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
                Thread.sleep(500);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                System.out.println("Form Submit clicked. Fetching OTP from database...");
            } catch (Exception subEx) {
                Assert.fail("FAILED [Submit Form]: " + subEx.getMessage());
            }
        } catch (Exception ex) {
            Assert.fail("FAILED [GST AutoHandle]: " + ex.getMessage());
        }
    }
    
    @Test(priority = 9)
    public void testEnterOTP() throws InterruptedException {
        System.out.println("Step 9: Fetch and Enter OTP from DB");
        String otp = fetchOtpFromDatabase(mobileNumber);
        Assert.assertNotNull(otp, "OTP not found in DB.");
        Assert.assertEquals(otp.length(), 6, "OTP is not 6-digits.");

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
            Assert.fail("FAILED [OTP Entry]: " + e.getMessage());
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
            System.out.println("Buyer Registration Test flow completed successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [Action Button/Continue]: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null)
            driver.quit();
        System.out.println("Test execution finished.");
    }

    // Helper: Only accept exactly 10 digit number from console
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

    // Method to select GST dropdown option (first value)
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