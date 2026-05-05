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

import React, { FC } from "react";
import { matchPath, useLocation, NavLink } from "react-router-dom";
import { createAttributesForTesting } from "../devBuild";
import classNames from "classnames";

type Props = {
  to: string;
  match: string | string[];
  label: string;
  testId?: string;
  className?: string;
};

const PrimaryNavigationLink: FC<Props> = ({ to, match, testId, label, className }) => {
  const { pathname } = useLocation();
  let isMatch: boolean;
  if (Array.isArray(match)) {
    isMatch = match.some((m) => !!matchPath(m + "/*", pathname));
  } else {
    isMatch = !!matchPath((match as string) + "/*", pathname);
  }

  return (
    <NavLink
      to={to}
      className={() => classNames(className, "navbar-item", { "is-active": isMatch })}
      {...createAttributesForTesting(testId)}
    >
      {label}
    </NavLink>
  );
};

export default PrimaryNavigationLink;
