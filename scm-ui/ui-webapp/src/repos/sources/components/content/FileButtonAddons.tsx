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
import { Button, ButtonAddons } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  className?: string;
  historyIsSelected: boolean;
  showHistory: (p: boolean) => void;
};

class FileButtonAddons extends React.Component<Props> {
  showHistory = () => {
    this.props.showHistory(true);
  };

  showSources = () => {
    this.props.showHistory(false);
  };

  color = (selected: boolean) => {
    return selected ? "link is-selected" : "";
  };

  render() {
    const { className, t, historyIsSelected } = this.props;

    return (
      <ButtonAddons className={className}>
        <div title={t("sources.content.sourcesButton")}>
          <Button action={this.showSources} color={this.color(!historyIsSelected)}>
            <span className="icon">
              <i className="fas fa-code" />
            </span>
          </Button>
        </div>
        <div title={t("sources.content.historyButton")}>
          <Button action={this.showHistory} color={this.color(historyIsSelected)}>
            <span className="icon">
              <i className="fas fa-history" />
            </span>
          </Button>
        </div>
      </ButtonAddons>
    );
  }
}

export default withTranslation("repos")(FileButtonAddons);
