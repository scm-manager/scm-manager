/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
