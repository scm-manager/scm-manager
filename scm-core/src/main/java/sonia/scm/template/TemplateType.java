/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
