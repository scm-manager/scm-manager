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
import React, { ChangeEvent, FC, FocusEvent } from "react";
import { Help } from "../index";
import styled from "styled-components";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
import classNames from "classnames";

const StyledRadio = styled.label`
  margin-right: 0.5em;
`;

type BaseProps = {
  label?: string;
  name?: string;
  value?: string;
  checked?: boolean;
  disabled?: boolean;
  helpText?: string;
  defaultChecked?: boolean;
  className?: string;
  readOnly?: boolean;
};

const InnerRadio: FC<FieldProps<BaseProps, HTMLInputElement, boolean>> = ({ name, defaultChecked, readOnly, ...props }) => {
  const renderHelp = () => {
    const helpText = props.helpText;
    if (helpText) {
      return <Help message={helpText} />;
    }
  };

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (props.onChange) {
      if (isUsingRef<BaseProps, HTMLInputElement, boolean>(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(Boolean(event.target.checked), name);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (props.onBlur) {
      if (isUsingRef<BaseProps, HTMLInputElement, boolean>(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(Boolean(event.target.checked), name);
      }
    }
  };

  return (
    <fieldset disabled={readOnly}>
      {/*
        we have to ignore the next line,
        because jsx label does not the custom disabled attribute
        but bulma does.
        // @ts-ignore */}
      <StyledRadio className={classNames("radio", props.className)} disabled={props.disabled}>
        <input
          type="radio"
          name={name}
          value={props.value}
          checked={props.checked}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={props.disabled}
          ref={props.innerRef}
          defaultChecked={defaultChecked}
        />{" "}
        {props.label}
        {renderHelp()}
      </StyledRadio>
    </fieldset>
  );
};

const Radio: FieldType<BaseProps, HTMLInputElement, boolean> = createFormFieldWrapper(InnerRadio);

export default Radio;
