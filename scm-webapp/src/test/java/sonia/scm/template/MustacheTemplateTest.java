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

package sonia.scm.template;


import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import com.google.common.base.Function;

import java.io.IOException;

import java.util.Map;


public class MustacheTemplateTest extends TemplateTestBase
{


  @Override
  public Template getFailureTemplate() throws IOException
  {
    return getTemplate("sonia/scm/template/003.mustache");
  }

  
  @Override
  public Template getHelloTemplate()
  {
    return getTemplate("sonia/scm/template/001.mustache");
  }



  @Override
  protected void prepareEnv(Map<String, Object> env)
  {
    env.put("test", (Function<String, String>) input -> {
      throw new UnsupportedOperationException("Not supported yet.");
    });
  }



  private Template getTemplate(String path)
  {
    DefaultMustacheFactory factory = new DefaultMustacheFactory();
    Mustache mustache = factory.compile(path);

    return new MustacheTemplate(path, mustache);
  }
}
