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
  avatar?: ReactNode;
  contentLeft: ReactNode;
  contentRight: ReactNode;
  footer?: ReactNode;
};

const FlexFullHeight = styled.div`
  flex-direction: column;
  justify-content: space-around;
  align-self: stretch;
`;

const StyledLink = styled(Link)`
  color: inherit;
  :hover {
    color: #33b2e8 !important;
  }
`;

const CardColumnSmall: FC<Props> = ({ link, avatar, contentLeft, contentRight, footer }) => {
  const renderAvatar = avatar ? <figure className="media-left mr-2">{avatar}</figure> : null;
  const renderFooter = footer ? <small>{footer}</small> : null;

  return (
    <StyledLink to={link}>
      <div className="media">
        {renderAvatar}
        <FlexFullHeight className={classNames("media-content", "text-box", "is-flex")}>
          <div className="is-flex is-align-items-center">
            <div className="is-clipped mb-0">{contentLeft}</div>
            <div className="is-align-items-start ml-auto">{contentRight}</div>
          </div>
          {renderFooter}
        </FlexFullHeight>
      </div>
    </StyledLink>
  );
};

export default CardColumnSmall;
