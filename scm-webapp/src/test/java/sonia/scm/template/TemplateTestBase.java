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


import com.google.common.collect.Maps;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import java.util.Map;


public abstract class TemplateTestBase
{


  public abstract Template getFailureTemplate() throws IOException;


  public abstract Template getHelloTemplate() throws IOException;



  @Test
  public void testRender() throws IOException
  {
    Template template = getHelloTemplate();

    assertEquals("Hello marvin!", execute(template));
  }


  @Test(expected = IOException.class)
  public void testRenderFailure() throws IOException
  {
    Template template = getFailureTemplate();

    execute(template);
  }


  protected void prepareEnv(Map<String, Object> env) {}


  private String execute(Template template) throws IOException
  {
    Map<String, Object> env = Maps.newHashMap();

    env.put("name", "marvin");

    prepareEnv(env);

    StringWriter writer = new StringWriter();

    template.execute(writer, env);

    return writer.toString();
  }
}
