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

import React, { ComponentProps, useCallback, useState } from "react";
import * as HeadlessTabs from "@radix-ui/react-tabs";

type Props = Omit<ComponentProps<typeof HeadlessTabs.Root>, "value">;

/**
 * @beta
 * @since 2.47.0
 */
const Tabs = React.forwardRef<HTMLDivElement, Props>(({ defaultValue, onValueChange, children, ...props }, ref) => {
  const [value, setValue] = useState(defaultValue);
  const handleValueChange = useCallback(
    (newValue: string) => {
      setValue(newValue);
      if (onValueChange) {
        onValueChange(newValue);
      }
    },
    [onValueChange]
  );
  return (
    <HeadlessTabs.Root {...props} ref={ref} onValueChange={handleValueChange} value={value}>
      {children}
    </HeadlessTabs.Root>
  );
});

export default Tabs;
