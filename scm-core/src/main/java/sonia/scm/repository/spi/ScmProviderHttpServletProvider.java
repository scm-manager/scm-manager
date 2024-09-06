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
