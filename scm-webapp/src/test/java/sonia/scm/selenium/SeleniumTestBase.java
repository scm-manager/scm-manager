/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.selenium;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Files;

import org.junit.After;
import org.junit.Before;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
public class SeleniumTestBase
{

  /**
   * the logger for SeleniumTestBase
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SeleniumTestBase.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception
  {
    driver.quit();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception
  {
    driver = new FirefoxDriver();
    baseUrl = "http://localhost:8082/scm/";
    open("index.html");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   */
  protected void login(String username, String password)
  {
    type("input[name=username]", username);
    type("input[name=password]", password);
    waitAndClick("#loginButton button");

    String ue = waitForPresence("#scm-userinfo-tip").getText();

    assertEquals(username, ue);
  }

  /**
   * Method description
   *
   */
  protected void logout()
  {
    waitAndClick("#navLogout a");
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  protected void open(String url)
  {
    driver.get(baseUrl + url);
    pause(500, TimeUnit.MILLISECONDS);
  }

  /**
   * Method description
   *
   *
   * @param value
   * @param unit
   */
  protected void pause(int value, TimeUnit unit)
  {
    driver.manage().timeouts().implicitlyWait(value, unit);
  }

  /**
   * Method description
   *
   *
   * @param target
   */
  protected void screenshot(String target)
  {
    screenshot(new File(target));
  }

  /**
   * Method description
   *
   *
   * @param target
   */
  protected void screenshot(File target)
  {
    try
    {
      File scrFile =
        ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

      Files.copy(scrFile, target);
    }
    catch (IOException ex)
    {
      logger.error("could not create screenshot", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param cssSelector
   *
   * @return
   */
  protected WebElement select(String cssSelector)
  {
    WebElement element = driver.findElement(By.cssSelector(cssSelector));

    assertNotNull(element);

    return element;
  }

  /**
   * Method description
   *
   *
   * @param cssLocator
   * @param value
   */
  protected void type(String cssLocator, String value)
  {
    select(cssLocator).clear();
    select(cssLocator).sendKeys(value);
  }

  /**
   * Method description
   *
   *
   * @param query
   */
  protected void waitAndClick(String query)
  {
    waitToBeClickable(query).click();
  }

  /**
   * Method description
   *
   *
   * @param query
   *
   * @return
   */
  protected WebElement waitForPresence(String query)
  {
    WebDriverWait wait = new WebDriverWait(driver, 5);

    return wait.until(
        ExpectedConditions.presenceOfElementLocated(By.cssSelector(query)));
  }

  /**
   * Method description
   *
   *
   * @param query
   *
   * @return
   */
  protected WebElement waitToBeClickable(String query)
  {
    WebDriverWait wait = new WebDriverWait(driver, 5);

    return wait.until(
        ExpectedConditions.elementToBeClickable(By.cssSelector(query)));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected WebDriver driver;

  /** Field description */
  private String baseUrl;
}
