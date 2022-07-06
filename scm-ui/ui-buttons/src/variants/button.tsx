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
} as const;

export const ButtonVariantList = Object.values(ButtonVariants);

export type ButtonVariant = typeof ButtonVariants[keyof typeof ButtonVariants];

const BASE_BUTTON_CLASSES =
  "font-semibold border rounded py-2 px-12 text-center disabled:opacity-50 disabled:cursor-not-allowed";
const DEFAULT_BUTTON_CLASSES =
  "border-gray-200 hover:border-gray-400 active:shadow-inner disabled:active:shadow-none disabled:hover:border-gray-200";
const PRIMARY_BUTTON_CLASSES =
  "bg-cyan-400 hover:bg-cyan-600 active:bg-cyan-700 disabled:hover:bg-cyan-400 disabled:active:bg-cyan-400 text-gray-50 border-transparent";

export const createButtonClasses = (variant?: ButtonVariant) =>
  classNames(BASE_BUTTON_CLASSES, {
    [DEFAULT_BUTTON_CLASSES]: !variant,
    [PRIMARY_BUTTON_CLASSES]: variant === "primary",
  });

export type BaseButtonProps = {
  variant?: ButtonVariant;
};

export type ButtonProps = BaseButtonProps & ButtonHTMLAttributes<HTMLButtonElement>;

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, children, ...props }, ref) => (
    <button {...props} className={classNames(createButtonClasses(variant), className)} ref={ref}>
      {children}
    </button>
  )
);

export type LinkButtonProps = BaseButtonProps & ReactRouterLinkProps;

export const LinkButton = React.forwardRef<HTMLAnchorElement, LinkButtonProps>(
  ({ className, variant, children, ...props }, ref) => (
    <ReactRouterLink {...props} className={classNames(createButtonClasses(variant), className)} ref={ref}>
      {children}
    </ReactRouterLink>
  )
);

export type ExternalLinkButtonProps = BaseButtonProps & AnchorHTMLAttributes<HTMLAnchorElement>;

export const ExternalLinkButton = React.forwardRef<HTMLAnchorElement, ExternalLinkButtonProps>(
  ({ className, variant, children, ...props }, ref) => (
    <a {...props} className={classNames(createButtonClasses(variant), className)} ref={ref}>
      {children}
    </a>
  )
);
