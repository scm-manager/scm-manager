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

import React, { AnchorHTMLAttributes, ButtonHTMLAttributes } from "react";
import { Link as ReactRouterLink, LinkProps as ReactRouterLinkProps } from "react-router-dom";
import classNames from "classnames";

export const ButtonVariants = {
  PRIMARY: "primary",
  SECONDARY: "secondary",
  TERTIARY: "tertiary",
  SIGNAL: "signal",
} as const;

export const ButtonVariantList = Object.values(ButtonVariants);

type ButtonVariant = typeof ButtonVariants[keyof typeof ButtonVariants];

const BASE_BUTTON_CLASSES = classNames(
  "inline-block whitespace-nowrap rounded border py-2 px-6 text-center font-semibold focus:z-10 focus:outline focus:outline-offset-2 focus:outline-purple-500 disabled:cursor-not-allowed"
);
const DEFAULT_BUTTON_CLASSES = classNames(
  "border-gray-200 hover:border-gray-400 active:shadow-inner disabled:hover:border-gray-200 disabled:active:shadow-none"
);
const PRIMARY_BUTTON_CLASSES = classNames(
  "border-transparent bg-primary text-primary-contrast hover:bg-primary-hover active:bg-primary-active disabled:bg-primary-disabled disabled:text-primary-disabled-contrast "
);
const SECONDARY_BUTTON_CLASSES = classNames(
  "border-primary text-primary hover:border-primary-hover hover:text-primary-hover active:border-primary-active active:text-primary-active disabled:border-primary-disabled disabled:text-primary-disabled"
);
const TERTIARY_BUTTON_CLASSES = classNames(
  "border-transparent text-primary hover:text-primary-hover active:text-primary-active disabled:text-primary-disabled"
);
const SIGNAL_BUTTON_CLASSES = classNames(
  "border-transparent bg-signal text-signal-contrast hover:bg-signal-hover hover:text-signal-hover-contrast active:bg-signal-active active:text-signal-active-contrast disabled:bg-signal-disabled disabled:text-signal-disabled-contrast"
);

const createButtonClasses = (variant?: ButtonVariant) =>
  classNames(BASE_BUTTON_CLASSES, {
    [DEFAULT_BUTTON_CLASSES]: !variant,
    [PRIMARY_BUTTON_CLASSES]: variant === "primary",
    [SECONDARY_BUTTON_CLASSES]: variant === "secondary",
    [TERTIARY_BUTTON_CLASSES]: variant === "tertiary",
    [SIGNAL_BUTTON_CLASSES]: variant === "signal",
  });

type BaseButtonProps = {
  variant: ButtonVariant;
};

type ButtonProps = BaseButtonProps & ButtonHTMLAttributes<HTMLButtonElement>;

/**
 * Styled html button
 */
export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, children, ...props }, ref) => (
    <button {...props} className={classNames(createButtonClasses(variant), className)} ref={ref}>
      {children}
    </button>
  )
);

type LinkButtonProps = BaseButtonProps & ReactRouterLinkProps;

/**
 * Styled react router link
 */
export const LinkButton = React.forwardRef<HTMLAnchorElement, LinkButtonProps>(
  ({ className, variant, children, ...props }, ref) => (
    <ReactRouterLink {...props} className={classNames(createButtonClasses(variant), className)} ref={ref}>
      {children}
    </ReactRouterLink>
  )
);

type ExternalLinkButtonProps = BaseButtonProps & AnchorHTMLAttributes<HTMLAnchorElement>;

/**
 * Styled html anchor
 */
export const ExternalLinkButton = React.forwardRef<HTMLAnchorElement, ExternalLinkButtonProps>(
  ({ className, variant, children, ...props }, ref) => (
    <a {...props} className={classNames(createButtonClasses(variant), className)} ref={ref}>
      {children}
    </a>
  )
);
