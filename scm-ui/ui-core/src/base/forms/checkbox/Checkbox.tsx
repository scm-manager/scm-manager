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

import React, { InputHTMLAttributes } from "react";
import { createAttributesForTesting } from "../../helpers";
import Help from "../base/help/Help";
import styled from "styled-components";
import classNames from "classnames";

const StyledInput = styled.input`
  height: 1rem;
  width: 1rem;
`;

const StyledLabel = styled.label`
  margin-left: -0.75rem;
  display: inline-flex;
`;

type InputFieldProps = {
  label: string;
  helpText?: string;
  testId?: string;
  labelClassName?: string;
} & Omit<InputHTMLAttributes<HTMLInputElement>, "type">;

/**
 * @see https://bulma.io/documentation/form/checkbox/
 */
const Checkbox = React.forwardRef<HTMLInputElement, InputFieldProps>(
  (
    {
      readOnly,
      label,
      className,
      labelClassName,
      value,
      name,
      checked,
      defaultChecked,
      defaultValue,
      testId,
      helpText,
      ...props
    },
    ref
  ) => (
    <StyledLabel
      className={classNames("checkbox is-align-items-center", labelClassName)}
      // @ts-ignore bulma uses the disabled attribute on labels, although it is not part of the html spec
      disabled={readOnly || props.disabled}
    >
      {readOnly ? (
        <>
          <input
            type="hidden"
            name={name}
            value={value}
            defaultValue={defaultValue}
            checked={checked}
            defaultChecked={defaultChecked}
            readOnly
          />
          <StyledInput
            type="checkbox"
            className={classNames("m-3", className)}
            ref={ref}
            value={value}
            defaultValue={defaultValue}
            checked={checked}
            defaultChecked={defaultChecked}
            {...props}
            {...createAttributesForTesting(testId)}
            disabled
          />
        </>
      ) : (
        <StyledInput
          type="checkbox"
          className={classNames("m-3", className)}
          ref={ref}
          name={name}
          value={value}
          defaultValue={defaultValue}
          checked={checked}
          defaultChecked={defaultChecked}
          {...props}
          {...createAttributesForTesting(testId)}
        />
      )}

      {label}
      {helpText ? <Help className="ml-1" text={helpText} /> : null}
    </StyledLabel>
  )
);
export default Checkbox;
