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
import styled from "styled-components";
import { Icon } from "@scm-manager/ui-components";

type Props = {
  title: string;
  separatedEntries: boolean;
  children: ReactNode | Element[];
};

const Container = styled.div`
  margin-bottom: 1rem;
  a:not(:last-child) div.media {
    border-bottom: solid 1px #cdcdcd;
    padding-bottom: 1rem;
    margin-bottom: 1rem;
  }
`;

const Headline = styled.h3`
  font-size: 1.25rem;
  & small {
    font-size: 0.875rem;
  }
`;

const Content = styled.div`
  margin: 1.25rem 0 0;
`;

const Separator = styled.hr`
  margin: 0.5rem 0;
`;

const CollapsibleContainer: FC<Props> = ({ title, separatedEntries, children }) => {
  const [collapsed, setCollapsed] = useState(false);

  const icon = collapsed ? "angle-right" : "angle-down";
  let content = null;
  if (!collapsed) {
    if (separatedEntries) {
      content = React.Children.map(children, child => <Content>{child}</Content>);
    } else {
      content = <Content className="box">{children}</Content>;
    }
  }

  return (
    <Container>
      <div className="has-cursor-pointer" onClick={() => setCollapsed(!collapsed)}>
        <Headline>
          <Icon name={icon} color="default" /> {title}
        </Headline>
        <Separator />
      </div>
      {content}
    </Container>
  );
};

export default CollapsibleContainer;
