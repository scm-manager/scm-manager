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
import * as React from "react";
import classNames from "classnames";

type Props = {
  className?: string;
  color: string;
  icon?: string;
  label: string;
  title?: string;
  onClick?: () => void;
  onRemove?: () => void;
};

class Tag extends React.Component<Props> {
  static defaultProps = {
    color: "light"
  };

  render() {
    const { className, color, icon, label, title, onClick, onRemove } = this.props;
    let showIcon = null;
    if (icon) {
      showIcon = (
        <>
          <i className={classNames("fas", `fa-${icon}`)} />
          &nbsp;
        </>
      );
    }
    let showDelete = null;
    if (onRemove) {
      showDelete = <a className="tag is-delete" onClick={onRemove} />;
    }

    return (
      <>
        <span className={classNames("tag", `is-${color}`, className)} title={title} onClick={onClick}>
          {showIcon}
          {label}
        </span>
        {showDelete}
      </>
    );
  }
}

export default Tag;
