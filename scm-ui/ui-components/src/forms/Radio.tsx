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
import React, { ChangeEvent } from "react";
import { Help } from "../index";
import styled from "styled-components";

const StyledRadio = styled.label`
  margin-right: 0.5em;
`;

type Props = {
  label?: string;
  name?: string;
  value?: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled?: boolean;
  helpText?: string;
};

class Radio extends React.Component<Props> {
  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return <Help message={helpText} className="has-tooltip-multiline" />;
    }
  };

  onValueChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked, this.props.name);
    }
  };

  render() {
    return (
      <>
        {/*
        we have to ignore the next line, 
        because jsx label does not the custom disabled attribute
        but bulma does.
        // @ts-ignore */}
        <StyledRadio className="radio" disabled={this.props.disabled}>
          <input
            type="radio"
            name={this.props.name}
            value={this.props.value}
            checked={this.props.checked}
            onChange={this.onValueChange}
            disabled={this.props.disabled}
          />{" "}
          {this.props.label}
          {this.renderHelp()}
        </StyledRadio>
      </>
    );
  }
}

export default Radio;
