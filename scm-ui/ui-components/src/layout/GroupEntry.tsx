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
import React, {FC, ReactNode} from "react";
import {Link} from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";
import {useTranslation} from "react-i18next";
import {useKeyboardIteratorTarget} from "@scm-manager/ui-shortcuts";

const StyledGroupEntry = styled.div`
  max-height: calc(90px - 1.5rem);
  width: 100%;
  pointer-events: all;
`;

const OverlayLink = styled(Link)`
  width: 100%;
  position: absolute;
  height: calc(90px - 1.5rem);
  pointer-events: all;
  border-radius: 4px;
  :hover {
    cursor: pointer;
  }
`;

const Avatar = styled.div`
  .predefined-avatar {
    height: 48px;
    width: 48px;
    font-size: 1.75rem;
  }
`;

const Description = styled.p`
  height: 1.5rem;
  text-overflow: ellipsis;
  overflow-x: hidden;
  overflow-y: visible;
  white-space: nowrap;
  word-break: break-all;
`;

const ContentLeft = styled.div`
  min-width: 0;
`;

const ContentRight = styled.div`
  pointer-events: all;
  margin-bottom: -10px;
`;

type Props = {
  title?: string;
  avatar: string | ReactNode;
  name: string | ReactNode;
  description?: string | ReactNode;
  contentRight?: ReactNode;
  link: string;
  ariaLabel?: string;
};

const GroupEntry: FC<Props> = ({ link, avatar, title, name, description, contentRight, ariaLabel }) => {
  const [t] = useTranslation("repos");
  const ref = useKeyboardIteratorTarget();
  return (
    <div className="is-relative">
      <OverlayLink
        ref={ref}
        to={link}
        className="has-hover-background-blue"
        aria-label={t("overview.ariaLabel", { name: ariaLabel })}
      />
      <StyledGroupEntry
        className={classNames("is-flex", "is-justify-content-space-between", "is-align-items-center", "p-2")}
        title={title}
      >
        <ContentLeft className={classNames("is-flex", "is-flex-grow-1", "is-align-items-center")}>
          <Avatar className="mr-4">{avatar}</Avatar>
          <div className={classNames("is-flex-grow-1", "is-clipped")}>
            <div className="mx-1">{name}</div>
            <Description className="mx-1">{description}</Description>
          </div>
        </ContentLeft>
        <ContentRight
          className={classNames(
            "is-hidden-touch",
            "is-flex",
            "is-flex-shrink-0",
            "is-justify-content-flex-end",
            "pl-5"
          )}
        >
          {contentRight}
        </ContentRight>
      </StyledGroupEntry>
    </div>
  );
};

export default GroupEntry;
