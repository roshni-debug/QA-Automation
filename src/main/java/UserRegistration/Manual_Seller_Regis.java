package UserRegistration;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Manual_Seller_Regis {

    public static void main(String[] args) throws InterruptedException {
        // Set ChromeDriver path
    	WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        Scanner scanner = new Scanner(System.in);
        Actions actions = new Actions(driver);
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        // Prompt user for input
        System.out.print("Enter 10-digit Mobile Number: ");
        String mobileNumber = scanner.nextLine().trim();

        System.out.print("Enter 10-character PAN Number: ");
        String panNumber = scanner.nextLine().trim();
        
        System.out.print("Enter Owner name provided in VAHAN: ");
        String OwnerName = scanner.nextLine().trim();
        
        System.out.print("Enter CD: ");
        String CDNo = scanner.nextLine().trim();
          
        // Ask user if GST registered
        System.out.print("Are you GST registered? (yes/no): ");
        String gstStatus = scanner.nextLine().trim().toLowerCase();


        WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        // Navigate to login page
        driver.get("https://digielv.mmcm.in/");
        
        //Click on Manual Registration
        WebDriverWait wait11 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement manualClick = wait11.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='manual-register']"))
        );

        // Force click with JavaScript
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", manualClick);


        //Enter CD
        WebElement CD = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your CD No\"]"));
        CD.sendKeys(CDNo);

        //Verify Button
        driver.findElement(By.xpath("//button[normalize-space(text())='Verify' and contains(@class,'btn-primary')]")).click();

        // Handle GST selection
        if (gstStatus.equals("yes")) {
            driver.findElement(By.xpath("//label[contains(text(), 'Yes')]")).click();
            System.out.println("GST Registration selected.");
        } else {
            driver.findElement(By.xpath("//label[normalize-space(text())='No']")).click();
            System.out.println("Non-GST Registration selected.");
        }
        
                                              /* Organization Details/Individual Details */ 
     // Enter PAN number
        WebElement panNo = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your PAN No\"]"));
        panNo.sendKeys(panNumber);

        // Handle popup
        WebDriverWait popup = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement popclick = popup.until(
                ExpectedConditions.elementToBeClickable(By.xpath(" //button[contains(text(), 'Got it')]")));
        popclick.click();

        // Enter Owner name provided in VAHAN
        driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"full_name\"]")).sendKeys(OwnerName);

        // Enter Name to reflect on CD
        WebElement CDName = driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"org_name\"]"));
        CDName.sendKeys("CDName");

        // Enter Phone Number
        WebElement phoneInput = driver.findElement(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\"]"));
        phoneInput.sendKeys(mobileNumber);

        // Enter Address
        driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Address\" and @formcontrolname=\"org_address\"]"))
                .sendKeys("Address");

        // Enter Pin Code
        driver.findElement(By.xpath("//*[@name=\"undefined\"]")).sendKeys("401107");

        // Select first auto-suggested pin option
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement firstDropdownOption = shortWait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//li[@role='option' and contains(@class, 'p-autocomplete-item')]")
                    )
            );
            System.out.println("PIN code suggestion loaded.");
            firstDropdownOption.click();
        } catch (Exception e) {
            System.out.println("PIN code auto-suggestion failed: " + e.getMessage());
        }

        // -------------- GST conditional ---------------
        if (gstStatus.equals("yes")) {
        	
        	
            // ----- Business Details -----
            // Select first auto-suggested Constitutional option
            driver.findElement(By.xpath("//*[@aria-label=\"Select a Constitutional type\"]")).click();

            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement firstDropdownOption = shortWait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//*[@aria-label=\"Company\"]")
                        )
                );
                System.out.println("Constitutional Type is selected!");
                firstDropdownOption.click();
            } catch (Exception e) {
                System.out.println("Constitutional Type selection Failed: " + e.getMessage());
            }   

            // Fill GST details and person info
            selectGSTDropdownOption(driver);

            driver.findElement(By.xpath("//*[@placeholder=\"Enter Your Name\" and @formcontrolname=\"contact_name\"]"))
                    .sendKeys("Person Name");

            driver.findElement(By.xpath("//*[@placeholder=\"Please Enter 10-digit mobile number\" and @formcontrolname=\"contact_person_no\"]"))
                    .sendKeys("9023789409");
        }

        // -------------- Documents Details ---------------
        try {
            uploadFile(driver, "formFile0", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
            uploadFile(driver, "formFile1", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
            uploadFile(driver, "formFile2", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
            uploadFile(driver, "formFile3", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
            uploadFile(driver, "formFile4", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
        } catch (Exception e) {
            System.out.println("File Upload Failed: " + e.getMessage());
        }
        
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
          
        //Submit Button
                
     WebDriverWait wait4 = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            WebElement submitButton = wait4.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[normalize-space(text())='Submit']")));        
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);

            System.out.println("Submit button clicked");

        } catch (Exception e) {
            System.out.println("Failed to click Submit button");
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