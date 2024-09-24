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

package sonia.scm.annotation;


import com.google.common.base.Strings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class SubscriberElement implements DescriptorElement
{

  private static final String EL_CLASS = "class";

  private static final String EL_DESCRIPTION = "description";

  private static final String EL_EVENT = "event";

  private static final String EL_SUBSCRIBER = "subscriber";

  private final String description;

  private final String eventType;

  private final String subscriberType;

  public SubscriberElement(String subscriberType, String eventType,
    String description)
  {
    this.subscriberType = subscriberType;
    this.eventType = eventType;
    this.description = description;
  }



  @Override
  public void append(Document doc, Element root)
  {
    Element subscriberEl = doc.createElement(EL_SUBSCRIBER);
    Element classEl = doc.createElement(EL_CLASS);

    classEl.setTextContent(subscriberType);
    subscriberEl.appendChild(classEl);

    Element eventEl = doc.createElement(EL_EVENT);

    eventEl.setTextContent(eventType);
    subscriberEl.appendChild(eventEl);

    if (!Strings.isNullOrEmpty(description))
    {
      Element descriptionEl = doc.createElement(EL_DESCRIPTION);

      descriptionEl.setTextContent(description);
      subscriberEl.appendChild(descriptionEl);
    }

    root.appendChild(subscriberEl);
  }

}
