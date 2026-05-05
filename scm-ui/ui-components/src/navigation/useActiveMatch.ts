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

import { useLocation, useResolvedPath } from "react-router-dom";
import { RoutingProps } from "./RoutingProps";

const useActiveMatch = ({ to, activeWhenMatch, matchWhenExact = false }: RoutingProps) => {
  const location = useLocation();
  const resolved = useResolvedPath(to);

  const currentPath = location.pathname;
  const targetPath = resolved.pathname;

  let isPathActive: boolean;

  if (matchWhenExact) {
    isPathActive = currentPath === targetPath;
  } else {
    isPathActive = currentPath === targetPath || currentPath.startsWith(`${targetPath}/`);
  }

  const isActive = !resolved.hash && isPathActive;

  const match =
    activeWhenMatch &&
    (typeof activeWhenMatch === "string" ? new RegExp(activeWhenMatch) : activeWhenMatch).test(currentPath || "");

  return isActive || !!match;
};

export default useActiveMatch;
