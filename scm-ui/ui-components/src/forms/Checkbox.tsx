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
import React, { ChangeEvent, FC, FocusEvent, useEffect } from "react";
import { createAttributesForTesting, Help } from "../index";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import useInnerRef from "./useInnerRef";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
import classNames from "classnames";
import { createA11yId } from "../createA11yId";
import styled from "styled-components";

const StyledInput = styled.input`
  height: 1rem;
  width: 1rem;
`;

const StyledLabel = styled.label`
  margin-left: -0.75rem;
  display: inline-flex;
`;

export interface CheckboxElement extends HTMLElement {
  value: boolean;
}

type BaseProps = {
  label?: string;
  checked?: boolean;
  indeterminate?: boolean;
  name?: string;
  title?: string;
  disabled?: boolean;
  helpText?: string;
  testId?: string;
  className?: string;
  readOnly?: boolean;
};

const InnerCheckbox: FC<FieldProps<BaseProps, HTMLInputElement, boolean>> = ({
  label,
  name,
  indeterminate,
  disabled,
  testId,
  className,
  readOnly,
  ...props
}) => {
  const field = useInnerRef(props.innerRef);

  useEffect(() => {
    if (field.current) {
      field.current.indeterminate = indeterminate || false;
    }
  }, [field, indeterminate]);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (props.onChange) {
      if (isUsingRef<BaseProps, HTMLInputElement, boolean>(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(event.target.checked, name);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (props.onBlur) {
      if (isUsingRef<BaseProps, HTMLInputElement, boolean>(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(event.target.checked, name);
      }
    }
  };

  const id = createA11yId("checkbox");
  const helpId = createA11yId("checkbox");

  const renderHelp = () => {
    const { title, helpText } = props;
    if (helpText && !title) {
      return <Help message={helpText} id={helpId} />;
    }
  };

  const renderLabelWithHelp = () => {
    const { title, helpText } = props;
    if (title) {
      return <LabelWithHelpIcon label={title} helpText={helpText} id={id} helpId={helpId} />;
    }
  };
  return (
    <fieldset className="field" disabled={readOnly}>
      {renderLabelWithHelp()}
      <div className="control">
        {/*
            we have to ignore the next line,
            because jsx label does not the custom disabled attribute
            but bulma does.
            // @ts-ignore */}
        <StyledLabel className="checkbox is-align-items-center" disabled={disabled}>
          <StyledInput
            type="checkbox"
            name={name}
            className={classNames("m-3", className)}
            onChange={handleChange}
            onBlur={handleBlur}
            ref={field}
            checked={props.checked}
            disabled={disabled}
            readOnly={readOnly}
            aria-labelledby={id}
            aria-describedby={helpId}
            {...createAttributesForTesting(testId)}
          />{" "}
          {label}
          {renderHelp()}
        </StyledLabel>
      </div>
    </fieldset>
  );
};

/**
 * @deprecated
 */
const Checkbox: FieldType<BaseProps, HTMLInputElement, boolean> = createFormFieldWrapper(InnerCheckbox);

export default Checkbox;
