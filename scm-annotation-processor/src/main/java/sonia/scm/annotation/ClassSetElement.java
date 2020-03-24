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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Sebastian Sdorra
 */
public class ClassSetElement implements DescriptorElement
{

  /** Field description */
  private static final String EL_CLASS = "class";

  /** Field description */
  private static final String EL_DESCRIPTION = "description";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param elementName
   * @param classes
   */
  public ClassSetElement(String elementName,
    Iterable<ClassWithAttributes> classes)
  {
    this.elementName = elementName;
    this.classes = classes;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param doc
   * @param root
   */
  @Override
  public void append(Document doc, Element root)
  {

    for (ClassWithAttributes c : classes)
    {
      Element element = doc.createElement(elementName);
      Element classEl = doc.createElement(EL_CLASS);

      classEl.setTextContent(c.className);

      if (!Strings.isNullOrEmpty(c.description))
      {
        Element descriptionEl = doc.createElement(EL_DESCRIPTION);

        descriptionEl.setTextContent(c.description);
        element.appendChild(descriptionEl);
      }

      for (Entry<String, String> e : c.attributes.entrySet())
      {
        Element attr = doc.createElement(e.getKey());

        attr.setTextContent(e.getValue());
        element.appendChild(attr);
      }

      element.appendChild(classEl);
      root.appendChild(element);
    }

  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/03/18
   * @author         Enter your name here...
   */
  public static class ClassWithAttributes
  {

    /**
     * Constructs ...
     *
     *
     * @param className
     * @param description
     * @param attributes
     */
    public ClassWithAttributes(String className, String description,
      Map<String, String> attributes)
    {
      this.className = className;
      this.description = description;
      this.attributes = attributes;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Map<String, String> attributes;

    /** Field description */
    private final String className;

    /** Field description */
    private final String description;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Iterable<ClassWithAttributes> classes;

  /** Field description */
  private final String elementName;
}
