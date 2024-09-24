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

import java.util.Map;
import java.util.Map.Entry;


public class ClassSetElement implements DescriptorElement {

  private static final String EL_CLASS = "class";
  private static final String EL_DESCRIPTION = "description";

  private final String elementName;
  private final Iterable<ClassWithAttributes> classes;

  public ClassSetElement(String elementName, Iterable<ClassWithAttributes> classes) {
    this.elementName = elementName;
    this.classes = classes;
  }

  @Override
  public void append(Document doc, Element root) {
    for (ClassWithAttributes c : classes) {
      Element element = doc.createElement(elementName);
      Element classEl = doc.createElement(EL_CLASS);

      classEl.setTextContent(c.className);

      if (!Strings.isNullOrEmpty(c.description)) {
        Element descriptionEl = doc.createElement(EL_DESCRIPTION);

        descriptionEl.setTextContent(c.description);
        element.appendChild(descriptionEl);
      }

      for (Entry<String, String> e : c.attributes.entrySet()) {
        Element attr = doc.createElement(e.getKey());

        attr.setTextContent(e.getValue());
        element.appendChild(attr);
      }

      if (c.requires != null) {
        for (String requiresEntry : c.requires) {
          Element requiresElement = doc.createElement("requires");
          requiresElement.setTextContent(requiresEntry);
          element.appendChild(requiresElement);
        }
      }

      element.appendChild(classEl);
      root.appendChild(element);
    }
  }

  public static class ClassWithAttributes {

    private final String className;
    private final String description;
    private final String[] requires;
    private final Map<String, String> attributes;

    public ClassWithAttributes(String className, String description,
                               String[] requires, Map<String, String> attributes) {
      this.className = className;
      this.description = description;
      this.requires = requires;
      this.attributes = attributes;
    }
  }
}
