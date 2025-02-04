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
import classNames from "classnames";
import { useSecondaryNavigation } from "../useSecondaryNavigation";
import ExternalLink from "./ExternalLink";

type Props = {
  to: string;
  icon?: string;
  label: string;
};

const ExternalNavLink: FC<Props> = ({ to, icon, label }) => {
  const { collapsed } = useSecondaryNavigation();

  let showIcon;
  if (icon) {
    showIcon = (
      <>
        <i className={classNames(icon, "fa-fw")} />{" "}
      </>
    );
  }

  return (
    <li title={collapsed ? label : undefined}>
      <ExternalLink to={to} className={collapsed ? "has-text-centered" : ""} aria-label={collapsed ? label : undefined}>
        {showIcon}
        {collapsed ? null : label}
      </ExternalLink>
    </li>
  );
};

export default ExternalNavLink;
