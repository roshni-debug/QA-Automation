package CDRegisteration;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.sql.*;

public class KYC {

    public static void main(String[] args) throws InterruptedException {
        // Set ChromeDriver path
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\roshn\\Documents\\Selenium\\chromedriver-win64\\chromedriver.exe");

        // -----------------------------
        // ChromeOptions: auto-allow mic & camera
        // -----------------------------
        ChromeOptions options = new ChromeOptions();

        // Useful arguments to auto-allow and provide fake media streams (for automation)
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        
        Map<String, Object> prefs = new HashMap<>();

        // Default allow for mic and camera (1 = allow, 2 = block)
        prefs.put("profile.default_content_setting_values.media_stream_mic", 1);
        prefs.put("profile.default_content_setting_values.media_stream_camera", 1);

        // Optionally disable geolocation/popups if interfering
        prefs.put("profile.default_content_setting_values.geolocation", 2);
        prefs.put("profile.default_content_setting_values.notifications", 2);

        // If you want *site-specific* exceptions, set them (recommended)
        // Structure: profile.content_settings.exceptions.media_stream_camera -> { "https://your-website.com:443": {"setting": 1} }
        Map<String, Object> cameraExceptions = new HashMap<>();
        Map<String, Object> micExceptions = new HashMap<>();

        Map<String, Object> allowSetting = new HashMap<>();
        allowSetting.put("setting", 1);

        String siteOriginWithPort = null;
		// Note: key might require port. Try both forms if needed.
        cameraExceptions.put(siteOriginWithPort + ",", allowSetting); // Chrome stores keys with trailing comma
        micExceptions.put(siteOriginWithPort + ",", allowSetting);

        prefs.put("profile.content_settings.exceptions.media_stream_camera", cameraExceptions);
        prefs.put("profile.content_settings.exceptions.media_stream_mic", micExceptions);

        options.setExperimentalOption("prefs", prefs);


        // -----------------------------
        // Create driver with options (must pass options here)
        // -----------------------------
        WebDriver driver = new ChromeDriver(options);

        Scanner scanner = new Scanner(System.in);
        Actions actions = new Actions(driver);
        driver.manage().window().maximize();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Navigate to login page
            driver.get("https://digielv.mmcm.in/");

            // Click Login/Register Button
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Enter Your Mobile Number']")))
                .sendKeys("8000000000");
            driver.findElement(By.xpath("/html/body/app-root/app-user-login/section/div/div/div/div/div[2]/div/div/div/div[2]/button")).click();

            // Fetch OTP from DB once (no retries)
            String mobileNumber = "8000000000";
            String otp = fetchOtpFromDatabase(mobileNumber);

            // Enter OTP
            if (otp != null && !otp.isEmpty() && otp.length() == 6) {
                try {
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

            // KYC Verification
            WebDriverWait popup = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement popclick = popup.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div[2]/div/div[3]/button[2]")));
            popclick.click();

            try {
                String filePath = "C:\\Users\\roshn\\Downloads\\PAN Fake.png";
                uploadFile(driver, "//input[@type='file' and @accept='image/*,.pdf']", filePath);
                System.out.println("File uploaded successfully!");
            } catch (Exception e) {
                System.out.println("File upload failed: " + e.getMessage());
                e.printStackTrace();
            }

            // Click Continue
            WebDriverWait waitbt = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = waitbt.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(@class, 'continue-btn')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", button);

            // Click Test Camera & Microphone
            WebDriverWait waitbtn = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement TestCM = waitbtn.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class, 'btn') and normalize-space()='Test Camera & Microphone']")));
            JavascriptExecutor js1 = (JavascriptExecutor) driver;
            js1.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", TestCM);
            System.out.println("Test Camera & Microphone clicked. Camera & Microphone should be auto-allowed by ChromeOptions.");
            
            WebDriverWait waitRec = new WebDriverWait(driver, Duration.ofSeconds(30));
            // Wait for the Start Recording button to be clickable
            WebElement RecordingBtn = waitRec.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(@class, 'btn') and normalize-space()='Start Recording']"))); // <-- Fixed XPath
            // Use JavascriptExecutor to scroll into view
            JavascriptExecutor js11 = (JavascriptExecutor) driver;
            // Scroll the button into view first
            js11.executeScript("arguments[0].scrollIntoView(true);", RecordingBtn);
            // Force click using JavascriptExecutor (if normal click doesn't work)
            js11.executeScript("arguments[0].click();", RecordingBtn);
            // Optionally, add a small delay to ensure recording starts
            Thread.sleep(2000);  // Add a small delay after clicking, can be adjusted
            System.out.println("Recording started.");
        
            
            
            WebDriverWait waitFin = new WebDriverWait(driver, Duration.ofSeconds(30));
            // Wait for the Start Recording button to be clickable
            WebElement FinishBtn = waitFin.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(@class, 'btn') and normalize-space()='Finish KYC']"))); // <-- Fixed XPath
            // Use JavascriptExecutor to scroll into view
            JavascriptExecutor js111 = (JavascriptExecutor) driver;
            // Scroll the button into view first
            js111.executeScript("arguments[0].scrollIntoView(true);", FinishBtn);
            // Force click using JavascriptExecutor (if normal click doesn't work)
            js111.executeScript("arguments[0].click();", FinishBtn);
            // Optionally, add a small delay to ensure recording starts
            Thread.sleep(2000);  // Add a small delay after clicking, can be adjusted
            System.out.println("Recording started.");
        }
        
        finally {
        	
        	
        }
        
    }

    public static void uploadFile(WebDriver driver, String xpath, String filePath) throws Exception {
        // Validate file exists
        File f = new File(filePath);
        if (!f.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        // Wait for element
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

        // If hidden, make it visible
        try {
            if (!fileInput.isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('hidden'); arguments[0].style.display='block';", fileInput);
            }
        } catch (Exception e) {
            System.out.println("Could not modify input visibility, trying direct sendKeys...");
        }

        // Upload file
        fileInput.sendKeys(filePath);
    }

    // Database helper method to fetch OTP by mobile number
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";

        // Mobile number to search
        String mobileNumber1 = "8000000000";

        // SQL query (you may want to parameterize instead of string concat)
        String query1 = "SELECT otp FROM common.user_mstr WHERE mobile_no = " + mobileNumber1;

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database.\n");

            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(query1);

            System.out.println("User Details:");
            ResultSetMetaData rsmd = rs1.getMetaData();
            int columnCount = rsmd.getColumnCount();

            if (rs1.next()) {
                otp = rs1.getString("otp");
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(rsmd.getColumnName(i) + ": " + rs1.getString(i));
                }
            } else {
                System.out.println("No user found with mobile number: " + mobileNumber1);
            }

            rs1.close();
            stmt1.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return otp;
    }
}
