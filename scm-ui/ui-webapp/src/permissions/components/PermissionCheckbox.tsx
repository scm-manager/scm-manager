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
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-components";

type Props = {
  name: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled: boolean;
  role?: boolean;
};

type InnerProps = Props & {
  innerRef: React.Ref<HTMLInputElement>;
};

const PermissionCheckbox: FC<InnerProps> = ({ name, checked, onChange, disabled, role, innerRef }) => {
  const [t] = useTranslation("plugins");
  const key = name.split(":").join(".");

  const translateOrDefault = (key: string, defaultText: string) => {
    const translation = t(key);
    if (translation === key) {
      return defaultText;
    } else {
      return translation;
    }
  };

  const label = role
    ? t("verbs.repository." + name + ".displayName")
    : translateOrDefault("permissions." + key + ".displayName", key);
  const helpText = role
    ? t("verbs.repository." + name + ".description")
    : translateOrDefault("permissions." + key + ".description", t("permissions.unknown"));

  const commonCheckboxProps = {
    key: name,
    name,
    label,
    helpText,
    checked,
    disabled,
    testId: label,
  };

  if (innerRef) {
    return (
      <Checkbox
        {...commonCheckboxProps}
        onChange={onChange ? (event) => onChange(event.target.checked, name) : undefined}
        ref={innerRef}
      />
    );
  }

  return <Checkbox {...commonCheckboxProps} onChange={onChange ? (newValue) => onChange(newValue, name) : undefined} />;
};

export default React.forwardRef<HTMLInputElement, Props>((props, ref) => (
  <PermissionCheckbox {...props} innerRef={ref} />
));
