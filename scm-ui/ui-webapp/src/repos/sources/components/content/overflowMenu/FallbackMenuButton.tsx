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
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

const FallbackButton = styled(Button)`
  height: 2.5rem;
  width: 50px;
  margin-bottom: 0.5rem;
  > i {
    padding: 0 !important;
  }
  &:hover {
    color: var(--scm-link-color);
  }
`;

const FallbackLink = styled(Link)`
  width: 50px;
  &:hover {
    color: var(--scm-link-color);
  }
`;

const FallbackMenuButton: FC<{
  extension: extensionPoints.FileViewActionBarOverflowMenu["type"];
  extensionProps: extensionPoints.ContentActionExtensionProps;
  setSelectedModal: (element: ReactElement | undefined) => void;
}> = ({ extension, extensionProps, setSelectedModal }) => {
  const [t] = useTranslation("plugins");
  if ("action" in extension) {
    return (
      <FallbackButton
        icon={extension.icon}
        title={t(extension.label)}
        action={() => extension.action(extensionProps)}
      />
    );
  }
  if ("link" in extension) {
    return (
      <FallbackLink to={extension.link(extensionProps)} className="button" title={t(extension.label)}>
        <Icon name={extension.icon} color="inherit" />
      </FallbackLink>
    );
  }
  if ("modalElement" in extension) {
    return (
      <FallbackButton
        icon={extension.icon}
        title={t(extension.label)}
        action={() =>
          setSelectedModal(
            React.createElement(extension.modalElement, {
              ...extensionProps,
              close: () => setSelectedModal(undefined),
            })
          )
        }
      />
    );
  }
  return null;
};
export default FallbackMenuButton;
