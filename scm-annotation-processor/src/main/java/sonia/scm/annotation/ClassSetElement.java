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
