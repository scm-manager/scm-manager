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

import React, { FC, ReactNode, useState } from "react";
import classNames from "classnames";
import { Link } from "react-router-dom";
import styled from "styled-components";

const Container = styled.div`
  margin-bottom: 1em;
`;

const Wrapper = styled.div`
  padding: 0 0.75rem;
`;

type Props = {
  name: ReactNode;
  url?: string;
  elements: ReactNode[];
};

const ColumnGroup: FC<Props> = ({ name, url, elements }) => {
  const [collapsed, setCollapsed] = useState(false);

  const icon = collapsed ? "fa-angle-right" : "fa-angle-down";

  let content = null;
  if (!collapsed) {
    content = elements.map((entry, index) => {
      return (
        <div className={classNames("box", "box-link-shadow", "column", "is-clipped", "is-full")} key={index}>
          {entry}
        </div>
      );
    });
  }

  return (
    <Container>
      <h2>
        <span className={classNames("is-size-4", "has-cursor-pointer")} onClick={() => setCollapsed(!collapsed)}>
          <i className={classNames("fa", icon)} />
        </span>{" "}
        {url ? (
          <Link to={url} className={"has-text-dark"}>
            {name}
          </Link>
        ) : (
          name
        )}
      </h2>
      <hr />
      <Wrapper className={classNames("columns", "is-multiline")}>{content}</Wrapper>
      <div className="is-clearfix" />
    </Container>
  );
};

export default ColumnGroup;
