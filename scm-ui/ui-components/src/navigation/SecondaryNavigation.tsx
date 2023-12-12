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
import classNames from "classnames";
import { useTranslation } from "react-i18next";
import { useSecondaryNavigation } from "@scm-manager/ui-components";
import { SecondaryNavigationContext } from "./SecondaryNavigationContext";

type Props = {
  label: string;
  collapsible?: boolean;
};

type CollapsedProps = {
  collapsed: boolean;
};

const SectionContainer = styled.aside<{ collapsed: boolean }>`
  flex: 0 0 auto;
  position: sticky;
  position: -webkit-sticky; /* Safari */
  top: 5rem;
  min-width: ${(props: { collapsed: boolean }) => (props.collapsed ? "0" : "200px")};

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
  const { collapsible: isCollapsible, collapsed, toggleCollapse } = useSecondaryNavigation(collapsible);

  const arrowIcon = collapsed ? <i className="fas fa-caret-left" /> : <i className="fas fa-caret-down" />;
  const menuAriaLabel = collapsed ? t("secondaryNavigation.showContent") : t("secondaryNavigation.hideContent");

  return (
    <SectionContainer className="menu" collapsed={collapsed ?? false}>
      <div>
        <MenuLabel
          className={classNames("menu-label", { "is-clickable": isCollapsible })}
          collapsed={collapsed}
          onClick={toggleCollapse}
          aria-label={menuAriaLabel}
        >
          {isCollapsible ? (
            <Icon className="is-medium" collapsed={collapsed}>
              {arrowIcon}
            </Icon>
          ) : null}
          {collapsed ? "" : label}
        </MenuLabel>
        <ul className="menu-list">
          <SecondaryNavigationContext.Provider value={true}>{children}</SecondaryNavigationContext.Provider>
        </ul>
      </div>
    </SectionContainer>
  );
};

export default SecondaryNavigation;
