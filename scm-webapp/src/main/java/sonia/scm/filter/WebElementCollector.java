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

package sonia.scm.filter;


import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priorities;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.WebElementExtension;

import java.util.List;


public final class WebElementCollector
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(WebElementCollector.class);

  private final Iterable<TypedWebElementDescriptor<Filter>> filters;

  private final Iterable<TypedWebElementDescriptor<HttpServlet>> servlets;
 
  @SuppressWarnings("unchecked")
  private WebElementCollector(Iterable<WebElementExtension> elements)
  {
    List<TypedWebElementDescriptor<Filter>> fl = Lists.newArrayList();
    List<TypedWebElementDescriptor<HttpServlet>> sl = Lists.newArrayList();

    for (WebElementExtension element : elements)
    {
      if (Filter.class.isAssignableFrom(element.getClazz()))
      {
        fl.add(
          new TypedWebElementDescriptor<>(
            (Class<Filter>) element.getClazz(), element.getDescriptor()));
      }
      else if (Servlet.class.isAssignableFrom(element.getClazz()))
      {
        sl.add(
          new TypedWebElementDescriptor<>(
            (Class<HttpServlet>) element.getClazz(), element.getDescriptor()));
      }
      else
      {
        logger.warn(
          "found class {} witch is annotated with webelement annotation, but is neither an filter or servlet",
          element.getClazz());
      }
    }

    TypedWebElementDescriptorOrdering ordering = new TypedWebElementDescriptorOrdering();

    filters = ordering.immutableSortedCopy(fl);
    servlets = ordering.immutableSortedCopy(sl);
  }



  public static WebElementCollector collect(PluginLoader loader)
  {
    return new WebElementCollector(
      loader.getExtensionProcessor().getWebElements());
  }


  
  public Iterable<TypedWebElementDescriptor<Filter>> getFilters()
  {
    return filters;
  }

  
  public Iterable<TypedWebElementDescriptor<HttpServlet>> getServlets()
  {
    return servlets;
  }




  private static class TypedWebElementDescriptorOrdering
    extends Ordering<TypedWebElementDescriptor<?>>
  {

    @Override
    public int compare(TypedWebElementDescriptor<?> left,
      TypedWebElementDescriptor<?> right)
    {
      return Ints.compare(Priorities.getPriority(left.getClazz()),
        Priorities.getPriority(right.getClazz()));
    }
  }

}
