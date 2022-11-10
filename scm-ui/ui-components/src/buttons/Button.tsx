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
import React, { KeyboardEvent, MouseEvent, ReactNode, useCallback } from "react";
import classNames from "classnames";
import { Link } from "react-router-dom";
import Icon from "../Icon";
import { createAttributesForTesting } from "../devBuild";

export type ButtonProps = {
  label?: string;
  title?: string;
  loading?: boolean;
  disabled?: boolean;
  action?: (event: MouseEvent | KeyboardEvent) => void;
  link?: string;
  className?: string;
  icon?: string;
  fullWidth?: boolean;
  reducedMobile?: boolean;
  children?: ReactNode;
  testId?: string;
};

type Props = ButtonProps & {
  type?: "button" | "submit" | "reset";
  color?: string;
};

const Button = React.forwardRef<HTMLButtonElement | HTMLAnchorElement, Props>(
  (
    {
      link,
      className,
      icon,
      fullWidth,
      reducedMobile,
      testId,
      children,
      label,
      type = "button",
      title,
      loading,
      disabled,
      action,
      color = "default",
    },
    ref
  ) => {
    const executeRef = useCallback(
      (el: HTMLButtonElement | HTMLAnchorElement | null) => {
        if (typeof ref === "function") {
          ref(el);
        } else if (ref) {
          ref.current = el;
        }
      },
      [ref]
    );
    const renderIcon = () => {
      return (
        <>
          {icon ? (
            <Icon name={icon} color="inherit" className={classNames("is-medium", { "pr-5": label || children })} />
          ) : null}
        </>
      );
    };

    const classes = classNames(
      "button",
      "is-" + color,
      { "is-loading": loading },
      { "is-fullwidth": fullWidth },
      { "is-reduced-mobile": reducedMobile },
      className
    );

    const content = (
      <span>
        {renderIcon()}{" "}
        {(label || children) && (
          <>
            {label} {children}
          </>
        )}
      </span>
    );

    if (link && !disabled) {
      if (link.includes("://")) {
        return (
          <a ref={executeRef} className={classes} href={link} aria-label={label}>
            {content}
          </a>
        );
      }
      return (
        <Link ref={executeRef} className={classes} to={link} aria-label={label}>
          {content}
        </Link>
      );
    }

    return (
      <button
        type={type}
        title={title}
        disabled={disabled}
        onClick={(event) => action && action(event)}
        className={classes}
        ref={executeRef}
        {...createAttributesForTesting(testId)}
      >
        {content}
      </button>
    );
  }
);

export default Button;
