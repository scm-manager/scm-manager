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



package sonia.scm.graph;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

import sonia.scm.util.IOUtil;
import sonia.scm.util.SecurityUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GraphServlet extends HttpServlet
{

  /** Field description */
  public static final String CONTENT_TYPE = "application/octet-stream";

  /** Field description */
  private static final long serialVersionUID = 2733077353390468391L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param securityContextProvider
   * @param injector
   */
  @Inject
  public GraphServlet(Provider<WebSecurityContext> securityContextProvider,
                      Injector injector)
  {
    this.securityContextProvider = securityContextProvider;
    this.injector = injector;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(response);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(response);
  }

  /**
   * Method description
   *
   *
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  private void processRequest(HttpServletResponse response)
          throws ServletException, IOException
  {
    SecurityUtil.assertIsAdmin(securityContextProvider);

    PrintWriter writer = null;
    PrintWriter bufferWriter = null;

    try
    {
      response.setContentType(CONTENT_TYPE);

      Injector grapherInjector = Guice.createInjector(new GrapherModule(),
                                   new GraphvizModule());
      GraphvizRenderer renderer =
        grapherInjector.getInstance(GraphvizRenderer.class);
      StringWriter buffer = new StringWriter();

      bufferWriter = new PrintWriter(buffer);
      renderer.setOut(bufferWriter).setRankdir("TB");
      grapherInjector.getInstance(InjectorGrapher.class).of(injector).graph();
      bufferWriter.flush();
      writer = response.getWriter();

      String graphString = buffer.toString();

      writer.print(graphString.replaceAll("invis", "solid"));
    }
    finally
    {
      IOUtil.close(writer);
      IOUtil.close(bufferWriter);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Injector injector;

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
