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

import React, { AnchorHTMLAttributes, ButtonHTMLAttributes, FC, ReactNode, Ref } from "react";
import { Link as ReactRouterLink, LinkProps as ReactRouterLinkProps } from "react-router";
import classNames from "classnames";
import { createAttributesForTesting } from "../helpers";
import styled, { css } from "styled-components";

/**
 * @beta
 * @since 2.41.0
 */
export const ButtonVariants = {
  PRIMARY: "primary",
  SECONDARY: "secondary",
  TERTIARY: "tertiary",
  SIGNAL: "signal",
  DANGER: "danger",
  INFO: "info",
  GHOST: "ghost",
} as const;

export const ButtonVariantList = Object.values(ButtonVariants);

type ButtonVariant = (typeof ButtonVariants)[keyof typeof ButtonVariants];

/**
 * Borderless, transparent button styling that blends into its surroundings ("ghost button").
 * It inherits the surrounding text color and only reveals a subtle background on hover/active.
 * Shared between the {@link Button}, {@link LinkButton} and {@link ExternalLinkButton} ghost variant.
 */
const ghostStyles = css`
  border: none;
  background-color: transparent;
  border-radius: 6px;
  height: auto;
  padding: 0.5em 0.75em;
  color: inherit;
  &:not(:disabled) {
    &:hover {
      color: inherit;
      background-color: color-mix(in srgb, currentColor 10%, transparent);
    }
    &:active {
      background-color: color-mix(in srgb, currentColor 25%, transparent);
    }
  }
`;

const GhostButtonElement = styled.button`
  ${ghostStyles}
`;

const GhostLinkElement = styled(ReactRouterLink)`
  ${ghostStyles}
`;

const createButtonClasses = (variant?: ButtonVariant, isLoading?: boolean) =>
  classNames("button", {
    "is-primary": variant === "primary",
    "is-primary is-outlined": variant === "secondary",
    "is-primary is-inverted": variant === "tertiary",
    "is-warning": variant === "signal",
    "is-info is-outlined": variant === "info",
    "is-danger": variant === "danger",
    "is-loading": isLoading,
  });

type BaseButtonProps = {
  variant?: ButtonVariant;
  isLoading?: boolean;
  testId?: string;
};

type ButtonProps = BaseButtonProps &
  ButtonHTMLAttributes<HTMLButtonElement> & {
    ref?: Ref<HTMLButtonElement>;
  };

/**
 * Styled html button.
 *
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 */
export const Button: FC<ButtonProps> = ({ className, variant, isLoading, testId, type, children, ref, ...props }) => {
  const Component = variant === "ghost" ? GhostButtonElement : "button";
  return (
    <Component
      type={type ?? "button"}
      {...props}
      className={classNames(createButtonClasses(variant, isLoading), className)}
      ref={ref}
      {...createAttributesForTesting(testId)}
    >
      {children}
    </Component>
  );
};

type LinkButtonProps = BaseButtonProps & ReactRouterLinkProps & { ref?: Ref<HTMLAnchorElement> };

/**
 * Styled react router link.
 *
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 */
export const LinkButton: FC<LinkButtonProps> = ({ className, variant, isLoading, testId, children, ref, ...props }) => {
  const Component = variant === "ghost" ? GhostLinkElement : ReactRouterLink;
  return (
    <Component
      {...props}
      className={classNames(createButtonClasses(variant, isLoading), className)}
      ref={ref}
      {...createAttributesForTesting(testId)}
    >
      {children}
    </Component>
  );
};

type ExternalLinkProps = AnchorHTMLAttributes<HTMLAnchorElement> & { ref?: Ref<HTMLAnchorElement> };

/**
 * External links open in a new browser tab with rel flags "noopener" and "noreferrer" set by default.
 *
 * @beta
 * @since 2.44.0
 */
export const ExternalLink: FC<ExternalLinkProps> = ({ children, ref, ...props }) => (
  <a target="_blank" rel="noreferrer noopener" {...props} ref={ref}>
    {children}
  </a>
);

const GhostExternalLinkElement = styled(ExternalLink)`
  ${ghostStyles}
`;

type ExternalLinkButtonProps = BaseButtonProps &
  AnchorHTMLAttributes<HTMLAnchorElement> & { ref?: Ref<HTMLAnchorElement> };

/**
 * Styled {@link ExternalLink}.
 *
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 * @see ExternalLink
 */
export const ExternalLinkButton: FC<ExternalLinkButtonProps> = ({
  className,
  variant,
  isLoading,
  testId,
  children,
  ref,
  ...props
}) => {
  const Component = variant === "ghost" ? GhostExternalLinkElement : ExternalLink;
  return (
    <Component
      {...props}
      className={classNames(createButtonClasses(variant, isLoading), className)}
      ref={ref}
      {...createAttributesForTesting(testId)}
    >
      {children}
    </Component>
  );
};

const StyledIconButton = styled.button`
  border-radius: 6px !important;
  min-width: 2.5rem;
  height: 2.5rem;
  padding: 0.5em;
  &:not(:disabled) {
    &:hover {
      background-color: color-mix(in srgb, currentColor 10%, transparent);
    }
    &:active {
      background-color: color-mix(in srgb, currentColor 25%, transparent);
    }
  }
`;

const StyledIconButtonCircle = styled.button`
  border-radius: 50% !important;
  border: None;
  min-width: 1.5rem;
  height: 1.5rem;
  padding: 0.5em;
  &:not(:disabled) {
    &:hover {
      background-color: color-mix(in srgb, currentColor 10%, transparent);
    }
    &:active {
      background-color: color-mix(in srgb, currentColor 25%, transparent);
    }
  }
`;

export const IconButtonVariants = {
  COLORED: "colored",
  DEFAULT: "default",
} as const;

export const IconSizes = {
  SMALL: "small",
  MEDIUM: "medium",
} as const;

type IconButtonSize = (typeof IconSizes)[keyof typeof IconSizes];

type IconButtonVariant = (typeof IconButtonVariants)[keyof typeof IconButtonVariants];

const createIconButtonClasses = (variant?: IconButtonVariant) =>
  classNames("button", {
    "is-primary is-outlined": variant === "colored",
  });

type IconButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  testId?: string;
  variant?: IconButtonVariant;
  children: ReactNode;
  size?: IconButtonSize;
  ref?: Ref<HTMLButtonElement>;
};

/**
 * Styled html button.
 *
 * An icon button has to declare an `aria-label`.
 *
 * @beta
 * @since 3.2.0
 */
export const IconButton: FC<IconButtonProps> = ({
  className,
  variant = "default",
  size = "medium",
  testId,
  type,
  ref,
  children,
  ...props
}) => {
  const elementAttributes = {
    type: type ?? "button",
    ...props,
    className: classNames(createIconButtonClasses(variant), "button", "has-background-transparent"),
    ref: ref,
    ...createAttributesForTesting(testId),
  };
  return size === "small" ? (
    <StyledIconButtonCircle {...elementAttributes}>{children}</StyledIconButtonCircle>
  ) : (
    <StyledIconButton {...elementAttributes}>{children}</StyledIconButton>
  );
};
