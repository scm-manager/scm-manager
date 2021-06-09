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
  link?: string;
  avatar?: ReactNode;
  title: ReactNode;
  description?: string;
  contentRight?: ReactNode;
  footerLeft: ReactNode;
  footerRight: ReactNode;
  action?: () => void;
  className?: string;
};

const NoEventWrapper = styled.article`
  position: relative;
  pointer-events: none;
  z-index: 1;
`;

const AvatarWrapper = styled.figure`
  margin-top: 0.8em;
  margin-left: 1em !important;
`;

const FlexFullHeight = styled.div`
  flex-direction: column;
  justify-content: space-around;
  align-self: stretch;
`;

const FooterWrapper = styled.div`
  padding-bottom: 1rem;
`;

const ContentRight = styled.div`
  margin-left: auto;
`;

const RightMarginDiv = styled.div`
  margin-right: 0.5rem;
`;

const InheritFlexShrinkDiv = styled.div`
  flex-shrink: inherit;
  pointer-events: all;
`;

const CardColumn: FC<Props> = ({
  link,
  avatar,
  title,
  description,
  contentRight,
  footerLeft,
  footerRight,
  action,
  className
}) => {
  const renderAvatar = avatar ? <AvatarWrapper className="media-left">{avatar}</AvatarWrapper> : null;
  const renderDescription = description ? <p className="shorten-text">{description}</p> : null;
  const renderContentRight = contentRight ? <ContentRight>{contentRight}</ContentRight> : null;

  let createLink = null;
  if (link) {
    createLink = <Link className="overlay-column" to={link} />;
  } else if (action) {
    createLink = (
      <a
        className="overlay-column"
        onClick={e => {
          e.preventDefault();
          action();
        }}
        href="#"
      />
    );
  }

  return (
    <>
      {createLink}
      <NoEventWrapper className={classNames("media", className)}>
        {renderAvatar}
        <FlexFullHeight className={classNames("media-content", "text-box", "is-flex")}>
          <div className="is-flex">
            <div className="is-clipped mb-0">
              <p className="shorten-text m-0">{title}</p>
              {renderDescription}
            </div>
            {renderContentRight}
          </div>
          <FooterWrapper className={classNames("level", "is-flex")}>
            <RightMarginDiv className="level-left is-hidden-mobile">{footerLeft}</RightMarginDiv>
            <InheritFlexShrinkDiv className="level-right is-block is-mobile m-0 shorten-text">
              {footerRight}
            </InheritFlexShrinkDiv>
          </FooterWrapper>
        </FlexFullHeight>
      </NoEventWrapper>
    </>
  );
};

export default CardColumn;
