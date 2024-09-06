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
import ActionMenuItem from "./ActionMenuItem";
import LinkMenuItem from "./LinkMenuItem";
import ModalMenuItem from "./ModalMenuItem";
import { extensionPoints } from "@scm-manager/ui-extensions";

const MenuItem: FC<
  extensionPoints.FileViewActionBarOverflowMenu["type"] & {
    setSelectedModal: (element: ReactElement | undefined) => void;
    extensionProps: extensionPoints.ContentActionExtensionProps;
    setLoading?: (isLoading: boolean) => void;
  }
> = ({ extensionProps, label, icon, props, category, setSelectedModal, setLoading, ...rest }) => {
  if ("action" in rest) {
    return (
      <ActionMenuItem
        label={label}
        icon={icon}
        category={category}
        extensionProps={extensionProps}
        props={props}
        {...rest}
      />
    );
  }
  if ("link" in rest) {
    return (
      <LinkMenuItem
        category={category}
        label={label}
        icon={icon}
        extensionProps={extensionProps}
        props={props}
        {...rest}
      />
    );
  }
  if ("modalElement" in rest) {
    return (
      <ModalMenuItem
        category={category}
        label={label}
        icon={icon}
        extensionProps={extensionProps}
        setSelectedModal={setSelectedModal}
        setLoading={setLoading}
        props={props}
        {...rest}
      />
    );
  }
  return null;
};

export default MenuItem;
