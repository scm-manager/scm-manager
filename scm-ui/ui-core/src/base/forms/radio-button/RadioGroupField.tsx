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

/**
 * @beta
 * @since 2.48.0
 */
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
