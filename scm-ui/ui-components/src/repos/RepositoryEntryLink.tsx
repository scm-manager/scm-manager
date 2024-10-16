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

import React from "react";
import { Link } from "react-router-dom";
import Icon from "../Icon";
import Tooltip from "../Tooltip";

type Props = {
  to: string;
  icon: string;
  tooltip?: string;
};

class RepositoryEntryLink extends React.Component<Props> {
  render() {
    const { to, icon, tooltip } = this.props;

    let content = <Icon className="fa-lg" name={icon} color="inherit" alt={`${icon} icon`} />;
    if (tooltip) {
      content = (
        <Tooltip message={tooltip} location="top">
          {content}
        </Tooltip>
      );
    }

    return (
      <Link className="level-item is-clickable" to={to}>
        {content}
      </Link>
    );
  }
}

export default RepositoryEntryLink;
