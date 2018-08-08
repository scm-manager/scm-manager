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
import org.mapstruct.factory.Mappers;
import sonia.scm.api.v2.resources.SvnConfigDtoToSvnConfigMapper;
import sonia.scm.api.v2.resources.SvnConfigToSvnConfigDtoMapper;
import sonia.scm.plugin.Extension;

import java.util.HashMap;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class SvnServletModule extends ServletModule
{

  /** Field description */
  public static final String PARAMETER_SVN_PARENTPATH = "SVNParentPath";

  /** Field description */
  public static final String PATTERN_SVN = "/svn/*";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    filter(PATTERN_SVN).through(SvnGZipFilter.class);
    filter(PATTERN_SVN).through(SvnBasicAuthenticationFilter.class);
    filter(PATTERN_SVN).through(SvnPermissionFilter.class);

    bind(SvnConfigDtoToSvnConfigMapper.class).to(Mappers.getMapper(SvnConfigDtoToSvnConfigMapper.class).getClass());
    bind(SvnConfigToSvnConfigDtoMapper.class).to(Mappers.getMapper(SvnConfigToSvnConfigDtoMapper.class).getClass());

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(PARAMETER_SVN_PARENTPATH,
      System.getProperty("java.io.tmpdir"));
    serve(PATTERN_SVN).with(SvnDAVServlet.class, parameters);
  }
}
