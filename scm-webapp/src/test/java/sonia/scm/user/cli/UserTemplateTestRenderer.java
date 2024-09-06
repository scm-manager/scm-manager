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

package sonia.scm.user.cli;

import sonia.scm.cli.TemplateTestRenderer;

import java.util.ResourceBundle;

class UserTemplateTestRenderer {
  private final TemplateTestRenderer testRenderer = new TemplateTestRenderer();
  private final UserCommandBeanMapper beanMapper = new UserCommandBeanMapperImpl();
  private final UserTemplateRenderer templateRenderer = new UserTemplateRenderer(testRenderer.getContextMock(), testRenderer.getTemplateEngineFactory(), beanMapper) {
    @Override
    protected ResourceBundle getBundle() {
      return testRenderer.getResourceBundle();
    }
  };

  UserTemplateRenderer getTemplateRenderer() {
    return templateRenderer;
  }

  String getStdOut() {
    return testRenderer.getStdOut();
  }

  String getStdErr() {
    return testRenderer.getStdErr();
  }

  public void setLocale(String locale) {
    testRenderer.setLocale(locale);
  }
}
