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


import com.google.common.collect.Lists;

import sonia.scm.Type;

import java.io.Serializable;

import java.util.Collection;

/**
 * Represents the type of a {@link TemplateType}.
 *
 * @since 1.19
 */
public final class TemplateType extends Type implements Serializable
{

  private static final long serialVersionUID = 7947596921895752539L;

  private Collection<String> extensions;
  public TemplateType(String name, String displayName,
    Collection<String> extensions)
  {
    super(name, displayName);
    this.extensions = extensions;
  }

  public TemplateType(String name, String displayName, String extension,
    String... extensions)
  {
    this(name, displayName, Lists.asList(extension, extensions));
  }


  /**
   * Returns all extensions associated with this template engine.
   *
   *
   * @return all extensions
   */
  public Collection<String> getExtensions()
  {
    return extensions;
  }

}
