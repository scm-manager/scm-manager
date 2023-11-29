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
    
package sonia.scm.repository.spi;

import com.google.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.util.Decorators;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public abstract class ScmProviderHttpServletProvider implements Provider<ScmProviderHttpServlet> {

  @Inject(optional = true)
  private Set<ScmProviderHttpServletDecoratorFactory> decoratorFactories;

  private final String type;

  protected ScmProviderHttpServletProvider(String type) {
    this.type = type;
  }

  @Override
  public ScmProviderHttpServlet get() {
    return Decorators.decorate(getRootServlet(), getDecoratorsForType());
  }

  private List<ScmProviderHttpServletDecoratorFactory> getDecoratorsForType() {
    return decoratorFactories.stream().filter(d -> d.handlesScmType(type)).collect(toList());
  }

  protected abstract ScmProviderHttpServlet getRootServlet();
}
