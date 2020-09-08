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
import React, { ReactNode } from "react";
import { Link } from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  name: string;
  url?: string;
  elements: ReactNode[];
};

type State = {
  collapsed: boolean;
};

const Container = styled.div`
  margin-bottom: 1em;
`;

const Wrapper = styled.div`
  padding: 0 0.75rem;
`;

export default class CardColumnGroup extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  toggleCollapse = () => {
    this.setState(prevState => ({
      collapsed: !prevState.collapsed
    }));
  };

  isLastEntry = (array: ReactNode[], index: number) => {
    return index === array.length - 1;
  };

  isLengthOdd = (array: ReactNode[]) => {
    return array.length % 2 !== 0;
  };

  isFullSize = (array: ReactNode[], index: number) => {
    return this.isLastEntry(array, index) && this.isLengthOdd(array);
  };

  render() {
    const { name, url, elements } = this.props;
    const { collapsed } = this.state;

    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    let content = null;
    if (!collapsed) {
      content = elements.map((entry, index) => {
        const fullColumnWidth = this.isFullSize(elements, index);
        const sizeClass = fullColumnWidth ? "is-full" : "is-half";
        return (
          <div className={classNames("box", "box-link-shadow", "column", "is-clipped", sizeClass)} key={index}>
            {entry}
          </div>
        );
      });
    }

    const header = url ? (
      <h2>
        <span className={classNames("is-size-4", "has-cursor-pointer")} onClick={this.toggleCollapse}>
          <i className={classNames("fa", icon)} />
        </span>{" "}
        <Link to={url} className={"has-text-dark"}>{name}</Link>
      </h2>
    ) : (
      <h2>
        <span className={classNames("is-size-4", "has-cursor-pointer")} onClick={this.toggleCollapse}>
          <i className={classNames("fa", icon)} /> {name}
        </span>
      </h2>
    );

    return (
      <Container>
        {header}
        <hr />
        <Wrapper className={classNames("columns", "card-columns", "is-multiline")}>{content}</Wrapper>
        <div className="is-clearfix" />
      </Container>
    );
  }
}
