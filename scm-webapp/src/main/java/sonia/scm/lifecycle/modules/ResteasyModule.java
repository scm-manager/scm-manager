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

package sonia.scm.lifecycle.modules;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;

import java.util.Map;

/**
 * Module to configure resteasy with guice.
 */
public class ResteasyModule extends ServletModule {
  @Override
  protected void configureServlets() {
    Map<String, String> initParams = ImmutableMap.of(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, "/api");
    serve("/api/*").with(ResteasyAllInOneServletDispatcher.class, initParams);
  }
}
