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



package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ScmState;
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import javax.xml.bind.JAXBContext;

/**
 *
 * @author Sebastian Sdorra
 */
@Provider
public class JsonJaxbContextResolver implements ContextResolver<JAXBContext>
{

  /**
   * Constructs ...
   *
   *
   * @throws Exception
   */
  public JsonJaxbContextResolver() throws Exception
  {
    this.context = new JSONJAXBContext(
        JSONConfiguration.mapped().rootUnwrapping(true).arrays(
          "member", "groups", "permissions", "repositories",
          "repositoryTypes", "users").nonStrings(
            "readable", "writeable", "groupPermission",
            "admin").build(), types.toArray(new Class[0]));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param objectType
   *
   * @return
   */
  @Override
  public JAXBContext getContext(Class<?> objectType)
  {
    return (types.contains(objectType))
           ? context
           : null;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private List<Class> types = Arrays.asList(new Class[] { Group.class,
          Repository.class, ScmState.class, User.class });
}
