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
import { Link } from "react-router-dom";
import styled from "styled-components";
import Icon from "../Icon";

const Container = styled.div`
  margin-bottom: 0.2em;
`;

const ContentWrapper = styled.div`
  padding: 0 0.75rem;
`;

const TitleWrapper = styled.div`
  font-size: 1.25rem;
  font-weight: bold;
`;

const CollapsableTitle = styled.span`
  cursor: pointer;
  font-size: 1rem;
`;
const Entry = styled.div`
  margin: 0.5rem 0;
`;

type Props = {
  name: ReactNode;
  url?: string;
  elements: ReactNode[];
};

const GroupEntries: FC<Props> = ({ name, url, elements }) => {
  const [collapsed, setCollapsed] = useState(false);

  let content = null;
  if (!collapsed) {
    content = elements.map((entry, index) => {
      return <Entry key={index}>{entry}</Entry>;
    });
  }

  return (
    <Container>
      <TitleWrapper>
        <CollapsableTitle onClick={() => setCollapsed((prevState: boolean) => !prevState)}>
          <Icon name={collapsed ? "angle-right" : "angle-down"} color="dark" />
        </CollapsableTitle>{" "}
        {url ? (
          <Link to={url} className={"has-text-dark"}>
            {name}
          </Link>
        ) : (
          name
        )}
      </TitleWrapper>
      <ContentWrapper>{content}</ContentWrapper>
      <div className="is-clearfix" />
    </Container>
  );
};

export default GroupEntries;
