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

import React, { FC, useEffect } from "react";
import { Link } from "react-router-dom";
import classNames from "classnames";
import { useSecondaryNavigation } from "@scm-manager/ui-components";
import { RoutingProps } from "./RoutingProps";
import useActiveMatch from "./useActiveMatch";
import { createAttributesForTesting } from "../devBuild";
import { SubNavigationContext } from "./SubNavigationContext";

type Props = RoutingProps & {
  label: string;
  title?: string;
  icon?: string;
  testId?: string;
};

const SubNavigation: FC<Props> = ({
  to,
  activeOnlyWhenExact,
  activeWhenMatch,
  icon,
  title,
  label,
  children,
  testId,
}) => {
  const { collapsed, setCollapsible } = useSecondaryNavigation();
  const parents = to.split("/");
  parents.splice(-1, 1);
  const parent = parents.join("/");

  const active = useActiveMatch({
    to: parent,
    activeOnlyWhenExact,
    activeWhenMatch,
  });

  useEffect(() => {
    if (active) {
      setCollapsible(false);
    }
  }, [active, setCollapsible]);

  let defaultIcon = "fas fa-cog";
  if (icon) {
    defaultIcon = icon;
  }

  let childrenList = null;
  if (active && !collapsed) {
    childrenList = <ul className="sub-menu">{children}</ul>;
  }

  return (
    <SubNavigationContext.Provider value={true}>
      <li title={collapsed ? title : undefined}>
        <Link
          className={classNames(active ? "is-active" : "", collapsed ? "has-text-centered" : "")}
          to={to}
          {...createAttributesForTesting(testId)}
        >
          <i className={classNames(defaultIcon, "fa-fw")} /> {collapsed ? "" : label}
        </Link>
        {childrenList}
      </li>
    </SubNavigationContext.Provider>
  );
};

export default SubNavigation;
