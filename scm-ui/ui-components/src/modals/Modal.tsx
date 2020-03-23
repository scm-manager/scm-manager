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
  title: string;
  closeFunction: () => void;
  body: any;
  footer?: any;
  active: boolean;
  className?: string;
  headColor: string;
};

class Modal extends React.Component<Props> {
  static defaultProps = {
    headColor: "light"
  };

  render() {
    const { title, closeFunction, body, footer, active, className, headColor } = this.props;

    const isActive = active ? "is-active" : null;

    let showFooter = null;
    if (footer) {
      showFooter = <footer className="modal-card-foot">{footer}</footer>;
    }

    return (
      <div className={classNames("modal", className, isActive)}>
        <div className="modal-background" />
        <div className="modal-card">
          <header className={classNames("modal-card-head", `has-background-${headColor}`)}>
            <p className="modal-card-title is-marginless">{title}</p>
            <button className="delete" aria-label="close" onClick={closeFunction} />
          </header>
          <section className="modal-card-body">{body}</section>
          {showFooter}
        </div>
      </div>
    );
  }
}

export default Modal;
