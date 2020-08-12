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
import React, { ChangeEvent, FormEvent } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { createAttributesForTesting } from "../devBuild";

type Props = WithTranslation & {
  filter: (p: string) => void;
  value?: string;
  testId?: string;
};

type State = {
  value: string;
};

const FixedHeightInput = styled.input`
  height: 2.5rem;
`;

class FilterInput extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      value: this.props.value ? this.props.value : ""
    };
  }

  handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    this.setState({
      value: event.target.value
    });
  };

  handleSubmit = (event: FormEvent) => {
    this.props.filter(this.state.value);
    event.preventDefault();
  };

  render() {
    const { t, testId } = this.props;
    return (
      <form className="input-field" onSubmit={this.handleSubmit} {...createAttributesForTesting(testId)}>
        <div className="control has-icons-left">
          <FixedHeightInput
            className="input"
            type="search"
            placeholder={t("filterEntries")}
            value={this.state.value}
            onChange={this.handleChange}
          />
          <span className="icon is-small is-left">
            <i className="fas fa-search" />
          </span>
        </div>
      </form>
    );
  }
}

export default withTranslation("commons")(FilterInput);
