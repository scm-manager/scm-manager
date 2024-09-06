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

package sonia.scm.update;

import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.StaticResourceServlet;

class MigrationWizardModule extends ServletModule {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationWizardModule.class);

  @Override
  protected void configureServlets() {
    LOG.info("==========================================================");
    LOG.info("=                                                        =");
    LOG.info("=             STARTING MIGRATION SERVLET                 =");
    LOG.info("=                                                        =");
    LOG.info("=   Open SCM-Manager in a browser to start the wizard.   =");
    LOG.info("=                                                        =");
    LOG.info("==========================================================");
    serve("/images/*", "/assets/*", "/favicon.ico").with(StaticResourceServlet.class);
    serve("/*").with(MigrationWizardServlet.class);
  }
}
