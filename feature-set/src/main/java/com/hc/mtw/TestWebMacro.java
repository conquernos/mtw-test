package com.hc.mtw;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


public class TestWebMacro {

    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-extensions", "--dns-prefetch-disable", "--always-authorize-plugins", "--disable-gpu");
        ChromeDriver driver = new ChromeDriver(options);
        driver.get("https://deview.kr/oauth/chooseLogin?redirectUrl=http://deview.kr/2018/register");
        driver.findElementByXPath("//*[@id=\"naver\"]/span").click();

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        try {
            jse.executeScript("document.getElementById('id').value='univerquest';");
        } catch (Exception e) {

        }
        try {
            jse.executeScript("document.getElementById('pw').value='amn116jhin@';");
        } catch (Exception e) {

        }

//        driver.findElementByXPath("//*[@id=\"id\"]").click();
//        driver.findElementByXPath("//*[@id=\"id\"]").clear();
//        driver.findElementByXPath("//*[@id=\"id\"]").sendKeys("univerquest");
//        driver.findElementByXPath("//*[@id=\"pw\"]").click();
//        driver.findElementByXPath("//*[@id=\"pw\"]").clear();
//        driver.findElementByXPath("//*[@id=\"pw\"]").sendKeys("amn116jhin@");

        driver.findElementByXPath("//*[@id=\"frmNIDLogin\"]/fieldset/input").click();



        driver.findElementByXPath("//*[@id=\"frmNIDLogin\"]/fieldset/span[2]/a").click();

        driver.findElementByXPath("//*[@id=\"frmNIDLogin\"]/fieldset/input").click();

//        driver.close();
    }

}
