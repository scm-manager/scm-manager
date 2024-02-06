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

package sonia.scm.repository;


import sonia.scm.repository.api.HookContext;

/**
 * Repository hook event represents an change event of a repository.
 *
 * @since 1.6
 */
public class RepositoryHookEvent
{

  /** context of current hook */
  private final HookContext context;

  /** modified repository */
  private final Repository repository;

  /** hook type */
  private final RepositoryHookType type;

  public RepositoryHookEvent(HookContext context, Repository repository,
    RepositoryHookType type)
  {
    this.context = context;
    this.repository = repository;
    this.type = type;
  }


  public HookContext getContext()
  {
    return context;
  }

  
  public Repository getRepository()
  {
    return repository;
  }

  
  public RepositoryHookType getType()
  {
    return type;
  }

  @Override
  public String toString() {
    return "RepositoryHookEvent{" +
      "repository=" + repository +
      ", type=" + type +
      '}';
  }
}
