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
import classNames from "classnames";
import { createAttributesForTesting } from "./devBuild";

type Props = {
  title?: string;
  iconStyle?: string;
  name: string;
  color?: string;
  className?: string;
  onClick?: (event: React.MouseEvent) => void;
  onEnter?: (event: React.KeyboardEvent) => void;
  testId?: string;
  tabIndex?: number;
  alt?: string;
};

/**
 * @deprecated
 */
const Icon: FC<Props> = ({
  iconStyle = "fas",
  color = "secondary",
  title,
  name,
  className,
  onClick,
  testId,
  tabIndex,
  onEnter,
  alt = title,
}) => {
  return (
    <i
      onClick={onClick}
      onKeyPress={(event) => event.key === "Enter" && onEnter && onEnter(event)}
      title={title}
      className={classNames(iconStyle, "fa-fw", "fa-" + name, `has-text-${color}`, className)}
      tabIndex={tabIndex}
      aria-label={alt}
      {...createAttributesForTesting(testId)}
    />
  );
};

export default Icon;
