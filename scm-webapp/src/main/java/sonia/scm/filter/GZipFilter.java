/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GZipFilter extends HttpFilter
{

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
                          HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException
  {
    String ae = request.getHeader("accept-encoding");

    if ((ae != null) && (ae.indexOf("gzip") != -1))
    {
      GZipResponseWrapper wrappedResponse = new GZipResponseWrapper(response);

      chain.doFilter(request, wrappedResponse);
      wrappedResponse.finishResponse();
    }
    else
    {
      chain.doFilter(request, response);
    }
  }
}
