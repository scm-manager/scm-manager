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

import React, { FC } from "react";
import { Hit as HitType } from "@scm-manager/ui-types";
import classNames from "classnames";

export type HitProps = {
  hit: HitType;
};

type Props = {
  className?: string;
};

type SearchResultType = FC & {
  Title: FC<Props>;
  Left: FC<Props>;
  Content: FC<Props>;
  Right: FC<Props>;
};

const Hit: SearchResultType = ({ children }) => {
  return <article className="media">{children}</article>;
};

Hit.Title = ({ className, children }) => (
  <h3 className={classNames("has-text-weight-bold is-ellipsis-overflow", className)}>{children}</h3>
);

Hit.Left = ({ className, children }) => <div className={classNames("media-left", className)}>{children}</div>;

Hit.Right = ({ className, children }) => (
  <div className={classNames("media-right is-size-7 has-text-right", className)}>{children}</div>
);

Hit.Content = ({ className, children }) => <div className={classNames("media-content", className)}> {children}</div>;

export default Hit;
