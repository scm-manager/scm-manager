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

/**
 * @deprecated Use {@link ui-buttons/src/Button.tsx} instead
 */
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
