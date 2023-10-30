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
import Field from "../base/Field";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import RadioGroup from "./RadioGroup";

type Props = {
  fieldClassName?: string;
  labelClassName?: string;
  label: string;
  helpText?: string;
} & ComponentProps<typeof RadioGroup>;

const RadioGroupField = React.forwardRef<HTMLDivElement, Props>(
  ({ fieldClassName, labelClassName, label, helpText, children, ...props }, ref) => {
    return (
      <Field className={fieldClassName} as="fieldset">
        <Label className={labelClassName} as="legend">
          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </Label>
        <RadioGroup ref={ref} {...props}>
          {children}
        </RadioGroup>
      </Field>
    );
  }
);

export default RadioGroupField;
