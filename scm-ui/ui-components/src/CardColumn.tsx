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
import React, { ReactNode, useCallback } from "react";
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

const InheritFlexShrinkDiv = styled.div`
  flex-shrink: inherit;
  pointer-events: all;
`;

const InvisibleButton = styled.button`
  background: none;
  border: none;
  cursor: pointer;
`;

const CardColumn = React.forwardRef<HTMLElement, Props>(
  ({ link, avatar, title, description, contentRight, footerLeft, footerRight, action, className }, ref) => {
    const renderAvatar = avatar ? <figure className="media-left mt-3 ml-4">{avatar}</figure> : null;
    const renderDescription = description ? <p className="shorten-text">{description}</p> : null;
    const renderContentRight = contentRight ? <div className="ml-auto">{contentRight}</div> : null;
    const executeRef = useCallback(
      (el: HTMLButtonElement | HTMLAnchorElement | null) => {
        if (typeof ref === "function") {
          ref(el);
        } else if (ref) {
          ref.current = el;
        }
      },
      [ref]
    );

    let createLink = null;
    if (link) {
      createLink = <Link ref={executeRef} className="overlay-column" to={link} />;
    } else if (action) {
      createLink = (
        <InvisibleButton
          ref={executeRef}
          className="overlay-column"
          onClick={(e) => {
            e.preventDefault();
            action();
          }}
          tabIndex={0}
        />
      );
    }

    return (
      <>
        {createLink}
        <NoEventWrapper className={classNames("media", className)}>
          {renderAvatar}
          <div
            className={classNames(
              "media-content",
              "text-box",
              "is-flex",
              "is-flex-direction-column",
              "is-justify-content-space-around",
              "is-align-self-stretch"
            )}
          >
            <div className="is-flex">
              <div className="is-clipped mb-0">
                <p className="shorten-text m-0">{title}</p>
                {renderDescription}
              </div>
              {renderContentRight}
            </div>
            <div className={classNames("level", "is-flex", "pb-4")}>
              <div className={classNames("level-left", "is-hidden-mobile", "mr-2")}>{footerLeft}</div>
              <InheritFlexShrinkDiv className="level-right is-block is-mobile m-0 shorten-text">
                {footerRight}
              </InheritFlexShrinkDiv>
            </div>
          </div>
        </NoEventWrapper>
      </>
    );
  }
);

export default CardColumn;
