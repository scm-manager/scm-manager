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
import React, { FC, ReactElement, ReactNode, useContext, useEffect } from "react";
import styled from "styled-components";
import SubNavigation from "./SubNavigation";
import { matchPath, useLocation } from "react-router-dom";
import { isMenuCollapsed, MenuContext } from "./MenuContext";

type Props = {
  label: string;
  children: ReactElement[];
  collapsed: boolean;
  onCollapse?: (newStatus: boolean) => void;
};

type CollapsedProps = {
  collapsed: boolean;
};

const SectionContainer = styled.aside<CollapsedProps>`
  position: sticky;
  position: -webkit-sticky; /* Safari */
  top: 2rem;
  width: ${props => (props.collapsed ? "5.5rem" : "20.5rem")};
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

const SecondaryNavigation: FC<Props> = ({ label, children, collapsed, onCollapse }) => {
  const location = useLocation();
  const menuContext = useContext(MenuContext);

  const subNavActive = isSubNavigationActive(children, location.pathname);
  const isCollapsed = collapsed && !subNavActive;

  useEffect(() => {
    if (isMenuCollapsed()) {
      menuContext.setMenuCollapsed(!subNavActive);
    }
  }, [subNavActive]);

  const childrenWithProps = React.Children.map(children, (child: ReactElement) =>
    React.cloneElement(child, { collapsed: isCollapsed })
  );
  const arrowIcon = isCollapsed ? <i className="fas fa-caret-down" /> : <i className="fas fa-caret-right" />;

  return (
    <SectionContainer className="menu" collapsed={isCollapsed}>
      <div>
        <MenuLabel
          className="menu-label"
          collapsed={isCollapsed}
          onClick={onCollapse && !subNavActive ? () => onCollapse(!isCollapsed) : undefined}
        >
          {onCollapse && !subNavActive && (
            <Icon color="info" className="is-medium" collapsed={isCollapsed}>
              {arrowIcon}
            </Icon>
          )}
          {isCollapsed ? "" : label}
        </MenuLabel>
        <ul className="menu-list">{childrenWithProps}</ul>
      </div>
    </SectionContainer>
  );
};

const createParentPath = (to: string) => {
  const parents = to.split("/");
  parents.splice(-1, 1);
  return parents.join("/");
};

const isSubNavigationActive = (children: ReactNode, url: string): boolean => {
  const childArray = React.Children.toArray(children);
  const match = childArray
    .filter(child => {
      // what about extension points?
      // @ts-ignore
      return child.type.name === SubNavigation.name;
    })
    .map(child => {
      // @ts-ignore
      return child.props;
    })
    .find(props => {
      const path = createParentPath(props.to);
      const matches = matchPath(url, {
        path,
        exact: props.activeOnlyWhenExact as boolean
      });
      return matches != null;
    });

  return match != null;
};

export default SecondaryNavigation;
