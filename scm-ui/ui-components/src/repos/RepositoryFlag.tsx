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
import { Card, CardVariant, Tooltip } from "@scm-manager/ui-core";
import { Color, Size } from "../styleConstants";
import { TooltipLocation } from "../Tooltip";

type Props = {
  color?: Color;
  title: string;
  onClick?: () => void;
  size?: Size;
  tooltipLocation: TooltipLocation;
  variant?: CardVariant;
  children?: ReactNode;
};

const RepositoryFlag: FC<Props> = ({
  children,
  title,
  size = "small",
  tooltipLocation = "bottom",
  variant,
  ...props
}) => (
  <Tooltip side={tooltipLocation} message={title}>
    <Card.Details.Detail.Tag {...props} cardVariant={variant} className={`is-${size} is-relative`}>
      {children}
    </Card.Details.Detail.Tag>
  </Tooltip>
);

export default RepositoryFlag;
