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
import { Links } from "@scm-manager/ui-types";
import { InputField, Checkbox, validation as validator } from "@scm-manager/ui-components";

type Configuration = {
  repositoryDirectory?: string;
  gcExpression?: string;
  nonFastForwardDisallowed: boolean;
  defaultBranch: string;
  _links: Links;
};

type Props = WithTranslation & {
  initialConfiguration: Configuration;
  readOnly: boolean;

  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

type State = Configuration & {};

class GitConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  onGcExpressionChange = (value: string) => {
    this.setState(
      {
        gcExpression: value
      },
      () => this.props.onConfigurationChange(this.state, true)
    );
  };

  onNonFastForwardDisallowed = (value: boolean) => {
    this.setState(
      {
        nonFastForwardDisallowed: value
      },
      () => this.props.onConfigurationChange(this.state, true)
    );
  };

  onDefaultBranchChange = (value: string) => {
    this.setState(
      {
        defaultBranch: value
      },
      () => this.props.onConfigurationChange(this.state, this.isValidDefaultBranch())
    );
  };

  isValidDefaultBranch = () => {
    return validator.isNameValid(this.state.defaultBranch);
  };

  render() {
    const { gcExpression, nonFastForwardDisallowed, defaultBranch } = this.state;
    const { readOnly, t } = this.props;

    return (
      <>
        <InputField
          name="gcExpression"
          label={t("scm-git-plugin.config.gcExpression")}
          helpText={t("scm-git-plugin.config.gcExpressionHelpText")}
          value={gcExpression}
          onChange={this.onGcExpressionChange}
          disabled={readOnly}
        />
        <Checkbox
          name="nonFastForwardDisallowed"
          label={t("scm-git-plugin.config.nonFastForwardDisallowed")}
          helpText={t("scm-git-plugin.config.nonFastForwardDisallowedHelpText")}
          checked={nonFastForwardDisallowed}
          onChange={this.onNonFastForwardDisallowed}
          disabled={readOnly}
        />
        <InputField
          name="defaultBranch"
          label={t("scm-git-plugin.config.defaultBranch")}
          helpText={t("scm-git-plugin.config.defaultBranchHelpText")}
          value={defaultBranch}
          onChange={this.onDefaultBranchChange}
          disabled={readOnly}
          validationError={!this.isValidDefaultBranch()}
          errorMessage={t("scm-git-plugin.config.defaultBranchValidationError")}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(GitConfigurationForm);
