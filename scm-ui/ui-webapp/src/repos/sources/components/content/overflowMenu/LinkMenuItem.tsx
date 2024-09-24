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
import { useTranslation } from "react-i18next";
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { Menu } from "@scm-manager/ui-overlays";

const MenuItemLinkContainer = styled(Link)<{ active: boolean }>`
  border-radius: 5px;
  padding: 0.5rem;
  color: ${(props) => (props.active ? "var(--scm-white-color)" : "inherit")};
  :hover {
    color: var(--scm-white-color);
  }
`;

const LinkMenuItem: FC<
  extensionPoints.LinkMenuProps & { extensionProps: extensionPoints.ContentActionExtensionProps }
> = ({ link, label, props, icon, extensionProps }) => {
  const [t] = useTranslation("plugins");

  return (
    <Menu.Link to={link(extensionProps)} {...props}>
      <Icon name={icon} className="pr-5 has-text-inherit" />
      <span>{t(label)}</span>
    </Menu.Link>
  );
};

export default LinkMenuItem;
