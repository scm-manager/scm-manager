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

import React, { FC, useContext, useEffect } from "react";
import classNames from "classnames";
import { Link } from "react-router-dom";
import { createAttributesForTesting } from "@scm-manager/ui-core";
import { useSecondaryNavigation } from "../useSecondaryNavigation";
import { RoutingProps } from "./RoutingProps";
import { SubNavigationContext } from "./SubNavigationContext";
import useActiveMatch from "./useActiveMatch";

type Props = RoutingProps & {
  label: string;
  title?: string;
  icon?: string;
  testId?: string;
};

type NavLinkContentProp = {
  label: string;
  icon?: string;
  collapsed: boolean;
};

const NavLinkContent: FC<NavLinkContentProp> = ({ label, icon, collapsed }) => (
  <>
    {icon ? (
      <>
        <i className={classNames(icon, "fa-fw")} />{" "}
      </>
    ) : null}
    {collapsed ? null : label}
  </>
);

const NavLink: FC<Props> = ({ to, activeWhenMatch, activeOnlyWhenExact, title, testId, children, ...contentProps }) => {
  const active = useActiveMatch({ to, activeWhenMatch, activeOnlyWhenExact });
  const { collapsed, setCollapsible } = useSecondaryNavigation();
  const isSubNavigation = useContext(SubNavigationContext);

  useEffect(() => {
    if (active) {
      setCollapsible(!isSubNavigation);
    }
  }, [active, isSubNavigation, setCollapsible]);

  return (
    <li title={collapsed ? title : undefined}>
      <Link
        className={classNames(active ? "is-active" : "", collapsed ? "has-text-centered" : "")}
        to={to}
        {...createAttributesForTesting(testId)}
        aria-label={collapsed ? title : undefined}
        {...(active ? { "aria-current": "page" } : {})}
      >
        {children || <NavLinkContent {...contentProps} collapsed={collapsed} />}
      </Link>
    </li>
  );
};

NavLink.defaultProps = {
  activeOnlyWhenExact: true,
};

export default NavLink;
