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

import java.io.IOException;

/**
 * This exception is thrown, if an error during the template rendering 
 * phase occurs.
 *
 * @since 1.19
 */
public class TemplateRenderException extends IOException
{

  private static final long serialVersionUID = 2176596549717814756L;


  public TemplateRenderException() {}

  public TemplateRenderException(String message)
  {
    super(message);
  }

  public TemplateRenderException(Throwable cause)
  {
    super(cause);
  }

  public TemplateRenderException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
