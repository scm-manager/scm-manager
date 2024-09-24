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

import jakarta.inject.Inject;
import sonia.scm.i18n.I18nCollector;

class CliExceptionHandlerFactory {

  private final I18nCollector i18nCollector;

  @Inject
  public CliExceptionHandlerFactory(I18nCollector i18nCollector) {
    this.i18nCollector = i18nCollector;
  }

  CliExecutionExceptionHandler createExecutionExceptionHandler(String languageCode) {
    return new CliExecutionExceptionHandler(i18nCollector, languageCode);
  }

  CliParameterExceptionHandler createParameterExceptionHandler(String languageCode) {
    return new CliParameterExceptionHandler(languageCode);
  }
}
