package CDRegisteration;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeoutException;
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

import org.openqa.selenium.ElementNotInteractableException;

//GST NonGST Buyer Transaction

public class DealerCompleteBidPlace {

    public static void main(String[] args) throws InterruptedException {
        // Set ChromeDriver path
    	WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        Actions actions = new Actions(driver);
        driver.manage().window().maximize();

        // WebDriverWait for waiting for elements
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    
        Scanner scanner = new Scanner(System.in);
        Actions actions1 = new Actions(driver);
        driver.manage().window().maximize();
        
        // Prompt user for input
      // System.out.print("Enter 10-digit Mobile Number: ");
    //   String mobileNumber = scanner.nextLine().trim();

     //  System.out.print("Enter 10-character PAN Number: ");
     //  String panNumber = scanner.nextLine().trim();
           
    // Ask user if GST registered
       System.out.print("Are you GST registered? (yes/no): ");
       String gstStatus = scanner.nextLine().trim().toLowerCase();
       
    // Get 10-digit mobile number from console input
       String mobileNumber1 = readTenDigitsFromConsole();


        try {
            WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Navigate to login page
            driver.get("https://digielv.mmcm.in/");

            // Click Login/Register Button
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
            
// ***************************************************** CD Buyer Registration*********************************************************************

/*           // Click Register Here Button
            driver.findElement(By.xpath("/html/body/app-root/app-user-login/section/div/div/div/div/div[2]/div/div/div/div[2]/span/a")).click();

            // Click Buyer Tab
            driver.findElement(By.xpath("//*[@id=\"buyerTab\"]")).click();

            // Handle GST selection
            if (gstStatus.equals("yes")) {
                // Select GST - Yes
                driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/div[1]/div/div/div/div/div[1]/p-radiobutton/div/div[2]")).click();
                System.out.println("GST Registration selected.");
            } else {
                // Select GST - No (Non-GST)
                driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/div[1]/div/div/div/div/div[2]/p-radiobutton/div/div[2]")).click();
                System.out.println("Non-GST Registration selected.");
            }           
            
            // Enter PAN number (from user input)
            WebElement panNo = driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/form/div[1]/div/div/div[1]/div/p-iconfield/span/input"));
            panNo.sendKeys(panNumber); 
            
            //Popup Click
            WebDriverWait popup = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement popclick = popup.until(
                ExpectedConditions.elementToBeClickable(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/app-registration-modal[1]/section/div/div/div/div[3]/button")));
            popclick.click();

            // Enter Email ID
            driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/form/div[1]/div/div/div[3]/div/p-iconfield/span/input")).sendKeys("roshni.chaurasiya@mmcm.in");

            // Enter Phone Number (from user input)
            WebElement phoneInput = driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/form/div[1]/div/div/div[4]/div/p-iconfield/span/input"));
            phoneInput.sendKeys(mobileNumber);

            // Enter Pin Code
            driver.findElement(By.xpath("//*[@name=\"undefined\"]")).sendKeys("401107");

            // Select 1st auto-suggested option after entering pin code
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

            // Enter Address
            driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/form/div[1]/div/div/div[9]/div/textarea")).sendKeys("Address");

            // If GST registered → fill GST details and person info
            if (gstStatus.equals("yes")) {
                // Select GST dropdown option
                selectGSTDropdownOption(driver);

                // Enter Person Name
                driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/form/div[2]/div/div/div[4]/div/p-iconfield/span/input"))
                        .sendKeys("Person Name");

                // Enter Person Phone Number
                driver.findElement(By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/form/div[2]/div/div/div[5]/div/p-iconfield/span/input"))
                        .sendKeys("9023789409");
            }

            // Click Submit Button
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

                if (otp != null && !otp.isEmpty() && otp.length() == 6) {
                    try {
                        WebDriverWait otpWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                        List<WebElement> otpInputs = otpWait.until(
                            ExpectedConditions.visibilityOfAllElementsLocatedBy(
                                By.xpath("//input[contains(@class, 'p-inputotp-input')]")
                            )
                        );

                        if (otpInputs.size() < 6) {
                            System.out.println("Less than 6 OTP fields found. Found: " + otpInputs.size());
                        } else {
                            for (int i = 0; i < 6; i++) {
                                WebElement input = otpInputs.get(i);
                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
                                input.click();
                                input.clear();
                                input.sendKeys(Character.toString(otp.charAt(i)));
                                Thread.sleep(100); // delay between digits
                            }

                            System.out.println("OTP entered: " + otp);
                        }
                    } catch (Exception e) {
                        System.out.println("Error entering OTP: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("OTP not found or invalid: " + otp);
                }

                // Click Registered Button        
                WebDriverWait actionBtn = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement btn = actionBtn.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id=\"actionBtn\"]")));
                btn.click();          
           
            // Click Continue Button
            WebDriverWait Continue = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement continueBtn = Continue.until(ExpectedConditions.elementToBeClickable(
            By.xpath("/html/body/app-root/app-registration-tab-mstr/div/app-registration/section/div/div/app-registration-modal[2]/section/div/div/div/div[3]/button")));
            continueBtn.click();   */

        // **************************************** User Login **************************************************
        
        // Get 10-digit mobile number from console input
        // Click Login/Register Button
        driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber1);
        driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();

        // Fetch OTP from DB once (no retries)
        String otp1 = fetchOtpFromDatabase(mobileNumber1);

        // Check if OTP was retrieved
        if (otp1 != null && !otp1.isEmpty() && otp1.length() == 6) {
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
                        input.sendKeys(Character.toString(otp1.charAt(i)));
                        Thread.sleep(100); // small delay between each digit
                    }

                    System.out.println("OTP entered successfully: " + otp1);
                }

            } catch (Exception e) {
                System.out.println("Error while entering OTP: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("OTP is null, empty, or not 6 digits. Value: " + otp1);
        }

        // Handle optional KYC cancellation popup if it appears
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div[2]/div/div[3]/button[1]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            // No popup appeared; move to next step silently
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
        
        
        //*********************************************My Account*************************************        
            try {
            // 🔹 Ask for account details from user
            System.out.print("Enter Account Number: ");
            String accountNoInput = scanner.nextLine().trim();

            System.out.print("Enter IFSC Code: ");
            String ifscCodeInput = scanner.nextLine().trim();

            // 🔹 Open account type dropdown
            WebElement accountTypeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-dropdown[@formcontrolname='account_type']//div[@class='p-dropdown-trigger']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountTypeDropdown);
            Thread.sleep(500);
            accountTypeDropdown.click();
            System.out.println("Clicked on Account Type dropdown");

            // 🔹 Wait for and click "Savings" option
            WebElement savingsOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[@role='option' and normalize-space()='Savings']")));
            savingsOption.click();
            System.out.println("Selected 'Savings' account type");

            // 🔹 Account No field
            WebElement accountNo = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@placeholder='Enter Your Account No']")));
            accountNo.clear();
            accountNo.sendKeys(accountNoInput);
            System.out.println("Entered Account No: " + accountNoInput);

            // 🔹 Re-enter Account No field
            WebElement reAccountNo = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@placeholder='Re-enter Account No']")));
            reAccountNo.clear();
            reAccountNo.sendKeys(accountNoInput);
            System.out.println("Re-entered Account No: " + accountNoInput);

            // 🔹 IFSC Code field
            WebElement ifscCode = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@placeholder='Enter Your ifsc']")));
            ifscCode.clear();
            ifscCode.sendKeys(ifscCodeInput);
            System.out.println("Entered IFSC Code: " + ifscCodeInput);

            // 🔹 File Upload
            try {
                uploadFile1(driver, "formFile0", "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png");
                System.out.println("File uploaded successfully.");
            } catch (Exception e) {
                System.out.println("File Upload Failed: " + e.getMessage());
            }

            // 🔹 Click Submit (Add) Button
            WebDriverWait waitSubmit1 = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement submitButton1 = waitSubmit1.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space(text())='Add']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton1);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton1);
            System.out.println("Clicked on 'Add' button successfully.");

        } catch (Exception e) {
            System.out.println("Error in Account Details Section: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // You can add cleanup code here if needed
        } 

 //**************************************************Funds Management************************************************************
        
        // Perform the full funds management process
           clickSidebarElement(driver, wait, "Funds Management");
           addFunds(driver, wait);
           selectPaymentMethod(driver, wait);
           clickContinue(driver, wait);
           enterAmountAndRemarks(driver, wait);
           selectNetBankingMethod(driver, wait);
           ContinuePayment(driver, wait);                  
           
           
           // Step 1: Wait for user to manually complete Razorpay payment
           System.out.println("Waiting for Razorpay payment (complete manually)...");
           Thread.sleep(12000); // Wait 2 min for manual payment (adjust as needed)

           // Step 2: Switch back to main window (in case Razorpay opened new window)
           String mainWindow = driver.getWindowHandle();
           Set<String> allWindows = driver.getWindowHandles();
           for (String win : allWindows) {
               if (!win.equals(mainWindow)) {
                   driver.switchTo().window(win);
                   driver.close(); // close Razorpay popup
               }
           }
           driver.switchTo().window(mainWindow);
           System.out.println("Returned to MMCM main window.");

           // Step 3: Wait for user to manually click 'Continue' after payment
           System.out.println("Waiting for user to click 'Continue' button...");
           Thread.sleep(5000); // Adjust if needed

           // Step 4: Click View Market Offers
           clickAngularMenu(driver, "//li[contains(@class,'menu-item')]//a[contains(normalize-space(.), 'View Market Offers')]");

           // Step 5: Click View All Offer button
           clickAngularMenu(driver, "//button[contains(text(),'View All Offer')]");

           System.out.println("Flow completed.");
        }
       finally {
        
     // Wait for and click on Buy Offer button
        WebElement BuyOffer = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-categorised-offers/section/div[1]/div/div[2]/div/div/div[3]/div/button"))
        );
        BuyOffer.click();

        // Enter bid price in the bid input field
        WebElement BidEnter = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"integeronly\"]"))
        );
        BidEnter.sendKeys("10500");

        // Click the Place Bid button
        WebElement BidPlace = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-categorised-offers/section/div[3]/div/div/div[3]/button[2]"))
        );
        BidPlace.click();

      //Click on continue button
        try {
            WebDriverWait wait11 = new WebDriverWait(driver, Duration.ofSeconds(20));

            // Wait for the modal itself to be visible
            WebElement modal = wait11.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.modal-content")
            ));

            // Wait for the Continue button inside the modal
            WebElement continueButton = wait11.until(ExpectedConditions.elementToBeClickable(
                modal.findElement(By.cssSelector("div.modal-footer > button.btn-primary"))
            ));

            // Optional: scroll into view (sometimes needed for Angular)
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", continueButton);
            Thread.sleep(500); // small pause to ensure animations finished

            // Try JS click first
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
            System.out.println("✅ Clicked 'Continue' on modal.");

        } catch (Exception e) {
            System.out.println("❌ Could not click 'Continue': " + e.getMessage());

            // Fallback: Actions click
            try {
                WebElement continueButton = driver.findElement(By.cssSelector("div.modal-footer > button.btn-primary"));
                new Actions(driver).moveToElement(continueButton).click().build().perform();
                System.out.println("✅ Clicked 'Continue' using Actions.");
            } catch (Exception ex) {
                System.out.println("❌ Fallback click failed: " + ex.getMessage());
            }
            
            finally {}
        }
        
        finally {}      
       }    
          
        }
        
       // finally {}
      
    //   }

   //   ******************************************* Helper Method *************************************************************      
    
       // Utility method: click Angular/JS heavy buttons safely
       public static void clickAngularMenu(WebDriver driver, String xpath) throws InterruptedException {
           WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
           try {
               WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));

               // Scroll into view
               ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
               Thread.sleep(500);

               // Extra check: element clickable
               try {
                   wait.until(ExpectedConditions.elementToBeClickable(element));
               } catch (Exception ignored) {}

               // JS click (most reliable for Angular)
               ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
               System.out.println("Clicked element: " + xpath);

               // Small wait after click
               Thread.sleep(1500);
           } catch (Exception e) {
               System.out.println("Could not click element: " + xpath + " | Error: " + e.getMessage());
           }
       }
        


	// Function to click any element in the sidebar (e.g., "Funds Management")
    public static void clickSidebarElement(WebDriver driver, WebDriverWait wait, String elementText) {
        try {
            WebElement sidebarLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), '" + elementText + "')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sidebarLink);
            System.out.println(elementText + " clicked successfully!");
        } catch (Exception e) {
            System.out.println("Failed to click " + elementText + ": " + e.getMessage());
        }
    }

    // Function to click the 'Add Funds' button
    public static void addFunds(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement addFund = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[1]/button")));
            addFund.click();
        } catch (Exception e) {
            System.out.println("Failed to click Add Funds: " + e.getMessage());
        }
    }

    // Function to select payment method
    public static void selectPaymentMethod(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement payMethod = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[2]/div[2]/div/div/div/div[2]/div/div[1]/div/button")));
            payMethod.click();
        } catch (Exception e) {
            System.out.println("Failed to select Payment Method: " + e.getMessage());
        }
    }

    // Function to click continue button
    public static void clickContinue(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[2]/div[2]/div/div/div/div[3]/button[2]")));
            continueBtn.click();
        } catch (Exception e) {
            System.out.println("Failed to click Continue: " + e.getMessage());
        }
    }

    // Function to enter the amount and remarks
    public static void enterAmountAndRemarks(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement amount = wait.until(ExpectedConditions.elementToBeClickable(By.id("integeronly")));
            amount.click();
            amount.clear();
            amount.sendKeys("15000");

            WebElement remarks = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//textarea[@placeholder='Enter Your Remarks']")));
            remarks.click();
            remarks.clear();
            remarks.sendKeys("Bid Price");

            WebElement payNowButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='pay-btn']")));
            if (payNowButton.isEnabled()) {
                payNowButton.click();
                System.out.println("Clicked Pay Now button!");
            } else {
                System.out.println("Pay Now button is disabled.");
            }
        } catch (Exception e) {
            System.out.println("Error while entering amount and remarks: " + e.getMessage());
        }
    }

    // Function to select NetBanking Method
    public static void selectNetBankingMethod(WebDriver driver, WebDriverWait wait) {
        try {
            driver.switchTo().frame(0);

            WebElement payMethod = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div[1]/div[1]/div[2]/div[2]/div/div/div/div/div[1]/div[1]/label[4]/div/div")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", payMethod);
            payMethod.click();

            WebElement KotakBank = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"main-stack-container\"]/div/div/div/div/div[2]/div/div/form[1]/div/label[2]/div")
            ));
            KotakBank.click();

            System.out.println("NetBanking Method selected successfully.");
        } catch (Exception e) {
            System.out.println("Failed to select NetBanking Method: " + e.getMessage());
        }
    }

    // Function to proceed with net banking
    public static void ContinuePayment(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement payNow = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='fee-bearer-cta' and contains(normalize-space(.), 'Continue & Pay')]")
            ));
            payNow.click();
        } catch (Exception e) {
            System.out.println("Failed to proceed with payment: " + e.getMessage());
        }
    }
    
    /*private static void continueClick(WebDriver driver, WebDriverWait wait){
        try {
        	
            driver.switchTo().frame(0);
 	
            // Extend explicit wait time to handle slow appearance (e.g., 30s)
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Wait until the element is both visible and clickable
            WebElement payNow = longWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"content\"]/main/app-funds-management/section/div/div[2]/div[2]/div/div/div/div[3]/button[2]")
            ));

            // Small pause to ensure stability (optional)
            Thread.sleep(1000);

            // Click using JavaScript to avoid overlay or intercept issues
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", payNow);

            System.out.println("Successfully clicked on Continue button");

        } catch (Exception e) {
            System.out.println("Couldn't click on Continue button: " + e.getMessage());
        }
    } */
   
    // Method to select GST dropdown option
    private static void selectGSTDropdownOption(WebDriver driver) throws InterruptedException {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // Wait and click GST dropdown
            WebElement gstDropdownTrigger = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-dropdown[@formcontrolname='org_gst']//div[contains(@class,'p-dropdown-trigger')]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", gstDropdownTrigger);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", gstDropdownTrigger);
            System.out.println("GST Dropdown opened.");

            // Wait for options
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
    
    //File Uploading
    public static void uploadFile1(WebDriver driver, String inputId, String filePath) throws Exception {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file' and @accept='.jpeg, .jpg, .png']")));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].classList.remove('d-none'); arguments[0].style.display='block'; arguments[0].style.visibility='visible';",
                fileInput
        );

        fileInput.sendKeys(filePath);
        System.out.println("File uploaded: " + filePath);     
      
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

}



