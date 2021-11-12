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
import { Links } from "@scm-manager/ui-types";
import { Checkbox, Select } from "@scm-manager/ui-components";

type Configuration = {
  compatibility: string;
  enabledGZip: boolean;
  _links: Links;
};

type Props = WithTranslation & {
  initialConfiguration: Configuration;
  readOnly: boolean;

  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

type State = Configuration;

class SvnConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
    };
  }

  handleChange = (value: any, name?: string) => {
    if (!name) {
      throw new Error("required name not set");
    }
    this.setState(
      // @ts-ignore
      {
        [name]: value,
      },
      () => this.props.onConfigurationChange(this.state, true)
    );
  };

  compatibilityOptions = (values: string[]) => {
    const options = [];
    for (const value of values) {
      options.push(this.compatibilityOption(value));
    }
    return options;
  };

  compatibilityOption = (value: string) => {
    return {
      value,
      label: this.props.t("scm-svn-plugin.config.compatibility-values." + value.toLowerCase()),
    };
  };

  render() {
    const { readOnly, t } = this.props;
    const compatibilityOptions = this.compatibilityOptions(["NONE", "PRE14", "PRE15", "PRE16", "PRE17", "WITH17"]);

    return (
      <>
        <Select
          name="compatibility"
          label={t("scm-svn-plugin.config.compatibility")}
          helpText={t("scm-svn-plugin.config.compatibilityHelpText")}
          value={this.state.compatibility}
          options={compatibilityOptions}
          onChange={this.handleChange}
        />
        <Checkbox
          name="enabledGZip"
          label={t("scm-svn-plugin.config.enabledGZip")}
          helpText={t("scm-svn-plugin.config.enabledGZipHelpText")}
          checked={this.state.enabledGZip}
          onChange={this.handleChange}
          disabled={readOnly}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(SvnConfigurationForm);
