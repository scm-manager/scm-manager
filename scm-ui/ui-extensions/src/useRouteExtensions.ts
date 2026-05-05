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

import { type RouteObject } from "react-router-dom";
import useBinder from "./useBinder";
import { ExtensionPointDefinition } from "./binder";

export type RouteExtensionPointDefinition<Name extends string, P = void> = ExtensionPointDefinition<
  Name,
  (props: P) => RouteObject | RouteObject[] | null,
  P
>;

interface UseRouteExtensionsProps<E extends RouteExtensionPointDefinition<string, any>> {
  name: E["name"];
  props?: E["props"];
}

export function useRouteExtensions<E extends RouteExtensionPointDefinition<string, any>>({
  name,
  props,
}: UseRouteExtensionsProps<E>) {
  const binder = useBinder();

  if (!binder.hasExtension<E>(name, props)) {
    return [];
  }
  const extensionFunctions = binder.getExtensions<E>(name, props);

  return extensionFunctions.flatMap((createRouteFn) => {
    const elementOrElements = createRouteFn(props);

    if (!elementOrElements) {
      return [];
    }

    return Array.isArray(elementOrElements) ? elementOrElements : [elementOrElements];
  });
}
