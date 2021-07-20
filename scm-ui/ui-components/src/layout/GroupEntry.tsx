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
import { Link } from "react-router-dom";
import styled from "styled-components";
import { devices } from "../devices";

const StyledGroupEntry = styled.div`
  max-height: calc(90px - 1.5rem);
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  align-items: center;
  overflow: hidden;
  pointer-events: all;
`;

const OverlayLink = styled(Link)`
  width: 100%;
  position: absolute;
  height: calc(90px - 1.5rem);
  pointer-events: all;
  :hover {
    background-color: rgb(51, 178, 232, 0.1);
    cursor: pointer;
  }
`;

const Avatar = styled.div`
  padding-right: 1rem;
  .image * {
    width: 48px !important;
    height: 48px !important;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 2rem;
  }
`;

const Name = styled.div`
  padding: 0 0.25rem;
`;

const NameDescriptionContainer = styled.div`
  flex: 0 0 0;
`;

const Description = styled.p`
  display: block;
  padding: 0 0.25rem;
  height: 1.5rem;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  word-break: break-all;
  width: calc(55vw);
  @media screen and (max-width: ${devices.desktop.width - 1}px) {
    width: calc(80vw);
  }
`;

const ContentLeft = styled.div`
  display: flex;
`;

const ContentRight = styled.div`
  display: flex;
  justify-self: flex-end;
  justify-content: space-between;
  pointer-events: all;
  padding-left: 2rem;
  margin-bottom: -10px;
`;

type Props = {
  title?: string;
  avatar: string | ReactNode;
  name: string | ReactNode;
  description?: string | ReactNode;
  contentRight?: ReactNode;
  link: string;
};

const GroupEntry: FC<Props> = ({ link, avatar, title, name, description, contentRight }) => {
  return (
    <>
      <OverlayLink to={link} />
      <StyledGroupEntry title={title}>
        <ContentLeft>
          <Avatar>{avatar}</Avatar>
          <NameDescriptionContainer>
            <Name>{name}</Name>
            <Description>{description}</Description>
          </NameDescriptionContainer>
        </ContentLeft>
        <ContentRight className="is-hidden-touch">{contentRight}</ContentRight>
      </StyledGroupEntry>
    </>
  );
};

export default GroupEntry;
