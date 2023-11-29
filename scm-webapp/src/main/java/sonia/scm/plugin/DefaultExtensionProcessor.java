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
import sonia.scm.config.ConfigBinding;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
public class DefaultExtensionProcessor implements ExtensionProcessor {

  /**
   * the logger for DefaultExtensionProcessor
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultExtensionProcessor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param collector
   */
  public DefaultExtensionProcessor(ExtensionCollector collector, ConfigurationResolver configurationResolver) {
    this.collector = collector;
    this.configBindings = collector.getConfigElements().stream().map(configElement -> {
      String valueAsString = configurationResolver.resolve(configElement.getKey(), configElement.getDefaultValue());
      Object value = ConfigurationTypeConverter.convert(configElement.getType(), valueAsString);
      return new ConfigBinding(configElement, value);
    }).collect(Collectors.toSet());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param extensionPoint
   * @return
   */
  @Override
  public Iterable<Class> byExtensionPoint(Class extensionPoint) {
    return collector.byExtensionPoint(extensionPoint);
  }

  /**
   * Method description
   *
   * @param extensionPoint
   * @return
   */
  @Override
  public Class oneByExtensionPoint(Class extensionPoint) {
    return collector.oneByExtensionPoint(extensionPoint);
  }

  /**
   * Method description
   *
   * @param binder
   */
  @Override
  public void processAutoBindExtensions(Binder binder) {
    logger.info("start processing extensions");

    Stopwatch sw = Stopwatch.createStarted();

    new ExtensionBinder(binder).bind(collector);
    logger.info("bound extensions in {}", sw.stop());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @return
   */
  @Override
  public Iterable<WebElementExtension> getWebElements() {
    return collector.getWebElements();
  }

  @Override
  public Iterable<Class<?>> getIndexedTypes() {
    return collector.getIndexedTypes();
  }

  @Override
  public Iterable<ConfigBinding> getConfigBindings() {
    return configBindings;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private final ExtensionCollector collector;
  private final Set<ConfigBinding> configBindings;
}
