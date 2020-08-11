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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  name: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled: boolean;
  role?: boolean;
};

class PermissionCheckbox extends React.Component<Props> {
  render() {
    const { name, checked, onChange, disabled, role, t } = this.props;

    const key = name.split(":").join(".");
    const label = role
      ? t("verbs.repository." + name + ".displayName")
      : this.translateOrDefault("permissions." + key + ".displayName", key);
    const helpText = role
      ? t("verbs.repository." + name + ".description")
      : this.translateOrDefault("permissions." + key + ".description", t("permissions.unknown"));

    // @ts-ignore we have to use the label here because cypress gets confused with asterix and dots
    const testId = label.replaceAll(" ", "-").toLowerCase();

    return (
      <Checkbox
        key={name}
        name={name}
        label={label}
        helpText={helpText}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        testId={testId}
      />
    );
  }

  translateOrDefault = (key: string, defaultText: string) => {
    const translation = this.props.t(key);
    if (translation === key) {
      return defaultText;
    } else {
      return translation;
    }
  };
}

export default withTranslation("plugins")(PermissionCheckbox);
