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
import com.google.common.collect.Lists;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper class for selenium page objects.
 * 
 * @author Sebastian Sdorra
 */
public final class Pages {
  
  private Pages() {
  }
  
  /**
   * Creates an instance of the given page object.
   * 
   * @param <T> page object type
   * @param driver selenium driver
   * @param clazz page object type
   * @param otherArguments other constructor arguments
   * 
   * @return instance of page object
   */
  public static <T extends BasePage> T get(WebDriver driver, Class<T> clazz, Object... otherArguments)
  {
    T page = null;
    try {
      List<Class<?>> argumentTypes = Lists.newArrayList();
      argumentTypes.add(WebDriver.class);
      for (Object argument : otherArguments) {
        argumentTypes.add(argument.getClass());
      }
      
      List<Object> arguments = Lists.newArrayList();
      arguments.add(driver);
      arguments.addAll(Arrays.asList(otherArguments));
      
      Constructor<T> constructor = clazz.getDeclaredConstructor(
        argumentTypes.toArray(new Class<?>[argumentTypes.size()])
      );
      page = constructor.newInstance(arguments.toArray(new Object[arguments.size()]));
      
      PageFactory.initElements(new DefaultElementLocatorFactory(new WaitingSearchContext(driver)), page);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
    return page;
  }
  
  private static class WaitingSearchContext implements SearchContext {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private WaitingSearchContext(WebDriver driver) {
      this.driver = driver;
      this.wait = new WebDriverWait(driver, 1);
    }
    
    @Override
    public List<WebElement> findElements(By by) {
      return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    @Override
    public WebElement findElement(By by) {
      return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }
  }
}
