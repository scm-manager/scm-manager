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
      <div className={classNames("radio is-flex is-align-items-center", labelClassName)}>
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
        <label htmlFor={inputId}>{label ?? context?.t(labelKey) ?? value}</label>
        {helpText ? <Help className="ml-3" text={helpText} /> : null}
      </div>
    );
  }
);

export default RadioButton;
