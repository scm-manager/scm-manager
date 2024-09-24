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
import { extensionPoints } from "@scm-manager/ui-extensions";
import { Menu } from "@scm-manager/ui-overlays";

const ActionMenuItem: FC<
  extensionPoints.ActionMenuProps & {
    extensionProps: extensionPoints.ContentActionExtensionProps;
  }
> = ({ action, label, props, icon, extensionProps }) => {
  const [t] = useTranslation("plugins");

  return (
    <Menu.Button onSelect={() => action(extensionProps)} {...props}>
      <Icon name={icon} className="pr-5 has-text-inherit" />
      {t(label)}
    </Menu.Button>
  );
};

export default ActionMenuItem;
