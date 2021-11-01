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
              {items.map((item) => {
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
    const newItems = this.props.items.filter((name) => name !== item);
    this.props.onRemove(newItems, item);
  };
}

export default ArrayConfigTable;
