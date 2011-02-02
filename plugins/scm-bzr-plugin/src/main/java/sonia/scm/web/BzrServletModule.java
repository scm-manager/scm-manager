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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.servlet.ServletModule;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.web.filter.BasicAuthenticationFilter;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class BzrServletModule extends ServletModule
{

  /** Field description */
  public static final String MAPPING_BZR = "/bzr/*";

  /** Field description */
  public static final String MAPPING_PUBLICBZR = "/public/bzr/*";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    filter(MAPPING_BZR,
           MAPPING_PUBLICBZR).through(BasicAuthenticationFilter.class);
    filter(MAPPING_BZR).through(BzrPermissionFilter.class);
    filter(MAPPING_PUBLICBZR).through(BzrPublicFilter.class);
    serve(MAPPING_BZR, MAPPING_PUBLICBZR).with(BzrCGIServlet.class);
  }
}
