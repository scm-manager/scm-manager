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

export { default as binder, Binder, ExtensionPointDefinition } from "./binder";
export * from "./useBinder";
export {
  default as ExtensionPoint,
  RenderableExtensionPointDefinition,
  SimpleRenderableDynamicExtensionPointDefinition,
} from "./ExtensionPoint";
export { default as ExtractProps } from "./extractProps";

// suppress eslint prettier warning,
// because prettier does not understand "* as"
// eslint-disable-next-line prettier/prettier
export * as extensionPoints from "./extensionPoints";
