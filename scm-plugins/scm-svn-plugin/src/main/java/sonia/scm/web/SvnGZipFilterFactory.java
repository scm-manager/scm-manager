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

package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletDecoratorFactory;

@Extension
public class SvnGZipFilterFactory implements ScmProviderHttpServletDecoratorFactory {

  private final SvnRepositoryHandler handler;

  @Inject
  public SvnGZipFilterFactory(SvnRepositoryHandler handler) {
    this.handler = handler;
  }

  @Override
  public boolean handlesScmType(String type) {
    return SvnRepositoryHandler.TYPE_NAME.equals(type);
  }

  @Override
  public ScmProviderHttpServlet createDecorator(ScmProviderHttpServlet delegate) {
    return new SvnGZipFilter(handler, delegate);
  }
}
