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

import React, { FC } from "react";
import styled from "styled-components";
import useMenuContext from "./MenuContext";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

type Props = {
  label: string;
  collapsible?: boolean;
};

type CollapsedProps = {
  collapsed: boolean;
};

const SectionContainer = styled.aside`
  position: sticky;
  position: -webkit-sticky; /* Safari */
  top: 2rem;
  width: 100%;

  @media (max-height: 900px) {
    position: relative;
    top: 0;
  }
`;

const Icon = styled.i<CollapsedProps>`
  padding-left: ${(props: CollapsedProps) => (props.collapsed ? "0" : "0.5rem")};
  padding-right: ${(props: CollapsedProps) => (props.collapsed ? "0" : "0.4rem")};
  height: 1.5rem;
  font-size: 24px;
  margin-top: -0.75rem;
`;

const MenuLabel = styled.p<CollapsedProps>`
  justify-content: ${(props: CollapsedProps) => (props.collapsed ? "center" : "inherit")};
`;

const SecondaryNavigation: FC<Props> = ({ label, children, collapsible = true }) => {
  const [t] = useTranslation("commons");
  const menuContext = useMenuContext();
  const isCollapsed = collapsible && menuContext.isCollapsed();

  const toggleCollapseState = () => {
    if (collapsible) {
      menuContext.setCollapsed(!isCollapsed);
    }
  };

  const uncollapseMenu = () => {
    if (collapsible && isCollapsed) {
      menuContext.setCollapsed(false);
    }
  };

  const arrowIcon = isCollapsed ? <i className="fas fa-caret-left" /> : <i className="fas fa-caret-down" />;
  const menuAriaLabel = isCollapsed ? t("secondaryNavigation.showContent") : t("secondaryNavigation.hideContent");

  return (
    <SectionContainer className="menu">
      <div>
        <MenuLabel
          className={classNames("menu-label", { "is-clickable": collapsible })}
          collapsed={isCollapsed}
          onClick={toggleCollapseState}
          aria-label={menuAriaLabel}
        >
          {collapsible ? (
            <Icon className="is-medium" collapsed={isCollapsed}>
              {arrowIcon}
            </Icon>
          ) : null}
          {isCollapsed ? "" : label}
        </MenuLabel>
        <ul className="menu-list" onClick={uncollapseMenu}>
          {children}
        </ul>
      </div>
    </SectionContainer>
  );
};

export default SecondaryNavigation;
