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
import React, { ReactNode } from "react";

type Props = {
  message: string;
  className?: string;
  location: TooltipLocation;
  multiline?: boolean;
  children: ReactNode;
  id?: string;
};

export type TooltipLocation = "bottom" | "right" | "top" | "left";

class Tooltip extends React.Component<Props> {
  static defaultProps = {
    location: "right"
  };

  render() {
    const { className, message, location, multiline, children, id } = this.props;
    let classes = `tooltip has-tooltip-${location}`;
    if (multiline) {
      classes += " has-tooltip-multiline";
    }
    if (className) {
      classes += " " + className;
    }

    return (
      <span className={classes} data-tooltip={message} aria-label={message} id={id}>
        {children}
      </span>
    );
  }
}

export default Tooltip;
