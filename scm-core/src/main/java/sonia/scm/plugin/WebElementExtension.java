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

package sonia.scm.plugin;

import lombok.Value;

/**
 * WebElementExtension can be a servlet or filter which is ready to bind.
 * Those extensions are loaded by the {@link ExtensionProcessor} from a {@link WebElementDescriptor}.
 *
 * We don't know if we can load the defined class from the descriptor, because the class could be optional
 * (annotated with {@link Requires}). So we have to load the class as string with {@link WebElementDescriptor} and when
 * we know that it is safe to load the class (all requirements are fulfilled), we will create our WebElementExtension.
 *
 * @since 2.0.0
 */
@Value
public class WebElementExtension {
  Class<?> clazz;
  WebElementDescriptor descriptor;
}
