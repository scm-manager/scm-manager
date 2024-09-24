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

import React, { ChangeEvent, FC, FocusEvent, useMemo } from "react";
import classNames from "classnames";
import { Help } from "../index";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
import { createA11yId } from "../createA11yId";

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
  ariaLabelledby?: string;
};

const InnerRadio: FC<FieldProps<BaseProps, HTMLInputElement, boolean>> = ({
  name,
  defaultChecked,
  readOnly,
  ariaLabelledby,
  ...props
}) => {
  const id = useMemo(() => ariaLabelledby || createA11yId("radio"), [ariaLabelledby]);
  const helpId = useMemo(() => createA11yId("radio"), []);

  const renderHelp = () => {
    const helpText = props.helpText;
    if (helpText) {
      return <Help message={helpText} id={helpId} />;
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

  const labelElement = props.label ? <span id={id}>{props.label}</span> : null;

  return (
    <fieldset className="is-inline-block" disabled={readOnly}>
      {/*
        we have to ignore the next line,
        because jsx label does not the custom disabled attribute
        but bulma does.
        // @ts-ignore */}
      <label className={classNames("radio", "mr-2", props.className)} disabled={props.disabled}>
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
          aria-labelledby={id}
          aria-describedby={props.helpText ? helpId : undefined}
        />{" "}
        {labelElement}
        {renderHelp()}
      </label>
    </fieldset>
  );
};

/**
 * @deprecated
 */
const Radio: FieldType<BaseProps, HTMLInputElement, boolean> = createFormFieldWrapper(InnerRadio);

export default Radio;
