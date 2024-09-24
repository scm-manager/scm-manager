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

import * as React from "react";
import { DisplayedUser } from "@scm-manager/ui-types";
import { Help, Tag } from "../index";

type Props = {
  items: DisplayedUser[];
  label: string;
  helpText?: string;
  onRemove: (p: DisplayedUser[]) => void;
};

/**
 * @deprecated
 */
export default class TagGroup extends React.Component<Props> {
  render() {
    const { items, label, helpText } = this.props;
    let help = null;
    if (helpText) {
      help = <Help message={helpText} />;
    }
    return (
      <div className="field is-grouped is-grouped-multiline">
        {label && items ? (
          <div className="control">
            <label className="label">
              <span>{label} </span>
              {help}
              {items.length > 0 ? ":" : ""}
            </label>
          </div>
        ) : (
          ""
        )}
        {items.map((item, key) => {
          return (
            <div className="control" key={key}>
              <div className="tags has-addons">
                <Tag color="info" outlined={true} label={item.displayName} onRemove={() => this.removeEntry(item)} />
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  removeEntry = (item: DisplayedUser) => {
    const newItems = this.props.items.filter((name) => name !== item);
    this.props.onRemove(newItems);
  };
}
