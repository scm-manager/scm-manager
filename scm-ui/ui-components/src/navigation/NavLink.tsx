/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC, useContext, useEffect } from "react";
import classNames from "classnames";
import { Link } from "react-router-dom";
import { useSecondaryNavigation } from "@scm-manager/ui-components";
import { RoutingProps } from "./RoutingProps";
import useActiveMatch from "./useActiveMatch";
import { createAttributesForTesting } from "../devBuild";
import { SecondaryNavigationContext } from "./SecondaryNavigationContext";
import { SubNavigationContext } from "./SubNavigationContext";

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
  const isSecondaryNavigation = useContext(SecondaryNavigationContext);
  const isSubNavigation = useContext(SubNavigationContext);

  useEffect(() => {
    if (isSecondaryNavigation && active) {
      setCollapsible(!isSubNavigation);
    }
  }, [active, isSecondaryNavigation, isSubNavigation, setCollapsible]);

  return (
    <li title={collapsed ? title : undefined}>
      <Link
        className={classNames(active ? "is-active" : "", collapsed ? "has-text-centered" : "")}
        to={to}
        {...createAttributesForTesting(testId)}
      >
        {children ? (
          children
        ) : (
          <NavLinkContent {...contentProps} collapsed={(isSecondaryNavigation && collapsed) ?? false} />
        )}
      </Link>
    </li>
  );
};

NavLink.defaultProps = {
  activeOnlyWhenExact: true,
};

export default NavLink;
