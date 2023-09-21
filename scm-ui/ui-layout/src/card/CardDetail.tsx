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

import React, {
  ButtonHTMLAttributes,
  ComponentProps,
  ComponentPropsWithoutRef,
  HTMLAttributes,
  ReactNode,
} from "react";
import classNames from "classnames";
import { useGeneratedId } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { Icon } from "@scm-manager/ui-buttons";

export const CardVariants = {
  LIGHT: "light",
  INFO: "info",
} as const;

type CardVariant = typeof CardVariants[keyof typeof CardVariants];

const createCardVariantClasses = (variant?: string | undefined) =>
  classNames({
    "is-light": variant === "light" || !variant,
    "is-info": variant === "info",
  });

type CardDetailProps = HTMLAttributes<HTMLSpanElement> & {
  children: ReactNode | ((props: { labelId: string }) => ReactNode);
};

type CardVariantProps = {
  cardVariant?: CardVariant;
};

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetail = React.forwardRef<HTMLSpanElement, CardDetailProps>(
  ({ children, className, ...props }, ref) => {
    const labelId = useGeneratedId();
    return (
      <span {...props} className={classNames("is-flex is-align-items-center has-gap-1 p-1", className)} ref={ref}>
        {typeof children === "function" ? children({ labelId }) : children}
      </span>
    );
  }
);

const InteractiveDetailStyles = `
  &:focus {
    outline-offset: -0.125rem;
    outline-width: 0.125rem;
  }
`;

const StyledCardButtonDetail = styled.button`
  ${InteractiveDetailStyles}

  &[aria-expanded="true"] {
    outline: var(--scm-border-color) solid 0.125rem;
    outline-offset: -0.125rem;
  }
`;

/**
 * @beta
 * @since 2.47.0
 */
export const CardButtonDetail = React.forwardRef<HTMLButtonElement, ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ children, className, ...props }, ref) => (
    <StyledCardButtonDetail
      {...props}
      className={classNames(
        "is-flex is-align-items-center has-gap-1 p-1 is-borderless has-background-transparent is-relative has-hover-background-blue has-rounded-border is-clickable",
        className
      )}
      ref={ref}
    >
      {children}
    </StyledCardButtonDetail>
  )
);

const StyledCardLinkDetail = styled(Link)`
  ${InteractiveDetailStyles}

  min-height: 2rem;
`;

/**
 * @beta
 * @since 2.47.0
 */
export const CardLinkDetail = React.forwardRef<HTMLAnchorElement, ComponentPropsWithoutRef<typeof Link>>(
  ({ children, className, ...props }, ref) => (
    <StyledCardLinkDetail
      {...props}
      className={classNames(
        "is-flex is-align-items-center has-gap-1 p-1 is-borderless has-background-transparent is-relative has-hover-background-blue has-rounded-border is-clickable",
        className
      )}
      ref={ref}
    >
      {children}
    </StyledCardLinkDetail>
  )
);

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetailLabel = React.forwardRef<HTMLSpanElement, HTMLAttributes<HTMLSpanElement>>(
  ({ children, className, ...props }, ref) => (
    <span {...props} className={classNames("has-text-secondary is-size-7", className)} ref={ref}>
      {children}
    </span>
  )
);

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetailTag = React.forwardRef<HTMLSpanElement, HTMLAttributes<HTMLSpanElement> & CardVariantProps>(
  ({ children, className, ...props }, ref) => (
    <span
      {...props}
      className={classNames("tag is-rounded", createCardVariantClasses(props.cardVariant), className)}
      ref={ref}
    >
      {children}
    </span>
  )
);

/**
 * @beta
 * @since 2.47.0
 */
export const CardDetailIcon = React.forwardRef<HTMLElement, ComponentProps<typeof Icon>>(
  ({ children, className, ...props }, ref) => (
    <Icon {...props} className={classNames("is-size-5", className)} ref={ref}>
      {children}
    </Icon>
  )
);

const CardDetailsRowContainer = styled.div`
  gap: 0.125rem 1rem;

  &:first-child {
    margin-left: -0.25rem;
  }
`;

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetails = React.forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ children, className, ...props }, ref) => (
    <CardDetailsRowContainer
      {...props}
      className={classNames("is-flex is-flex-wrap-wrap is-align-items-center", className)}
      ref={ref}
    >
      {children}
    </CardDetailsRowContainer>
  )
);
