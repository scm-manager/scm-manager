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
  bytes: number;
};

class FileSize extends React.Component<Props> {
  static format(bytes: number) {
    if (!bytes) {
      return "0 B";
    }

    const units = ["B", "K", "M", "G", "T", "P", "E", "Z", "Y"];
    const i = Math.floor(Math.log(bytes) / Math.log(1000));

    const size = i === 0 ? bytes : (bytes / 1000 ** i).toFixed(2);
    return `${size} ${units[i]}`;
  }

  render() {
    const formattedBytes = FileSize.format(this.props.bytes);
    return <span>{formattedBytes}</span>;
  }
}

export default FileSize;
