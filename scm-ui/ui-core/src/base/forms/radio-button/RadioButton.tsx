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
import classNames from "classnames";
import Help from "../base/help/Help";
import * as RadioGroup from "@radix-ui/react-radio-group";
import { createAttributesForTesting, useAriaId } from "../../helpers";
import styled from "styled-components";
import { useRadioButtonContext } from "./RadioButtonContext";

const StyledRadioButton = styled(RadioGroup.Item)`
  all: unset;
  width: 1rem;
  height: 1rem;
  border: var(--scm-border);
  border-radius: 100%;

  :hover {
    border-color: var(--scm-hover-color);
  }

  :hover *::after {
    background-color: var(--scm-info-hover-color);
  }

  :disabled {
    background-color: var(--scm-dark-color-25);
    border-color: var(--scm-hover-color);
  }

  :disabled *::after {
    background-color: var(--scm-info-color);
  }
`;

const StyledIndicator = styled(RadioGroup.Indicator)`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  position: relative;

  ::after {
    content: "";
    display: block;
    width: 0.5rem;
    height: 0.5rem;
    border-radius: 50%;
    background-color: var(--scm-info-color);
  }
`;

type Props = {
  value: string;
  id?: string;
  testId?: string;
  indicatorClassName?: string;
  label?: string;
  labelClassName?: string;
  helpText?: string;
} & ComponentProps<typeof RadioGroup.Item>;

/**
 * @beta
 * @since 2.48.0
 */
const RadioButton = React.forwardRef<HTMLButtonElement, Props>(
  ({ id, testId, indicatorClassName, label, labelClassName, className, helpText, value, ...props }, ref) => {
    const context = useRadioButtonContext();
    const inputId = useAriaId(id);
    const labelKey = `${context?.prefix}.radio.${value}`;

    return (
      <label className={classNames("radio is-flex is-align-items-center", labelClassName)} htmlFor={inputId}>
        <StyledRadioButton
          form={context?.formId}
          id={inputId}
          value={value}
          ref={ref}
          className={classNames("mr-3 mt-3 mb-3", className)}
          {...props}
          {...createAttributesForTesting(testId)}
        >
          <StyledIndicator className={indicatorClassName} />
        </StyledRadioButton>
        {label ?? context?.t(labelKey) ?? value}
        {helpText ? <Help className="ml-3" text={helpText} /> : null}
      </label>
    );
  }
);

export default RadioButton;
