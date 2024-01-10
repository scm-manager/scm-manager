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
import * as React from "react";
import classNames from "classnames";
import styled from "styled-components";

const ActionContainer = styled.div`
  @media screen and (max-width: 768px) {
    flex-direction: column;
    > button {
      flex: 1 1 100%;
    }
  }
  @media screen and (min-width: 769px) and (max-width: 1215px) {
    > button {
      flex-grow: 1;
    }
  }
  @media screen and (min-width: 1216px) {
    > button {
      min-width: 25ch;
    }
  }
`;

type Props = {
  children?: React.ReactElement;
};

export default class PluginTopActions extends React.Component<Props> {
  render() {
    const { children } = this.props;
    return (
      <ActionContainer className={classNames("is-flex", "is-align-self-stretch", "has-gap-1")}>
        {children}
      </ActionContainer>
    );
  }
}
