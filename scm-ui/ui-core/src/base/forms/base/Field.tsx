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

import React, { FC, HTMLProps } from "react";
import classNames from "classnames";

const Field: FC<HTMLProps<HTMLDivElement> | ({ as: keyof JSX.IntrinsicElements } & HTMLProps<HTMLElement>)> = ({
  as = "div",
  className,
  children,
  ...rest
}) => React.createElement(as, { className: classNames("field", className), ...rest }, children);

export default Field;
