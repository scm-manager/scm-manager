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

import React, { ComponentProps } from "react";
import classNames from "classnames";
import * as HeadlessTabs from "@radix-ui/react-tabs";

type Props = ComponentProps<typeof HeadlessTabs.Trigger>;

/**
 * @beta
 * @since 2.47.0
 */
const TabTrigger = React.forwardRef<HTMLButtonElement, Props>(({ children, className, ...props }, ref) => (
  <li>
    <HeadlessTabs.Trigger
      className={classNames("has-background-transparent is-borderless", className)}
      ref={ref}
      {...props}
    >
      {children}
    </HeadlessTabs.Trigger>
  </li>
));
export default TabTrigger;
