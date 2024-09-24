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
