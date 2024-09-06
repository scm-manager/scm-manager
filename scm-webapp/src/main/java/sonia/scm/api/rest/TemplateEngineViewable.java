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

package sonia.scm.api.rest;


import com.google.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.template.Viewable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


@Provider
public class TemplateEngineViewable implements MessageBodyWriter<Viewable>
{
  
  private final TemplateEngineFactory templateEngineFactory;

  @Inject
  public TemplateEngineViewable(TemplateEngineFactory templateEngineFactory)
  {
    this.templateEngineFactory = templateEngineFactory;
  }


  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.isAssignableFrom(Viewable.class);
  }

  @Override
  public long getSize(Viewable viewable, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Viewable viewable, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    String path = viewable.getPath();
    
    TemplateEngine engine = templateEngineFactory.getEngineByExtension(path);

    if (engine == null)
    {
      throw new IOException("could not find template engine for ".concat(path));
    }

    Template template = engine.getTemplate(path);

    if (template == null)
    {
      throw new IOException("could not find template for ".concat(path));
    }

    PrintWriter writer = new PrintWriter(entityStream);

    template.execute(writer, viewable.getContext());
    writer.flush();
  }
}
