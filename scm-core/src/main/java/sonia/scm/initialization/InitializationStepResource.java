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

package sonia.scm.initialization;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.ExtensionPoint;

import java.util.Locale;

/**
 * @deprecated Limited use for Plugin Development, see as internal
 */
@ExtensionPoint
@Deprecated(since = "2.35.0", forRemoval = true)
public interface InitializationStepResource {
  String name();

  /**
   * @since 2.35.0
   */
  default void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder, Locale locale) {
    setupIndex(builder, embeddedBuilder);
  }

  void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder);
}
