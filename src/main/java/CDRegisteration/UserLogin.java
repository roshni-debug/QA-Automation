package CDRegisteration;

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

import io.github.bonigarcia.wdm.WebDriverManager;

public class UserLogin {

    public static void main(String[] args) throws InterruptedException, SQLException {
        // Set ChromeDriver path
    	WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        Actions actions = new Actions(driver);
        driver.manage().window().maximize();

        // WebDriverWait for waiting for elements
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Get 10-digit mobile number from console input
        String mobileNumber = readTenDigitsFromConsole();

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

        // KYC Cancellation
        WebDriverWait popup = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement CancelPopup = popup.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div[2]/div/div[3]/button[1]")));
        CancelPopup.click();
        
        driver.findElement(By.xpath("//*[@class=\"d-flex flex-row align-items-center gap-4 nav-logout\"]")).click();
        driver.findElement(By.xpath("//*[@class=\"btn btn-success rounded-pill\" and contains(text(), \"Confirm\")]")).click();
    }

    // Method to read exactly 10 digits from console
    public static String readTenDigitsFromConsole() {
        StringBuilder sb = new StringBuilder(10);
        System.out.print("Please enter your 10-digit mobile number: ");
        try {
            while (sb.length() < 10) {
                int ch = System.in.read();
                if (ch == -1) break; 
                if (ch == '\r' || ch == '\n') {
                    continue; 
                }
                char c = (char) ch;
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                    System.out.print(c); 
                } else {
                    // Ignore non-digit input
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
}

//Dealer 9999999990
// Buyer 8000000002
// Buyer 8000000060
//Non Gst 8000060000
//Buyer 8060806080
