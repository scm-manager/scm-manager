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
import classNames from "classnames";
import { Icon } from "../buttons";

export const StatusVariants = {
  IN_PROGRESS: "in progress",
  SUCCESS: "success",
  WARNING: "warning",
  DANGER: "danger",
  UNDEFINED: "undefined",
};

export const StatusVariantList = Object.values(StatusVariants);

export type StatusVariant = typeof StatusVariants[keyof typeof StatusVariants];

export const StatusIconSizeVariants = {
  SMALL_X: "xs",
  SMALL: "sm",
  MEDIUM: "md",
  LARGE: "lg",
  LARGE_2X: "2x",
};

export const StatusIconSizeVariantList = Object.values(StatusIconSizeVariants);

export type StatusIconSizeVariant = typeof StatusIconSizeVariants[keyof typeof StatusIconSizeVariants];

type IconProps = React.HTMLProps<HTMLElement> & {
  variant: StatusVariant;
  color?: string;
  iconSize?: StatusIconSizeVariant;
  invert?: boolean;
};

/**
 *
 * @beta
 * @since 3.9.0
 */
const StatusIcon = React.forwardRef<HTMLElement, IconProps>(
  ({ color, className, iconSize = StatusIconSizeVariants.MEDIUM, variant, invert = false, children }, ref) => {
    const icon = classNames({
      "exclamation-triangle": variant === StatusVariants.DANGER,
      "check-circle": variant === StatusVariants.SUCCESS,
      "hourglass-start": variant === StatusVariants.IN_PROGRESS,
      "circle-notch": variant === StatusVariants.UNDEFINED,
    });
    if (!color) {
      if (invert) {
        color = classNames({
          "icon-color-inverted": variant === StatusVariants.DANGER || StatusVariants.SUCCESS,
          "icon-warning-inverted": variant === StatusVariants.WARNING,
          "icon-color-inverted-secondary":
            variant === StatusVariants.IN_PROGRESS || variant === StatusVariants.UNDEFINED,
        });
      } else {
        color = classNames({
          "has-text-danger": variant === StatusVariants.DANGER,
          "has-text-success": variant === StatusVariants.SUCCESS,
          "has-text-warning": variant === StatusVariants.WARNING,
          "icon-color-secondary": variant === StatusVariants.IN_PROGRESS || variant === StatusVariants.UNDEFINED,
        });
      }
    }

    return (
      <div className="is-flex is-align-items-center">
        {variant === "warning" ? (
          <WarningIcon color={color} iconSize={iconSize} className={className}>{`${icon}`}</WarningIcon>
        ) : (
          <Icon className={className}>{`${icon} ${color} fa-${iconSize}`}</Icon>
        )}
        {children && <span className="ml-2">{children}</span>}
      </div>
    );
  }
);

type WarningIconProps = React.HTMLProps<HTMLElement> & {
  color?: string;
  iconSize?: string;
};

const WarningIcon = React.forwardRef<HTMLElement, WarningIconProps>(({ color, className, iconSize }, ref) => {
  return (
    <span className={classNames(className, "icon", color)} aria-hidden="true" ref={ref}>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        id="Ebene_1"
        data-name="Ebene 1"
        height="1em"
        width="1em"
        viewBox="0 0 140 140"
        fill="currentColor"
        className={classNames("fas fa-fw fa-custom-icon", `fa-${iconSize}`)}
      >
        <defs>
          <mask id="cutoutMask">
            <rect x="0" y="0" width="140" height="140" fill="white" />
            <path
              className="cls-2"
              d="M79.93,109.67c-2.75,2.75-6.06,4.13-9.93,4.13s-7.18-1.38-9.93-4.13-4.13-6.06-4.13-9.93,1.38-7.18,4.13-9.93,6.06-4.13,9.93-4.13,7.18,1.38,9.93,4.13,4.13,6.06,4.13,9.93-1.38,7.18-4.13,9.93ZM59.98,76.01c0,1.17.41,2.14,1.23,2.9s1.82,1.14,2.99,1.14h11.6c1.17,0,2.17-.38,2.99-1.14s1.23-1.73,1.23-2.9l2.46-47.81c0-1.17-.41-2.2-1.23-3.08s-1.82-1.32-2.99-1.32h-16.52c-1.17,0-2.17.44-2.99,1.32s-1.23,1.9-1.23,3.08l2.46,47.81Z"
              fill="black"
            />
          </mask>
        </defs>
        <path
          className="cls-1"
          d="M125,0c4.17,0,7.71,1.46,10.62,4.38s4.38,6.46,4.38,10.62v110c0,4.17-1.46,7.71-4.38,10.63-2.92,2.92-6.46,4.38-10.62,4.38H15c-4.17,0-7.71-1.46-10.62-4.38-2.92-2.92-4.38-6.46-4.38-10.63V15c0-4.17,1.46-7.71,4.38-10.62S10.83,0,15,0h110Z"
          mask="url(#cutoutMask)"
          fill="currentColor"
        />
      </svg>
    </span>
  );
});

export default StatusIcon;
