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

import React, { FC, ReactNode } from "react";
import styled from "styled-components";

const Container = styled.div`
  margin-bottom: 1em;
`;

const ContentWrapper = styled.div``;

const TitleWrapper = styled.div`
  display: flex;
  align-items: center;
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: bold;
`;

const Entry = styled.div``;

const Separator = styled.div`
  border-bottom: 1px solid rgb(219, 219, 219);
`;

type Props = {
  namespaceHeader: ReactNode;
  elements: ReactNode[];
};

const GroupEntries: FC<Props> = ({ namespaceHeader, elements }) => {
  const content = elements.map((entry, index) => (
    <>
      <Entry key={index}>{entry}</Entry>
      {index + 1 !== elements.length ? <Separator /> : null}
    </>
  ));

  return (
    <Container>
      <TitleWrapper>{namespaceHeader}</TitleWrapper>
      <Separator />
      <ContentWrapper>{content}</ContentWrapper>
      <div className="is-clearfix" />
    </Container>
  );
};

export default GroupEntries;
