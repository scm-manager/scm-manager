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
