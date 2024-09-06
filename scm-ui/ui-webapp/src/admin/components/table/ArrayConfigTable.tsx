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
import { LabelWithHelpIcon, RemoveEntryOfTableButton } from "@scm-manager/ui-components";

type Props = {
  items: string[];
  label: string;
  removeLabel: string;
  onRemove: (p1: string[], p2: string) => void;
  disabled: boolean;
  helpText: string;
};

class ArrayConfigTable extends React.Component<Props> {
  render() {
    const { label, disabled, removeLabel, items, helpText } = this.props;
    if (items.length > 0) {
      return (
        <>
          <LabelWithHelpIcon label={label} helpText={helpText} />
          <table className="table is-hoverable is-fullwidth">
            <tbody>
              {items.map(item => {
                return (
                  <tr key={item}>
                    <td>{item}</td>
                    <td>
                      <RemoveEntryOfTableButton
                        entryname={item}
                        removeEntry={this.removeEntry}
                        disabled={disabled}
                        label={removeLabel}
                      />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      );
    }
    return null;
  }

  removeEntry = (item: string) => {
    const newItems = this.props.items.filter(name => name !== item);
    this.props.onRemove(newItems, item);
  };
}

export default ArrayConfigTable;
