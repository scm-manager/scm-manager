/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, { ComponentProps } from "react";
import Control from "../base/Control";
import * as RadixRadio from "@radix-ui/react-radio-group";
import RadioButton from "./RadioButton";

type Props = {
  options?: { value: string; label?: string; helpText?: string }[];
} & ComponentProps<typeof RadixRadio.Root>;

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
