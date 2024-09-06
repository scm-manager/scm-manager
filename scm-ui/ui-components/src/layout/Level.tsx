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

import React, { ReactNode } from "react";
import classNames from "classnames";

type Props = {
  className?: string;
  left?: ReactNode;
  children?: ReactNode;
  right?: ReactNode;
};

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

export default class Level extends React.Component<Props> {
  render() {
    const { className, left, children, right } = this.props;
    let child = null;
    if (children) {
      child = <div className="level-item">{children}</div>;
    }

    return (
      <div className={classNames("level", className)}>
        <div className="level-left">{left}</div>
        {child}
        <div className="level-right">{right}</div>
      </div>
    );
  }
}
