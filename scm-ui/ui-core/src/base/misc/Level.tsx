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

import React, { HTMLAttributes, ReactNode } from "react";
import classNames from "classnames";

type Props = HTMLAttributes<HTMLDivElement> & {
  left?: ReactNode;
  right?: ReactNode;
};

const Level = React.forwardRef<HTMLDivElement, Props>(({ right, left, children, className, ...props }, ref) => (
  <div className={classNames("level", className)} {...props} ref={ref}>
    <div className="level-left">{left}</div>
    {children ? <div className="level-item">{children}</div> : null}
    <div className="level-right">{right}</div>
  </div>
));

export default Level;
