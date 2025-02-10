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
  descriptionText?: string;
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
      descriptionText,
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
  ) => {
    const descriptionId = descriptionText ? `checkbox-description-${name}` : undefined;
    return (
      <>
        {descriptionText ? <p id={descriptionId}>{descriptionText}</p> : null}
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
                aria-describedby={descriptionId}
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
                aria-describedby={descriptionId}
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
              aria-describedby={descriptionId}
              {...props}
              {...createAttributesForTesting(testId)}
            />
          )}

          {label}
          {helpText ? <Help className="ml-1" text={helpText} /> : null}
        </StyledLabel>
      </>
    );
  }
);
export default Checkbox;
