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
import classNames from "classnames";
import styled from "styled-components";
import { Link } from "react-router-dom";

type Props = {
  link: string;
  icon: ReactNode;
  contentLeft: ReactNode;
  contentRight: ReactNode;
  footer?: ReactNode;
};

const FlexFullHeight = styled.div`
  flex-direction: column;
  justify-content: space-around;
  align-self: stretch;
`;

const ContentLeft = styled.div`
  margin-bottom: 0 !important;
  overflow: hidden;
`;

const ContentRight = styled.div`
  margin-left: auto;
  align-items: start;
`;

const CenteredItems = styled.div`
  align-items: center;
`;

const StyledLink = styled(Link)`
  color: inherit;
  :hover {
    color: #33b2e8 !important;
  }
`;

const IconWrapper = styled.figure`
  margin-right: 0.5rem;
`;

const CardColumnSmall: FC<Props> = ({ link, icon, contentLeft, contentRight, footer }) => {
  return (
    <StyledLink to={link}>
      <div className="media">
        <IconWrapper className="media-left">{icon}</IconWrapper>
        <FlexFullHeight className={classNames("media-content", "text-box", "is-flex")}>
          <CenteredItems className="is-flex">
            <ContentLeft>{contentLeft}</ContentLeft>
            <ContentRight>{contentRight}</ContentRight>
          </CenteredItems>
          <small>{footer}</small>
        </FlexFullHeight>
      </div>
    </StyledLink>
  );
};

export default CardColumnSmall;
