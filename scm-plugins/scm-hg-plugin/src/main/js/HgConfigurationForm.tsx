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
import { Link, Links } from "@scm-manager/ui-types";
import { InputField, Checkbox, Button, apiClient } from "@scm-manager/ui-components";

type Configuration = {
  hgBinary: string;
  encoding: string;
  showRevisionInId: boolean;
  enableHttpPostArgs: boolean;
  _links: Links;
};

type Props = WithTranslation & {
  initialConfiguration: Configuration;
  readOnly: boolean;

  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

type State = Configuration & {
  validationErrors: string[];
};

class HgConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
      validationErrors: []
    };
  }

  updateValidationStatus = () => {
    const requiredFields = ["hgBinary", "encoding"];

    const validationErrors = [];
    for (const field of requiredFields) {
      // @ts-ignore
      if (!this.state[field]) {
        validationErrors.push(field);
      }
    }

    this.setState({
      validationErrors
    });

    return validationErrors.length === 0;
  };

  hasValidationError = (name: string) => {
    return this.state.validationErrors.indexOf(name) >= 0;
  };

  handleChange = (value: string | boolean, name?: string) => {
    if (!name) {
      throw new Error("name not set");
    }
    this.setState(
      // @ts-ignore
      {
        [name]: value
      },
      () => this.props.onConfigurationChange(this.state, this.updateValidationStatus())
    );
  };

  triggerAutoConfigure = () => {
    apiClient
      .put(
        (this.props.initialConfiguration._links.autoConfiguration as Link).href,
        { ...this.props.initialConfiguration, hgBinary: this.state.hgBinary },
        "application/vnd.scmm-hgConfig+json;v=2"
      )
      .then(() =>
        apiClient
          .get((this.props.initialConfiguration._links.self as Link).href)
          .then(r => r.json())
          .then((config: Configuration) => this.setState({ hgBinary: config.hgBinary }))
      )
      .then(() => this.updateValidationStatus());
  };

  inputField = (name: string) => {
    const { readOnly, t } = this.props;
    return (
      <div className="column is-half">
        <InputField
          name={name}
          label={t("scm-hg-plugin.config." + name)}
          helpText={t("scm-hg-plugin.config." + name + "HelpText")}
          // @ts-ignore
          value={this.state[name]}
          onChange={this.handleChange}
          validationError={this.hasValidationError(name)}
          errorMessage={t("scm-hg-plugin.config.required")}
          disabled={readOnly}
        />
      </div>
    );
  };

  checkbox = (name: string) => {
    const { readOnly, t } = this.props;
    return (
      <Checkbox
        name={name}
        label={t("scm-hg-plugin.config." + name)}
        helpText={t("scm-hg-plugin.config." + name + "HelpText")}
        // @ts-ignore
        checked={this.state[name]}
        onChange={this.handleChange}
        disabled={readOnly}
      />
    );
  };

  render() {
    const { t } = this.props;
    return (
      <div className="columns is-multiline">
        {this.inputField("hgBinary")}
        {this.inputField("encoding")}
        <div className="column is-half">{this.checkbox("showRevisionInId")}</div>
        <div className="column is-half">{this.checkbox("enableHttpPostArgs")}</div>
        <div className="column is-full">
          <Button
            disabled={!this.props.initialConfiguration?._links?.autoConfiguration}
            action={() => this.triggerAutoConfigure()}
          >
            {t("scm-hg-plugin.config.autoConfigure")}
          </Button>
        </div>
      </div>
    );
  }
}

export default withTranslation("plugins")(HgConfigurationForm);
