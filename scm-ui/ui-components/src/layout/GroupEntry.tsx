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
import { useHistory } from "react-router-dom";
import styled from "styled-components";

const StyledGroupEntry = styled.div`
  width: 100%;
  display: flex;
  padding: 1rem;
  border: 1px solid rgb(219, 219, 219);
  border-radius: 5px;
  align-items: center;
  overflow: hidden;

  :hover {
    border: 1px solid #33b2e8;
    cursor: pointer;
  }
`;

const Avatar = styled.div`
  padding: 0 0.25rem;
`;

const Name = styled.div`
  padding: 0 0.25rem;
`;

const NameDescriptionContainer = styled.div`
  flex-grow: 1;
`;

const Description = styled.div`
  padding: 0 0.25rem;
`;

const ContentLeft = styled.div`
  padding: 0 1rem;
`;

const ContentRight = styled.div`
  padding-left: 2rem;
`;

type Props = {
  link: string;
  title?: string;
  avatar: string | ReactNode;
  name: string | ReactNode;
  description?: string | ReactNode;
  contentLeft: ReactNode;
  contentRight: ReactNode;
};

const GroupEntry: FC<Props> = ({ link, avatar, title, name, description, contentLeft, contentRight }) => {
  const history = useHistory();

  return (
    <StyledGroupEntry onClick={() => history.push(link)} title={title}>
      <Avatar>{avatar}</Avatar>
      <NameDescriptionContainer>
        <Name>{name}</Name>
        {description ? <Description>{description}</Description> : null}
      </NameDescriptionContainer>
      <ContentLeft>{contentLeft}</ContentLeft>
      <ContentRight>{contentRight}</ContentRight>
    </StyledGroupEntry>
  );
};

export default GroupEntry;
