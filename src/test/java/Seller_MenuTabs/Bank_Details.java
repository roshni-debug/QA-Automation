package Seller_MenuTabs;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.ElementNotInteractableException;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Bank_Details {

    public static void main(String[] args) throws InterruptedException {
        // Set ChromeDriver path
    	WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        Scanner scanner = new Scanner(System.in);
        Actions actions = new Actions(driver);
        driver.manage().window().maximize();

        // WebDriverWait for waiting for elements
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Get 10-digit mobile number from console input
        String mobileNumber = readTenDigitsFromConsole();
        
       // Account number addition
        System.out.print("Enter 16 Digit Account Number: ");
        String Account_No = scanner.nextLine().trim();
        
        // Navigate to login page
        driver.get("https://digielv.mmcm.in/");

        // Click Login/Register Button
        driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
        driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
        driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();

        // Fetch OTP from DB once (no retries)
        String otp = fetchOtpFromDatabase(mobileNumber);

        // Check if OTP was retrieved
        if (otp != null && !otp.isEmpty() && otp.length() == 6) {
            try {
                // Wait for all 6 OTP input boxes to appear
                WebDriverWait otpWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                List<WebElement> otpInputs = otpWait.until(
                        ExpectedConditions.visibilityOfAllElementsLocatedBy(
                                By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                        )
                );

                if (otpInputs.size() < 6) {
                    System.out.println("Less than 6 OTP input fields found. Found: " + otpInputs.size());
                } else {
                    for (int i = 0; i < 6; i++) {
                        WebElement input = otpInputs.get(i);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
                        input.click();
                        input.clear();
                        input.sendKeys(Character.toString(otp.charAt(i)));
                        Thread.sleep(100); // small delay between each digit
                    }

                    System.out.println("OTP entered successfully: " + otp);
                }

            } catch (Exception e) {
                System.out.println("Error while entering OTP: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("OTP is null, empty, or not 6 digits. Value: " + otp);
        }

        // Handle optional KYC cancellation popup if it appears
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
            // No popup appeared; move to next step silently
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }

       // Open account type dropdown
          WebElement accountTypeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                  By.xpath("//p-dropdown[@formcontrolname='account_type']//div[@class='p-dropdown-trigger']")));
          ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountTypeDropdown);
          Thread.sleep(500);
          accountTypeDropdown.click();
          System.out.println("Clicked on Account Type dropdown");

          // Wait for and click "Savings" option
          WebElement savingsOption = wait.until(ExpectedConditions.elementToBeClickable(
                  By.xpath("//li[@role='option' and normalize-space()='Savings']")));
          savingsOption.click();
          System.out.println("Selected 'Savings' account type");


           //Account No
           WebDriverWait TextBox = new WebDriverWait(driver, Duration.ofSeconds(90));
           WebElement AccountNo = TextBox.until(
           ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Enter Your Account No\"]")));
           AccountNo.sendKeys(Account_No);
        
          //ReEnter Account No
          WebDriverWait TextBox1 = new WebDriverWait(driver, Duration.ofSeconds(10));
          WebElement ReAccountNo = TextBox1.until(
          ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Re-enter Account No\"]")));
          ReAccountNo.sendKeys(Account_No);
        
       //IFSC Code
        WebDriverWait TextBox2 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement IFSCcode = TextBox2.until(
        ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Enter Your ifsc\"]")));
        IFSCcode.sendKeys("HDFC0009226");
        
        //File Upload
        try {
            uploadFile(driver, "formFile0", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
        } catch (Exception e) {
            System.out.println("File Upload Failed: " + e.getMessage());
        }
        
        finally {
        
        }

                  
    //Submit Button
            WebDriverWait waitSubmit = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement submitButton = waitSubmit.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div/div[2]/div/div/form/div[3]/div/div[9]/button")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);  
           
}

    // Method to read exactly 10 digits from console
    public static String readTenDigitsFromConsole() {
        StringBuilder sb = new StringBuilder(10);
        System.out.print("Please enter your 10-digit mobile number: ");
        try {
            while (sb.length() < 10) {
                int ch = System.in.read(); // reads one byte/char
                if (ch == -1) break; // EOF
                // Handle CR and LF (enter) by ignoring them
                if (ch == '\r' || ch == '\n') {
                    continue; // ignore enter pressed before 10 digits
                }
                char c = (char) ch;
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                    System.out.print(c); // echo the digit
                } else {
                    // ignore any non-digit character (including spaces), do not echo
                    // If you want to give beep feedback: System.out.print("\007");
                }
            }

            // After collecting 10 digits, consume remaining characters on the current input line
            int leftover;
            do {
                leftover = System.in.read();
            } while (leftover != -1 && leftover != '\n');

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    // Database helper method to fetch OTP by mobile number
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";

        String query1 = "SELECT otp FROM common.user_mstr WHERE mobile_no = " + mobileNumber;

        try {
            // Load PostgreSQL driver (not strictly required for JDBC 4.0+)
            Class.forName("org.postgresql.Driver");

            // Establish connection
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database.\n");

            // Query 1: Select all user details
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(query1);

            if (rs1.next()) {
                otp = rs1.getString("otp");
            } else {
                System.out.println("No user found with mobile number: " + mobileNumber);
            }

            rs1.close();
            stmt1.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return otp;
    }


    //File Uploading
    public static void uploadFile(WebDriver driver, String inputId, String filePath) throws Exception {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div/div[2]/div/div/form/div[3]/div/div[8]/div/input")));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].classList.remove('d-none'); arguments[0].style.display='block'; arguments[0].style.visibility='visible';",
                fileInput
        );

        fileInput.sendKeys(filePath);
        System.out.println("File uploaded: " + filePath);          
    }   
}

