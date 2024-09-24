/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
