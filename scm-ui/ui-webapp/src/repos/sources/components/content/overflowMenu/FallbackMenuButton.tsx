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
import { Icon, IconButton, IconLinkButton } from "@scm-manager/ui-core";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

const FallbackMenuButton: FC<{
  extension: extensionPoints.FileViewActionBarOverflowMenu["type"];
  extensionProps: extensionPoints.ContentActionExtensionProps;
  setSelectedModal: (element: ReactElement | undefined) => void;
}> = ({ extension, extensionProps, setSelectedModal }) => {
  const [t] = useTranslation("plugins");
  if ("action" in extension) {
    return (
      <IconButton
        title={t(extension.label)}
        aria-label={t(extension.label)}
        onClick={() => extension.action(extensionProps)}
      >
        <Icon>{extension.icon}</Icon>
      </IconButton>
    );
  }
  if ("link" in extension) {
    return (
      <IconLinkButton to={extension.link(extensionProps)} title={t(extension.label)} aria-label={t(extension.label)}>
        <Icon>{extension.icon}</Icon>
      </IconLinkButton>
    );
  }
  if ("modalElement" in extension) {
    return (
      <IconButton
        title={t(extension.label)}
        aria-label={t(extension.label)}
        onClick={() =>
          setSelectedModal(
            React.createElement(extension.modalElement, {
              ...extensionProps,
              close: () => setSelectedModal(undefined),
            })
          )
        }
      >
        <Icon>{extension.icon}</Icon>
      </IconButton>
    );
  }
  return null;
};
export default FallbackMenuButton;
