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

import React, { FC, ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Link } from "react-router-dom";

type Props = {
  link?: string;
  avatar?: ReactNode;
  contentLeft: ReactNode;
  contentRight: ReactNode;
  footer?: ReactNode;
};

const StyledLink = styled(Link)`
  color: inherit;
`;

const CardColumnSmall: FC<Props> = ({ link, avatar, contentLeft, contentRight, footer }) => {
  const renderAvatar = avatar ? <figure className="media-left mr-2 mt-1">{avatar}</figure> : null;
  const renderFooter = footer ? <small>{footer}</small> : null;
  const content = (
    <div className="p-2 media has-hover-background-blue">
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
        <div className="is-flex is-flex-direction-column is-flex-align-items-start">
          <div className="is-clipped">{contentLeft}</div>
          <div>{contentRight}</div>
        </div>
        {renderFooter}
      </div>
    </div>
  );
  if (!link) {
    return content;
  }
  return <StyledLink to={link}>{content}</StyledLink>;
};

export default CardColumnSmall;
