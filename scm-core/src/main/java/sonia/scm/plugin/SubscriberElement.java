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


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @since 2.0.0
 */
@XmlRootElement(name = "subscriber")
@XmlAccessorType(XmlAccessType.FIELD)
public final class SubscriberElement
{
  private String description;

  @XmlElement(name = "event")
  private Class<?> eventClass;

  @XmlElement(name = "class")
  private Class<?> subscriberClass;

  SubscriberElement() {}

  public SubscriberElement(Class<?> subscriberClass, Class<?> eventClass,
    String description)
  {
    this.subscriberClass = subscriberClass;
    this.eventClass = eventClass;
    this.description = description;
  }

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

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(eventClass, subscriberClass, description);
  }

  
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


  
  public String getDescription()
  {
    return description;
  }

  
  public Class<?> getEventClass()
  {
    return eventClass;
  }

  
  public Class<?> getSubscriberClass()
  {
    return subscriberClass;
  }

}
