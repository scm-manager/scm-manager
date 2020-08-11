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
import React, { MouseEvent, ReactNode } from "react";
import classNames from "classnames";
import { withRouter, RouteComponentProps } from "react-router-dom";
import Icon from "../Icon";
import { createAttributesForTesting } from "../devBuild";

export type ButtonProps = {
  label?: string;
  loading?: boolean;
  disabled?: boolean;
  action?: (event: MouseEvent) => void;
  link?: string;
  className?: string;
  icon?: string;
  fullWidth?: boolean;
  reducedMobile?: boolean;
  children?: ReactNode;
  testId?: string;
};

type Props = ButtonProps &
  RouteComponentProps & {
    title?: string;
    type?: "button" | "submit" | "reset";
    color?: string;
  };

class Button extends React.Component<Props> {
  static defaultProps: Partial<Props> = {
    type: "button",
    color: "default"
  };

  onClick = (event: React.MouseEvent) => {
    const { action, link, history } = this.props;
    if (action) {
      action(event);
    } else if (link) {
      history.push(link);
    }
  };

  render() {
    const {
      label,
      title,
      loading,
      disabled,
      type,
      color,
      className,
      icon,
      fullWidth,
      reducedMobile,
      children,
      testId
    } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";
    const reducedMobileClass = reducedMobile ? "is-reduced-mobile" : "";
    if (icon) {
      return (
        <button
          type={type}
          title={title}
          disabled={disabled}
          onClick={this.onClick}
          className={classNames("button", "is-" + color, loadingClass, fullWidthClass, reducedMobileClass, className)}
          {...createAttributesForTesting(testId)}
        >
          <span className="icon is-medium">
            <Icon name={icon} color="inherit" />
          </span>
          <span>
            {label} {children}
          </span>
        </button>
      );
    }

    return (
      <button
        type={type}
        title={title}
        disabled={disabled}
        onClick={this.onClick}
        className={classNames("button", "is-" + color, loadingClass, fullWidthClass, className)}
        {...createAttributesForTesting(testId)}
      >
        {label} {children}
      </button>
    );
  }
}

export default withRouter(Button);
