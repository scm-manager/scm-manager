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

package sonia.scm.lifecycle.modules.resteasyguice;

import com.google.inject.AbstractModule;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class ResteasyGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
//    bind(ClientHttpEngine.class).to(ApacheHttpClient43Engine.class);
    bind(RuntimeDelegate.class).toInstance(RuntimeDelegate.getInstance());
    bind(Response.ResponseBuilder.class).toProvider(ResponseBuilderProvider.class);
    bind(UriBuilder.class).toProvider(UriBuilderProvider.class);
    bind(Variant.VariantListBuilder.class).toProvider(VariantListBuilderProvider.class);
  }
}
