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

import React, { FC, ReactNode } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Button } from "@scm-manager/ui-core";
import { useSecondaryNavigation } from "../useSecondaryNavigation";

type Props = {
  label: string;
  collapsible?: boolean;
  children?: ReactNode;
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
  color: #e2e2e2;
`;

const SecondaryNavigation: FC<Props> = ({ label, children }) => {
  const [t] = useTranslation("commons");
  const { collapsible: isCollapsible, collapsed, toggleCollapse } = useSecondaryNavigation();

  const arrowIcon = collapsed ? <i className="fas fa-caret-left" /> : <i className="fas fa-caret-down" />;
  const menuAriaLabel = collapsed ? t("secondaryNavigation.showContent") : t("secondaryNavigation.hideContent");

  const MenuButton = styled(Button)`
    justify-content: ${collapsed ? "center" : "inherit"};
    width: 100%;
    color: #e2e2e2;
    &:hover {
      color: #e2e2e2;
    }
    &:focus {
      color: #e2e2e2;
    }
  `;

  return (
    <SectionContainer className="menu" collapsed={collapsed}>
      <div>
        <MenuButton className="menu-label" onClick={toggleCollapse} aria-label={menuAriaLabel}>
          {isCollapsible ? (
            <Icon className="is-medium" collapsed={collapsed}>
              {arrowIcon}
            </Icon>
          ) : null}
          {collapsed ? "" : label}
        </MenuButton>
        <ul className="menu-list">{children}</ul>
      </div>
    </SectionContainer>
  );
};

export default SecondaryNavigation;
