/**
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
