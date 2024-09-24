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

import { useLocalStorage } from "@scm-manager/ui-api";
import { useCallback, useMemo } from "react";

export const useSecondaryNavigation = (isNavigationCollapsible = true) => {
  const [isCollapsed, setCollapsed] = useLocalStorage<boolean>("secondaryNavigation.collapsed", false);
  const [isRouteCollapsible, setRouteCollapsible] = useLocalStorage<boolean>("secondaryNavigation.collapsible", true);

  const collapsible = useMemo(
    () => isRouteCollapsible && isNavigationCollapsible,
    [isNavigationCollapsible, isRouteCollapsible]
  );
  const collapsed = useMemo(() => collapsible && isCollapsed, [collapsible, isCollapsed]);

  const toggleCollapse = useCallback(() => {
    if (collapsible) {
      setCollapsed((previousIsCollapsed) => !previousIsCollapsed);
    }
  }, [collapsible, setCollapsed]);

  return useMemo(
    () => ({
      collapsed,
      collapsible,
      setCollapsible: setRouteCollapsible,
      toggleCollapse,
    }),
    [collapsed, collapsible, setRouteCollapsible, toggleCollapse]
  );
};
