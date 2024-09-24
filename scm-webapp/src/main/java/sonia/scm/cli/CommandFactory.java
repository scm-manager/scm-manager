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

package sonia.scm.cli;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import picocli.CommandLine;

class CommandFactory implements CommandLine.IFactory {

  private final Injector injector;

  public CommandFactory(Injector injector, CliContext context) {
    PermissionDescriptionResolverFactory permissionDescriptionResolverFactory = injector.getInstance(PermissionDescriptionResolverFactory.class);
    this.injector = injector.createChildInjector(new CliContextModule(permissionDescriptionResolverFactory, context));
  }

  @Override
  public <K> K create(Class<K> cls) throws Exception {
    return injector.getInstance(cls);
  }

  static class CliContextModule extends AbstractModule {

    private final CliContext context;
    private final PermissionDescriptionResolver permissionDescriptionResolver;

    private CliContextModule(PermissionDescriptionResolverFactory permissionDescriptionResolverFactory, CliContext context) {
      this.context = context;
      permissionDescriptionResolver = permissionDescriptionResolverFactory.createResolver(context.getLocale());
    }

    @Override
    protected void configure() {
      bind(CliContext.class).toInstance(context);
      bind(PermissionDescriptionResolver.class).toInstance(permissionDescriptionResolver);
    }
  }
}
