/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.Priorities;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.WebElementDescriptor;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Sebastian Sdorra
 */
public final class WebElementCollector
{

  /**
   * the logger for WebElementCollector
   */
  private static final Logger logger =
    LoggerFactory.getLogger(WebElementCollector.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param elements
   */
  @SuppressWarnings("unchecked")
  private WebElementCollector(Iterable<WebElementDescriptor> elements)
  {
    List<TypedWebElementDescriptor<? extends Filter>> fl = Lists.newArrayList();
    List<TypedWebElementDescriptor<? extends HttpServlet>> sl =
      Lists.newArrayList();

    for (WebElementDescriptor element : elements)
    {
      if (Filter.class.isAssignableFrom(element.getClazz()))
      {
        fl.add(
          new TypedWebElementDescriptor<>(
            (Class<? extends Filter>) element.getClazz(), element));
      }
      else if (Servlet.class.isAssignableFrom(element.getClazz()))
      {
        sl.add(
          new TypedWebElementDescriptor<>(
            (Class<? extends HttpServlet>) element.getClazz(), element));
      }
      else
      {
        logger.warn(
          "found class {} witch is annotated with webelement annotation, but is neither an filter or servlet",
          element.getClazz());
      }
    }

    TypedWebElementDescriptorOrdering ordering =
      new TypedWebElementDescriptorOrdering();

    filters = ordering.immutableSortedCopy(fl);
    servlets = ordering.immutableSortedCopy(sl);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param loader
   *
   * @return
   */
  public static WebElementCollector collect(PluginLoader loader)
  {
    return new WebElementCollector(
      loader.getExtensionProcessor().getWebElements());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<TypedWebElementDescriptor<? extends Filter>> getFilters()
  {
    return filters;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<TypedWebElementDescriptor<? extends HttpServlet>> getServlets()
  {
    return servlets;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 15/02/01
   * @author         Enter your name here...
   */
  private static class TypedWebElementDescriptorOrdering
    extends Ordering<TypedWebElementDescriptor<?>>
  {

    /**
     * Method description
     *
     *
     * @param left
     * @param right
     *
     * @return
     */
    @Override
    public int compare(TypedWebElementDescriptor<?> left,
      TypedWebElementDescriptor<?> right)
    {
      return Ints.compare(Priorities.getPriority(left.getClazz()),
        Priorities.getPriority(right.getClazz()));
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Iterable<TypedWebElementDescriptor<? extends Filter>> filters;

  /** Field description */
  private final Iterable<TypedWebElementDescriptor<? extends HttpServlet>> servlets;
}
