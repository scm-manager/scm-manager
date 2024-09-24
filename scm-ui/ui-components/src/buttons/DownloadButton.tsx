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
  displayName: string;
  url?: string;
  disabled?: boolean;
  onClick?: () => void;
};

/**
 * @deprecated
 */
class DownloadButton extends React.Component<Props> {
  render() {
    const { displayName, url, disabled, onClick } = this.props;
    const onClickOrDefault = !!onClick ? onClick : () => {};
    return (
      <>
        {/*
        we have to ignore the next line, 
        because jsx a does not the custom disabled attribute
        but bulma does.
        // @ts-ignore */}
        <a className="button is-link" href={url} disabled={disabled} onClick={onClickOrDefault}>
          <span className="icon is-medium">
            <i className="fas fa-arrow-circle-down" />
          </span>
          <span>{displayName}</span>
        </a>
      </>
    );
  }
}

export default DownloadButton;
