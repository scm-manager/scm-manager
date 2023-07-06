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
import { Checkbox, InputField, Subtitle } from "@scm-manager/ui-components";
import { ConfigChangeHandler } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  baseUrl: string;
  forceBaseUrl: boolean;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

class BaseUrlSettings extends React.Component<Props> {
  render() {
    const { t, baseUrl, forceBaseUrl, hasUpdatePermission } = this.props;

    return (
      <>
        <Subtitle subtitle={t("base-url-settings.name")} />
        <div className="columns">
          <div className="column">
            <InputField
              label={t("base-url-settings.base-url")}
              onChange={this.handleBaseUrlChange}
              value={baseUrl}
              disabled={!hasUpdatePermission}
              helpText={t("help.baseUrlHelpText")}
            />
            <Checkbox
              checked={forceBaseUrl}
              label={t("base-url-settings.force-base-url")}
              onChange={this.handleForceBaseUrlChange}
              disabled={!hasUpdatePermission}
              helpText={t("help.forceBaseUrlHelpText")}
            />
          </div>
        </div>
      </>
    );
  }

  handleBaseUrlChange = (value: string) => {
    this.props.onChange(true, value, "baseUrl");
  };
  handleForceBaseUrlChange = (value: boolean) => {
    this.props.onChange(true, value, "forceBaseUrl");
  };
}

export default withTranslation("config")(BaseUrlSettings);
