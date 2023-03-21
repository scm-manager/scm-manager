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

import React from "react";
import Field from "../base/Field";
import Control from "../base/Control";
import Label from "../base/label/Label";
import FieldMessage from "../base/field-message/FieldMessage";
import Input from "./Input";
import Help from "../base/help/Help";
import { useGeneratedId } from "@scm-manager/ui-components";

type InputFieldProps = {
  label: string;
  helpText?: string;
  error?: string;
  type?: "text" | "password" | "email" | "tel";
} & Omit<React.ComponentProps<typeof Input>, "type">;

/**
 * @see https://bulma.io/documentation/form/input/
 */
const InputField = React.forwardRef<HTMLInputElement, InputFieldProps>(
  ({ label, helpText, error, className, id, ...props }, ref) => {
    const inputId = useGeneratedId(id ?? props.testId);
    const variant = error ? "danger" : undefined;
    return (
      <Field className={className}>
        <Label htmlFor={inputId}>
          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </Label>
        <Control>
          <Input variant={variant} ref={ref} id={inputId} {...props}></Input>
        </Control>
        {error ? <FieldMessage variant={variant}>{error}</FieldMessage> : null}
      </Field>
    );
  }
);
export default InputField;
