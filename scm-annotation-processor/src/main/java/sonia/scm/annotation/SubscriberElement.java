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
