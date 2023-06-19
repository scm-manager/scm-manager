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

import Field from "../base/Field";
import Label from "../base/label/Label";
import Help from "../base/help/Help";
import React from "react";
import { useGeneratedId } from "@scm-manager/ui-components";
import { withForwardRef } from "../helpers";
import Combobox, { ComboboxProps } from "./Combobox";
import classNames from "classnames";

/**
 * @beta
 * @since 2.45.0
 */
const ComboboxField = function ComboboxField<T>(
  {
    label,
    helpText,
    error,
    className,
    isLoading,
    ...props
  }: ComboboxProps<T> & { label: string; helpText?: string; error?: string; isLoading?: boolean },
  ref: React.ForwardedRef<HTMLInputElement>
) {
  const labelId = useGeneratedId();
  return (
    <Field className={className}>
      <Label id={labelId}>
        {label}
        {helpText ? <Help className="ml-1" text={helpText} /> : null}
      </Label>
      <div className={classNames("control", { "is-loading": isLoading })}>
        <Combobox {...props} ref={ref} aria-labelledby={labelId} />
      </div>
    </Field>
  );
};
export default withForwardRef(ComboboxField);
