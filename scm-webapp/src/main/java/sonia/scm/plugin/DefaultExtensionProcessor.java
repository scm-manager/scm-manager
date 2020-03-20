/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Stopwatch;
import com.google.inject.Binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
public class DefaultExtensionProcessor implements ExtensionProcessor
{

  /**
   * the logger for DefaultExtensionProcessor
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultExtensionProcessor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param collector
   */
  public DefaultExtensionProcessor(ExtensionCollector collector)
  {
    this.collector = collector;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param extensionPoint
   *
   * @return
   */
  @Override
  public Iterable<Class> byExtensionPoint(Class extensionPoint)
  {
    return collector.byExtensionPoint(extensionPoint);
  }

  /**
   * Method description
   *
   *
   * @param extensionPoint
   *
   * @return
   */
  @Override
  public Class oneByExtensionPoint(Class extensionPoint)
  {
    return collector.oneByExtensionPoint(extensionPoint);
  }

  /**
   * Method description
   *
   *
   * @param binder
   */
  @Override
  public void processAutoBindExtensions(Binder binder)
  {
    logger.info("start processing extensions");

    Stopwatch sw = Stopwatch.createStarted();

    new ExtensionBinder(binder).bind(collector);
    logger.info("bound extensions in {}", sw.stop());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterable<WebElementDescriptor> getWebElements()
  {
    return collector.getWebElements();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ExtensionCollector collector;
}
