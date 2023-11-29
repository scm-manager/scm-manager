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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@XmlRootElement(name = "subscriber")
@XmlAccessorType(XmlAccessType.FIELD)
public final class SubscriberElement
{

  /**
   * Constructs ...
   *
   */
  SubscriberElement() {}

  /**
   * Constructs ...
   *
   *
   * @param subscriberClass
   * @param eventClass
   * @param description
   */
  public SubscriberElement(Class<?> subscriberClass, Class<?> eventClass,
    String description)
  {
    this.subscriberClass = subscriberClass;
    this.eventClass = eventClass;
    this.description = description;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final SubscriberElement other = (SubscriberElement) obj;

    return Objects.equal(eventClass, other.eventClass)
      && Objects.equal(subscriberClass, other.subscriberClass)
      && Objects.equal(description, other.description);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(eventClass, subscriberClass, description);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("eventClass", eventClass)
                  .add("subscriberClass", subscriberClass)
                  .add("description", description)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<?> getEventClass()
  {
    return eventClass;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<?> getSubscriberClass()
  {
    return subscriberClass;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String description;

  /** Field description */
  @XmlElement(name = "event")
  private Class<?> eventClass;

  /** Field description */
  @XmlElement(name = "class")
  private Class<?> subscriberClass;
}
