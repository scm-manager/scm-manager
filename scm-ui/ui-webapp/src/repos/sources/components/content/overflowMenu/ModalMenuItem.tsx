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

import React, { FC, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { Icon } from "@scm-manager/ui-components";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { Menu } from "@scm-manager/ui-overlays";

const ModalMenuItem: FC<
  extensionPoints.ModalMenuProps & {
    setSelectedModal: (element: ReactElement | undefined) => void;
    extensionProps: extensionPoints.ContentActionExtensionProps;
    setLoading?: (isLoading: boolean) => void;
  }
> = ({ modalElement, label, icon, props, extensionProps, setSelectedModal, setLoading }) => {
  const [t] = useTranslation("plugins");

  return (
    <Menu.Button
      onSelect={() =>
        setSelectedModal(
          React.createElement(modalElement, { ...extensionProps, close: () => setSelectedModal(undefined), setLoading })
        )
      }
      {...props}
    >
      <Icon name={icon} className="pr-5 has-text-inherit" />
      <span>{t(label)}</span>
    </Menu.Button>
  );
};

export default ModalMenuItem;
