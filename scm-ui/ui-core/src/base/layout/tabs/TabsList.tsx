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
import * as RadixTabs from "@radix-ui/react-tabs";
import classNames from "classnames";

/**
 * @beta
 * @since 2.47.0
 */
const TabsList = React.forwardRef<HTMLUListElement, HTMLAttributes<HTMLUListElement>>(({ children, ...props }, ref) => (
  <RadixTabs.List className={classNames("tabs", /* required for focus-outline */ "p-1")}>
    <ul {...props} ref={ref}>
      {children}
    </ul>
  </RadixTabs.List>
));

export default TabsList;
