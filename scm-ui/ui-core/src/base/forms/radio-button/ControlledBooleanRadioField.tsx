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
import { Controller, Path } from "react-hook-form";
import classNames from "classnames";
import { useScmFormContext } from "../ScmFormContext";
import { useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";
import Field from "../base/Field";
import Label from "../base/label/Label";
import RadioGroup from "./RadioGroup";
import RadioButton from "./RadioButton";

type Props<T extends Record<string, unknown>> = {
  name: Path<T>;
  label?: string;
  description?: string;
  trueLabel?: string;
  falseLabel?: string;
  rules?: ComponentProps<typeof Controller>["rules"];
  readOnly?: boolean;
  fieldClassName?: string;
};

/**
 * Controlled radio field for a boolean value: renders a "true"/"false" radio pair and maps the
 * selection to and from a boolean. When not passed explicitly, the labels fall back to the
 * translations `<name>.label`, `<name>.true` and `<name>.false`.
 *
 * @beta
 * @since 4.0.0
 */
function ControlledBooleanRadioField<T extends Record<string, unknown>>({
  name,
  label,
  description,
  trueLabel,
  falseLabel,
  rules,
  readOnly,
  fieldClassName,
}: Props<T>) {
  const { control, t, readOnly: formReadOnly } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();
  const nameWithPrefix = formPathPrefix ? `${formPathPrefix}.${name}` : name;
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const labelTranslation = label ?? t(`${prefixedNameWithoutIndices}.label`) ?? "";
  const trueTranslation = trueLabel ?? t(`${prefixedNameWithoutIndices}.true`) ?? "";
  const falseTranslation = falseLabel ?? t(`${prefixedNameWithoutIndices}.false`) ?? "";
  const disabled = readOnly ?? formReadOnly;

  return (
    <Controller
      control={control}
      name={nameWithPrefix}
      rules={rules}
      render={({ field }) => (
        /* pt-0 neutralises the column's padding-top: on a fieldset Chromium renders that padding *below*
           the legend, which would push the description ~12px down instead of the 8px the InputField's label
           margin produces. With pt-0 the gap comes solely from the legend's own `.label` margin-bottom (0.5em),
           matching the InputField. */
        <Field className={classNames("column pt-0", fieldClassName)} as="fieldset">
          <Label as="legend">{labelTranslation}</Label>
          {description ? <p className="mb-2 is-size-7">{description}</p> : null}
          <RadioGroup
            name={nameWithPrefix}
            disabled={disabled}
            value={field.value ? "true" : "false"}
            onValueChange={(value: string) => field.onChange(value === "true")}
          >
            <RadioButton value="true" label={trueTranslation} />
            <RadioButton value="false" label={falseTranslation} />
          </RadioGroup>
        </Field>
      )}
    />
  );
}

export default ControlledBooleanRadioField;
