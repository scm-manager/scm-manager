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
import { withTranslation, WithTranslation } from "react-i18next";
import { NamespaceStrategies } from "@scm-manager/ui-types";
import { Select } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  namespaceStrategies?: NamespaceStrategies;
  label: string;
  value?: string;
  disabled?: boolean;
  helpText?: string;
  onChange: (value: string, name?: string) => void;
};

class NamespaceStrategySelect extends React.Component<Props> {
  createNamespaceOptions = () => {
    const { namespaceStrategies, t } = this.props;
    let available = [];
    if (namespaceStrategies && namespaceStrategies.available) {
      available = namespaceStrategies.available;
    }

    return available.map((ns) => {
      const key = "namespaceStrategies." + ns;
      let label = t(key);
      if (label === key) {
        label = ns;
      }
      return {
        value: ns,
        label: label,
      };
    });
  };

  findSelected = () => {
    const { namespaceStrategies, value } = this.props;
    if (!namespaceStrategies || !namespaceStrategies.available || namespaceStrategies.available.indexOf(value) < 0) {
      return namespaceStrategies.current;
    }
    return value;
  };

  render() {
    const { label, helpText, disabled, onChange } = this.props;
    const nsOptions = this.createNamespaceOptions();
    return (
      <Select
        label={label}
        onChange={onChange}
        value={this.findSelected()}
        disabled={disabled}
        options={nsOptions}
        helpText={helpText}
      />
    );
  }
}

export default withTranslation("plugins")(NamespaceStrategySelect);
