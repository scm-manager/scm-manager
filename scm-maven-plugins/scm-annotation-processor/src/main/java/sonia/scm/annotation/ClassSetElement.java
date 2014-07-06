/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
