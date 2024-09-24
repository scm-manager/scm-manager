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

type Props = {
  icon?: string;
  label: string;
  action: () => void;
};

// TODO is it used in the menu? should it use MenuContext for collapse state?

class NavAction extends React.Component<Props> {
  render() {
    const { label, icon, action } = this.props;

    let showIcon = null;
    if (icon) {
      showIcon = (
        <>
          <i className={icon} />{" "}
        </>
      );
    }

    return (
      <li>
        <button onClick={action}>
          {showIcon}
          {label}
        </button>
      </li>
    );
  }
}

export default NavAction;
