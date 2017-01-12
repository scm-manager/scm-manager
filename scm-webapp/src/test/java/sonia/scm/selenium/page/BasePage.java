/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.selenium.page;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Abstract selenium base page.
 *
 * @author Sebastian Sdorra
 * 
 * @param <P> concrete page implementation
 */
public abstract class BasePage<P extends BasePage> {
  
  /**
   * Selenium test driver.
   */
  protected final WebDriver driver;

  /**
   * Constructs a new base page.
   * 
   * @param driver selenium test driver
   */
  protected BasePage(WebDriver driver) {
    this.driver = driver;
  }
  
  /**
   * Performs a {@link Thread#sleep(long)} for the given timeout.
   * 
   * @param time timeout
   * @param unit time unit of timeout
   */
  protected void sleep(long time, TimeUnit unit) {
    try {
      unit.sleep(time);
    } catch (InterruptedException ex) {
      throw Throwables.propagate(ex);
    }
  }
  
  /**
   * Wait for the element until it is clickable.
   * 
   * @param by element selector
   * 
   * @return web element
   */
  protected WebElement waitToBeClickable(By by){
    return waitToBeClickable(driver.findElement(by));
  }
  
  /**
   * Waits for the element until it is clickable.
   * 
   * @param element web element
   * 
   * @return web element
   */
  protected WebElement waitToBeClickable(WebElement element) {
    WebDriverWait wait = new WebDriverWait(driver, 5);

    return wait.until(ExpectedConditions.elementToBeClickable(element));
  }
  
  /**
   * Waits until the element is present.
   * 
   * @param by element locator
   * 
   * @return web element
   */
  protected WebElement waitFor(By by){
    WebDriverWait wait = new WebDriverWait(driver, 1);
    return wait.until(ExpectedConditions.presenceOfElementLocated(by));
  }
  
  /**
   * Waits until the elements are present.
   * 
   * @param by element selector
   * 
   * @return list of web elements
   */
  protected List<WebElement> waitForAll(By by){
    WebDriverWait wait = new WebDriverWait(driver, 1);
    return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
  }
  
  /**
   * Creates a screenshot of the current browser content and stores it at the given path.
   * 
   * @param target target file path
   * 
   * @return {@code this}
   */
  public P screenshot(String target) {
    return screenshot(new File(target));
  }

  /**
   * Creates a screenshot of the current browser content and stores it at the file.
   * 
   * @param target target file
   * 
   * @return {@code this}
   */
  public P screenshot(File target) {
    try {
      File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

      Files.copy(scrFile, target);
    } catch (IOException ex) {
      throw Throwables.propagate(ex);
    }
    return self();
  }
  
  /**
   * Returns {@code this}.
   * 
   * @return {@code this}
   */
  protected abstract P self();

}
