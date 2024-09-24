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
import Control from "../base/Control";
import * as RadixRadio from "@radix-ui/react-radio-group";
import RadioButton from "./RadioButton";

type Props = {
  options?: { value: string; label?: string; helpText?: string }[];
} & ComponentProps<typeof RadixRadio.Root>;

/**
 * @beta
 * @since 2.48.0
 */
const RadioGroup = React.forwardRef<HTMLDivElement, Props>(({ options, children, className, ...props }, ref) => (
  <RadixRadio.Root {...props} asChild>
    <Control ref={ref} className={className}>
      {children ??
        options?.map((option) => (
          <RadioButton key={option.value} value={option.value} label={option.label} helpText={option.helpText} />
        ))}
    </Control>
  </RadixRadio.Root>
));

export default RadioGroup;
