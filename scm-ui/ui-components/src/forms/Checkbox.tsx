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
import { Help } from "../index";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import TriStateCheckbox from "./TriStateCheckbox";

type Props = {
  label?: string;
  onChange?: (value: boolean, name?: string) => void;
  checked: boolean;
  indeterminate?: boolean;
  name?: string;
  title?: string;
  disabled?: boolean;
  helpText?: string;
  testId?: string;
};

export default class Checkbox extends React.Component<Props> {
  onCheckboxChange = () => {
    if (this.props.onChange) {
      this.props.onChange(!this.props.checked, this.props.name);
    }
  };

  renderHelp = () => {
    const { title, helpText } = this.props;
    if (helpText && !title) {
      return <Help message={helpText} />;
    }
  };

  renderLabelWithHelp = () => {
    const { title, helpText } = this.props;
    if (title) {
      return <LabelWithHelpIcon label={title} helpText={helpText} />;
    }
  };

  render() {
    const { label, checked, indeterminate, disabled, testId } = this.props;
    return (
      <div className="field">
        {this.renderLabelWithHelp()}
        <div className="control" onClick={this.onCheckboxChange}>
          {/*
            we have to ignore the next line,
            because jsx label does not the custom disabled attribute
            but bulma does.
            // @ts-ignore */}
          <label className="checkbox" disabled={disabled}>
            <TriStateCheckbox checked={checked} indeterminate={indeterminate} disabled={disabled} testId={testId} />
            {label}
            {this.renderHelp()}
          </label>
        </div>
      </div>
    );
  }
}
