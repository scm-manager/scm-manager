/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.btrace;

//~--- JDK imports ------------------------------------------------------------

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;

import java.io.IOException;
import java.io.Writer;

import java.util.Map;

import static com.sun.btrace.BTraceUtils.*;

/**
 *
 * @author Sebastian Sdorra
 */
@BTrace
public class IndexPage
{

  /** Field description */
  private static String space = "";

  /** Field description */
  private static boolean renderMapEntries = false;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @OnMethod(clazz = "sonia.scm.boot.BootstrapFilter", method = "doFilter")
  public static void onEnterBootstrapFilter()
  {
    printSpacedLine("enter BootstrapFilter");
    incSpace();
  }

  /**
   * Method description
   *
   *
   * @param request
   */
  @OnMethod(clazz = "sonia.scm.filter.GZipFilter", method = "doFilter")
  public static void onEnterGzipFilter()
  {
    printSpacedLine("Enter GZipFilter");
    incSpace();
  }

  /**
   * Method description
   *
   */
  @OnMethod(clazz = "sonia.scm.template.TemplateServlet", method = "doGet")
  public static void onEnterTemplateSevlet()
  {
    renderMapEntries = true;
    printSpacedLine("enter TemplateServlet");
    incSpace();
  }

  /**
   * Method description
   *
   */
  @OnMethod(
    clazz = "sonia.scm.boot.BootstrapFilter",
    method = "doFilter",
    location = @Location(Kind.RETURN)
  )
  public static void onExitBootstrapFilter()
  {
    decSpace();
    printSpacedLine("exit BootstrapFilter");
  }

  /**
   * Method description
   *
   */
  @OnMethod(
    clazz = "sonia.scm.filter.GZipFilter",
    method = "doFilter",
    location = @Location(Kind.RETURN)
  )
  public static void onExitGzipFilter()
  {
    decSpace();
    printSpacedLine("Exit GZipFilter");
  }

  /**
   * Method description
   *
   *
   * @param templateName
   * @param writer
   * @param params
   */
  @OnMethod(
    clazz = "sonia.scm.template.FreemarkerTemplateHandler",
    method = "render",
    location = @Location(Kind.RETURN)
  )
  public static void onExitTemplate(String templateName, Writer writer,
    Map<String, ? extends Object> params)
  {
    printSpacedLine(strcat("exit template: ", templateName));
    decSpace();
  }

  /**
   * Method description
   *
   */
  @OnMethod(
    clazz = "sonia.scm.template.TemplateServlet",
    method = "doGet",
    location = @Location(Kind.RETURN)
  )
  public static void onExitTemplateSevlet()
  {
    decSpace();
    printSpacedLine("exit TemplateServlet");
    renderMapEntries = false;
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   *
   * @throws IOException
   */
  @OnMethod(clazz = "java.util.HashMap", method = "put")
  public static void onHashMapPut(Object key, Object value) throws IOException
  {
    if (renderMapEntries)
    {
      String msg = strcat("  ", str(key));

      msg = strcat(msg, " = ");
      msg = strcat(msg, str(value));
      printSpacedLine(msg);
    }
  }

  /**
   * Method description
   *
   *
   * @param templateName
   * @param writer
   * @param params
   */
  @OnMethod(clazz = "sonia.scm.template.FreemarkerTemplateHandler",
    method = "render")
  public static void onRenderTemplate(String templateName, Writer writer,
    Map<String, ? extends Object> params)
  {
    incSpace();
    printSpacedLine(strcat("render template: ", templateName));
  }

  /**
   * Method description
   *
   */
  private static void decSpace()
  {
    space = Strings.substr(space, 1);
  }

  /**
   * Method description
   *
   */
  private static void incSpace()
  {
    space = strcat(space, " ");
  }

  /**
   * Method description
   *
   *
   * @param text
   */
  private static void printSpacedLine(String text)
  {
    println(strcat(space, text));
  }
}
