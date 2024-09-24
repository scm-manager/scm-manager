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

import React, { HTMLAttributes } from "react";
import classNames from "classnames";

const Subtitle = React.forwardRef<HTMLHeadingElement, HTMLAttributes<HTMLHeadingElement> & { subtitle?: string }>(
  ({ subtitle, className, children, ...props }, ref) =>
    !(subtitle || children) ? null : (
      <h2 className={classNames("subtitle", className)} {...props} ref={ref}>
        {subtitle}
        {children}
      </h2>
    )
);

export default Subtitle;
