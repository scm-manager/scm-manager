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
import { Card, CardVariant, Tooltip, TooltipSide } from "@scm-manager/ui-core";
import { Color, Size } from "../styleConstants";

type Props = {
  color?: Color;
  title: string;
  onClick?: () => void;
  size?: Size;
  tooltipSide?: TooltipSide;
  variant?: CardVariant;
  children?: ReactNode;
};

const RepositoryFlag: FC<Props> = ({ children, title, size = "small", tooltipSide = "bottom", variant, ...props }) => (
  <Tooltip side={tooltipSide} message={title}>
    <Card.Details.Detail.Tag {...props} cardVariant={variant} className={`is-${size} is-relative`}>
      {children}
    </Card.Details.Detail.Tag>
  </Tooltip>
);

export default RepositoryFlag;
