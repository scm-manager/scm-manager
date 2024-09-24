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

import { urls } from "@scm-manager/ui-api";
import { useLocation, useRouteMatch } from "react-router-dom";
import { RoutingProps } from "./RoutingProps";

const useActiveMatch = ({ to, activeOnlyWhenExact, activeWhenMatch }: RoutingProps) => {
  let path = to;
  const index = to.indexOf("?");
  if (index > 0) {
    path = to.substr(0, index);
  }

  const match = useRouteMatch({
    path: urls.escapeUrlForRoute(path),
    exact: activeOnlyWhenExact,
  });

  const location = useLocation();

  const isActiveWhenMatch = () => {
    if (activeWhenMatch) {
      // noinspection PointlessBooleanExpressionJS could be a truthy value if a function is passed in
      return !!activeWhenMatch({
        location,
      });
    }
    return false;
  };

  return !!match || isActiveWhenMatch();
};

export default useActiveMatch;
