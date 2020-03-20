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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@XmlRootElement(name = "module")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmModule
{

  /** Field description */
  private static final Unwrapper unwrapper = new Unwrapper();

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getEvents()
  {
    return unwrap(events);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<ExtensionPointElement> getExtensionPoints()
  {
    return nonNull(extensionPoints);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<ExtensionElement> getExtensions()
  {
    return nonNull(extensions);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getRestProviders()
  {
    return unwrap(restProviders);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Class<?>> getRestResources()
  {
    return unwrap(restResources);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<SubscriberElement> getSubscribers()
  {
    return nonNull(subscribers);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<WebElementDescriptor> getWebElements()
  {
    return nonNull(webElements);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param iterable
   * @param <T>
   *
   * @return
   */
  private <T> Iterable<T> nonNull(Iterable<T> iterable)
  {
    if (iterable == null)
    {
      iterable = ImmutableSet.of();
    }

    return iterable;
  }

  /**
   * Method description
   *
   *
   * @param iterable
   *
   * @return
   */
  private Iterable<Class<?>> unwrap(Iterable<ClassElement> iterable)
  {
    Iterable<Class<?>> unwrapped;

    if (iterable != null)
    {
      unwrapped = Iterables.transform(iterable, unwrapper);
    }
    else
    {
      unwrapped = ImmutableSet.of();
    }

    return unwrapped;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/03/28
   * @author         Enter your name here...
   */
  private static class Unwrapper implements Function<ClassElement, Class<?>>
  {

    /**
     * Method description
     *
     *
     * @param classElement
     *
     * @return
     */
    @Override
    public Class<?> apply(ClassElement classElement)
    {
      return classElement.getClazz();
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "event")
  private Set<ClassElement> events;

  /** Field description */
  @XmlElement(name = "extension-point")
  private Set<ExtensionPointElement> extensionPoints;

  /** Field description */
  @XmlElement(name = "extension")
  private Set<ExtensionElement> extensions;

  /** Field description */
  @XmlElement(name = "rest-provider")
  private Set<ClassElement> restProviders;

  /** Field description */
  @XmlElement(name = "rest-resource")
  private Set<ClassElement> restResources;

  /** Field description */
  @XmlElement(name = "subscriber")
  private Set<SubscriberElement> subscribers;

  /** Field description */
  @XmlElement(name = "web-element")
  private Set<WebElementDescriptor> webElements;
}
