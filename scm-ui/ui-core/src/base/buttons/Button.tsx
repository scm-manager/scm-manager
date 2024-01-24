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
import { createAttributesForTesting } from "../helpers";

/**
 * @beta
 * @since 2.41.0
 */
export const ButtonVariants = {
  PRIMARY: "primary",
  SECONDARY: "secondary",
  TERTIARY: "tertiary",
  SIGNAL: "signal",
} as const;

export const ButtonVariantList = Object.values(ButtonVariants);

type ButtonVariant = typeof ButtonVariants[keyof typeof ButtonVariants];

const createButtonClasses = (variant?: ButtonVariant, isLoading?: boolean) =>
  classNames("button", {
    "is-primary": variant === "primary",
    "is-primary is-outlined": variant === "secondary",
    "is-primary is-inverted": variant === "tertiary",
    "is-warning": variant === "signal",
    "is-loading": isLoading,
  });

type BaseButtonProps = {
  variant?: ButtonVariant;
  isLoading?: boolean;
  testId?: string;
};

type ButtonProps = BaseButtonProps & ButtonHTMLAttributes<HTMLButtonElement>;

/**
 * Styled html button.
 *
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 */
export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, isLoading, testId, type, children, ...props }, ref) => (
    <button
      type={type ?? "button"}
      {...props}
      className={classNames(createButtonClasses(variant, isLoading), className)}
      ref={ref}
      {...createAttributesForTesting(testId)}
    >
      {children}
    </button>
  )
);

type LinkButtonProps = BaseButtonProps & ReactRouterLinkProps;

/**
 * Styled react router link.
 *
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 */
export const LinkButton = React.forwardRef<HTMLAnchorElement, LinkButtonProps>(
  ({ className, variant, isLoading, testId, children, ...props }, ref) => (
    <ReactRouterLink
      {...props}
      className={classNames(createButtonClasses(variant, isLoading), className)}
      ref={ref}
      {...createAttributesForTesting(testId)}
    >
      {children}
    </ReactRouterLink>
  )
);

/**
 * External links open in a new browser tab with rel flags "noopener" and "noreferrer" set by default.
 *
 * @beta
 * @since 2.44.0
 */
export const ExternalLink = React.forwardRef<HTMLAnchorElement, AnchorHTMLAttributes<HTMLAnchorElement>>(
  ({ children, ...props }, ref) => (
    <a target="_blank" rel="noreferrer noopener" {...props} ref={ref}>
      {children}
    </a>
  )
);

type ExternalLinkButtonProps = BaseButtonProps & AnchorHTMLAttributes<HTMLAnchorElement>;

/**
 * Styled {@link ExternalLink}.
 *
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 * @see ExternalLink
 */
export const ExternalLinkButton = React.forwardRef<HTMLAnchorElement, ExternalLinkButtonProps>(
  ({ className, variant, isLoading, testId, children, ...props }, ref) => (
    <ExternalLink
      {...props}
      className={classNames(createButtonClasses(variant, isLoading), className)}
      ref={ref}
      {...createAttributesForTesting(testId)}
    >
      {children}
    </ExternalLink>
  )
);
