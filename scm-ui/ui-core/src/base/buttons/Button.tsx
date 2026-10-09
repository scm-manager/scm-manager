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
 * It inherits the surrounding text color and, on hover/active, only shifts the text color to the
 * primary hover/active colors without applying any background effect.
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
      color: var(--scm-primary-hover-color, var(--scm-primary-color, #00d1df));
    }
    &:active {
      color: var(--scm-primary-active-color, var(--scm-primary-color, #00d1df));
    }
  }
`;

/**
 * Bulma pulls an icon in front of the text to the left with a negative margin, so the space in front of the icon
 * would be smaller than the space behind the text. Resetting it keeps the padding on both sides the same.
 * Shared between the {@link Button}, {@link LinkButton} and {@link ExternalLinkButton} including their ghost variant.
 */
const iconStyles = css`
  && > .icon:first-child:not(:last-child) {
    margin-left: 0;
  }
`;

const ButtonElement = styled.button`
  ${iconStyles}
`;

const LinkElement = styled(ReactRouterLink)`
  ${iconStyles}
`;

const GhostButtonElement = styled.button`
  ${ghostStyles}
  ${iconStyles}
`;

const GhostLinkElement = styled(ReactRouterLink)`
  ${ghostStyles}
  ${iconStyles}
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
 * An {@link Icon} may complement the text, in which case the text has to be wrapped in a `span`.
 * Buttons without text use the {@link IconButton} instead.
 *
 * @beta
 * @since 2.41.0
 */
export const Button: FC<ButtonProps> = ({ className, variant, isLoading, testId, type, children, ref, ...props }) => {
  const Component = variant === "ghost" ? GhostButtonElement : ButtonElement;
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
 * An {@link Icon} may complement the text, in which case the text has to be wrapped in a `span`.
 * A button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 2.41.0
 */
export const LinkButton: FC<LinkButtonProps> = ({ className, variant, isLoading, testId, children, ref, ...props }) => {
  const Component = variant === "ghost" ? GhostLinkElement : LinkElement;
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

const ExternalLinkElement = styled(ExternalLink)`
  ${iconStyles}
`;

const GhostExternalLinkElement = styled(ExternalLink)`
  ${ghostStyles}
  ${iconStyles}
`;

type ExternalLinkButtonProps = BaseButtonProps &
  AnchorHTMLAttributes<HTMLAnchorElement> & { ref?: Ref<HTMLAnchorElement> };

/**
 * Styled {@link ExternalLink}.
 *
 * An {@link Icon} may complement the text, in which case the text has to be wrapped in a `span`.
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
  const Component = variant === "ghost" ? GhostExternalLinkElement : ExternalLinkElement;
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

/**
 * Shared between the medium {@link IconButton} and the {@link IconLinkButton}.
 */
const iconButtonStyles = css`
  border-radius: 6px !important;
  /* 62px, the width of the icon button in the theme library */
  min-width: 3.875rem;
  height: 2.5rem;
  padding: 0.5em;
  /* On hover, only the border of the Bulma button changes */
  &:not(:disabled):active {
    background-image: linear-gradient(
      color-mix(in srgb, currentColor 25%, transparent),
      color-mix(in srgb, currentColor 25%, transparent)
    );
  }
`;

const StyledIconButton = styled.button`
  ${iconButtonStyles}
`;

const StyledIconLinkButton = styled(ReactRouterLink)`
  ${iconButtonStyles}
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
    className: classNames(
      createIconButtonClasses(variant),
      { "has-background-transparent": size === "small" },
      className,
    ),
    ref: ref,
    ...createAttributesForTesting(testId),
  };
  return size === "small" ? (
    <StyledIconButtonCircle {...elementAttributes}>{children}</StyledIconButtonCircle>
  ) : (
    <StyledIconButton {...elementAttributes}>{children}</StyledIconButton>
  );
};

type IconLinkButtonProps = ReactRouterLinkProps & {
  testId?: string;
  variant?: IconButtonVariant;
  children: ReactNode;
  ref?: Ref<HTMLAnchorElement>;
};

/**
 * Styled react router link which looks like an {@link IconButton}.
 *
 * An icon link button has to declare an `aria-label`.
 *
 * @beta
 * @since 4.0.0
 */
export const IconLinkButton: FC<IconLinkButtonProps> = ({
  className,
  variant = "default",
  testId,
  ref,
  children,
  ...props
}) => (
  <StyledIconLinkButton
    {...props}
    className={classNames(createIconButtonClasses(variant), className)}
    ref={ref}
    {...createAttributesForTesting(testId)}
  >
    {children}
  </StyledIconLinkButton>
);

/**
 * Resets the styles browsers apply to native buttons (background, border, spacing and font),
 * so only the content of the {@link UnstyledButton} is visible.
 */
const UnstyledButtonElement = styled.button`
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  color: inherit;
  font: inherit;
  text-align: inherit;
  cursor: pointer;

  &:disabled {
    cursor: not-allowed;
  }
`;

type UnstyledButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  testId?: string;
  ref?: Ref<HTMLButtonElement>;
};

/**
 * Native html button without any styling of its own.
 *
 * Use it for clickable elements that must not look like a button, e.g. an icon or an entry of a list.
 * The focus indicator of the browser is kept, so the element stays recognizable for keyboard users.
 *
 * An unstyled button has to declare an `aria-label` if it exclusively contains an {@link Icon} as its children.
 *
 * @beta
 * @since 4.0.0
 */
export const UnstyledButton: FC<UnstyledButtonProps> = ({ type, testId, ref, children, ...props }) => (
  <UnstyledButtonElement type={type ?? "button"} {...props} ref={ref} {...createAttributesForTesting(testId)}>
    {children}
  </UnstyledButtonElement>
);
