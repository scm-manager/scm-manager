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

import React, {FC, ReactElement, ReactNode, useEffect, useMemo} from "react";
import styled from "styled-components";
import SubNavigation from "./SubNavigation";
import { matchPath, useLocation } from "react-router-dom";
import useMenuContext from "./MenuContext";
import { ExtensionPoint, Binder, useBinder } from "@scm-manager/ui-extensions";
import { RoutingProps } from "./RoutingProps";

type Props = {
  label: string;
};

type CollapsedProps = {
  collapsed: boolean;
};

const SectionContainer = styled.aside`
  position: sticky;
  position: -webkit-sticky; /* Safari */
  top: 2rem;
`;

const Icon = styled.i<CollapsedProps>`
  padding-left: ${(props: CollapsedProps) => (props.collapsed ? "0" : "0.5rem")};
  padding-right: ${(props: CollapsedProps) => (props.collapsed ? "0" : "0.4rem")};
  height: 1.5rem;
  font-size: 24px;
  margin-top: -0.75rem;
`;

const MenuLabel = styled.p<CollapsedProps>`
  height: 3.2rem;
  display: flex;
  align-items: center;
  justify-content: ${(props: CollapsedProps) => (props.collapsed ? "center" : "inherit")};
  cursor: pointer;
`;

const SecondaryNavigation: FC<Props> = ({ label, children}) => {
  const location = useLocation();
  const binder = useBinder();
  const menuContext = useMenuContext();
  const subNavActive = isSubNavigationActive(binder, children, location.pathname);
  const isCollapsed = menuContext.isCollapsed();

  useEffect(() => {
    if (isCollapsed && subNavActive) {
      menuContext.setCollapsed(false);
    }
  }, [subNavActive, isCollapsed]);

  const toggleCollapseState = () => {
    if (!subNavActive) {
      menuContext.setCollapsed(!isCollapsed);
    }
  };

  const uncollapseMenu = () => {
    if (isCollapsed) {
      menuContext.setCollapsed(false);
    }
  };

  const arrowIcon = isCollapsed ? <i className="fas fa-caret-down" /> : <i className="fas fa-caret-right" />;

  return (
    <SectionContainer className="menu">
      <div>
        <MenuLabel
          className="menu-label"
          collapsed={isCollapsed}
          onClick={toggleCollapseState}
        >
          {!subNavActive && (
            <Icon color="info" className="is-medium" collapsed={isCollapsed}>
              {arrowIcon}
            </Icon>
          )}
          {isCollapsed ? "" : label}
        </MenuLabel>
        <ul className="menu-list" onClick={uncollapseMenu}>{children}</ul>
      </div>
    </SectionContainer>
  );
};

const createParentPath = (to: string) => {
  const parents = to.split("/");
  parents.splice(-1, 1);
  return parents.join("/");
};

const expandExtensionPoints = (binder: Binder, child: ReactElement): Array<ReactElement> => {
  // @ts-ignore
  if (child.type.name === ExtensionPoint.name) {
    // @ts-ignore
    return binder.getExtensions(child.props.name, child.props.props);
  }
  return [child];
};

const mapToProps = (child: ReactElement<RoutingProps>) => {
  return child.props;
};

const isSubNavigation = (child: ReactElement) => {
  // @ts-ignore
  return child.type.name === SubNavigation.name;
};

const isActive = (url: string, props: RoutingProps) => {
  const path = createParentPath(props.to);
  const matches = matchPath(url, {
    path,
    exact: props.activeOnlyWhenExact
  });
  return matches != null;
};

const isSubNavigationActive = (binder: Binder, children: ReactNode, url: string): boolean => {
  const childArray = React.Children.toArray(children);

  const match = childArray
    .filter(React.isValidElement)
    .flatMap(child => expandExtensionPoints(binder, child))
    .filter(isSubNavigation)
    .map(mapToProps)
    .find(props => isActive(url, props));

  return match != null;
};

export default SecondaryNavigation;
