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

import React, { MouseEvent, KeyboardEvent } from "react";
import { DeleteButton } from ".";
import classNames from "classnames";

type Props = {
  entryname: string;
  removeEntry: (p: string) => void;
  disabled: boolean;
  label: string;
};

type State = {};

/**
 * @deprecated
 */
class RemoveEntryOfTableButton extends React.Component<Props, State> {
  render() {
    const { label, entryname, removeEntry, disabled } = this.props;
    return (
      <div className={classNames("is-pulled-right")}>
        <DeleteButton
          label={label}
          action={(event: MouseEvent | KeyboardEvent) => {
            event.preventDefault();
            removeEntry(entryname);
          }}
          disabled={disabled}
        />
      </div>
    );
  }
}

export default RemoveEntryOfTableButton;
