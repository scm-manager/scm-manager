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

package sonia.scm;

/**
 * The constants in this class represent the current state of the running
 * SCM_Manager instance. The stage can be queried by calling
 * {@link SCMContextProvider#getStage()}.
 *
 * @since 1.12
 */
public enum Stage {

  /**
   * This value indicates SCM-Manager is right now in development.
   */
  DEVELOPMENT(com.google.inject.Stage.DEVELOPMENT),

  /**
   * This value indicates SCM-Manager is right now productive.
   */
  PRODUCTION(com.google.inject.Stage.PRODUCTION),

  /**
   * This value indicates SCM-Manager is right now in development but specifically configured for testing.
   *
   * @since 2.47.0
   */
  TESTING(com.google.inject.Stage.DEVELOPMENT);

  private final com.google.inject.Stage injectionStage;

  private Stage(com.google.inject.Stage injectionStage) {
    this.injectionStage = injectionStage;
  }

  /**
   * Returns the injection stage
   *
   * @since 2.0.0
   */
  public com.google.inject.Stage getInjectionStage() {
    return injectionStage;
  }

}
