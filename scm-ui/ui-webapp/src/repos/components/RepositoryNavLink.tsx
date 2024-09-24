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
import { Repository } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";
import { RouteProps } from "react-router-dom";

type Props = {
  repository: Repository;
  to: string;
  label: string;
  linkName: string;
  activeWhenMatch?: (route: RouteProps) => boolean;
  activeOnlyWhenExact: boolean;
  icon?: string;
  title?: string;
};

/**
 * Component renders only if the repository contains the link with the given name.
 */
const RepositoryNavLink: FC<Props> = ({ repository, linkName, ...props }) => {
  if (!repository._links[linkName]) {
    return null;
  }

  return <NavLink {...props} />;
};

export default RepositoryNavLink;
