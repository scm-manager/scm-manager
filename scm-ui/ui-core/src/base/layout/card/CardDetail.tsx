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

import React, { ButtonHTMLAttributes, ComponentProps, FC, HTMLAttributes, ReactNode, Ref } from "react";
import classNames from "classnames";
import { useAriaId } from "../../helpers";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { Icon } from "../../buttons";

export const CardVariants = {
  LIGHT: "light",
  INFO: "info",
  SUCCESS: "success",
  DANGER: "danger",
  WARNING: "warning",
} as const;

export type CardVariant = (typeof CardVariants)[keyof typeof CardVariants];

const createCardVariantClasses = (variant?: string | undefined) =>
  classNames({
    "is-light": variant === "light" || !variant,
    "is-info": variant === "info",
    "is-success": variant === "success",
    "is-danger": variant === "danger",
    "is-warning": variant === "warning",
  });

type CardDetailProps = Omit<HTMLAttributes<HTMLSpanElement>, "children"> & {
  children: ReactNode | ((props: { labelId: string }) => ReactNode);
  ref?: Ref<HTMLSpanElement>;
};

type CardVariantProps = {
  cardVariant?: CardVariant;
};

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetail: FC<CardDetailProps> = ({ children, className, ref, ...props }) => {
  const labelId = useAriaId();
  return (
    <span {...props} className={classNames("is-flex is-align-items-center has-gap-1 p-1", className)} ref={ref}>
      {typeof children === "function" ? children({ labelId }) : children}
    </span>
  );
};

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

type CardButtonDetailProps = ButtonHTMLAttributes<HTMLButtonElement> & { ref?: Ref<HTMLButtonElement> };

/**
 * @beta
 * @since 2.47.0
 */
export const CardButtonDetail: FC<CardButtonDetailProps> = ({ children, className, ref, ...props }) => (
  <StyledCardButtonDetail
    {...props}
    className={classNames(
      "is-flex is-align-items-center has-gap-1 p-1 is-borderless has-background-transparent is-relative has-hover-background-blue has-rounded-border is-clickable",
      className,
    )}
    ref={ref}
  >
    {children}
  </StyledCardButtonDetail>
);

const StyledCardLinkDetail = styled(Link)`
  ${InteractiveDetailStyles};

  min-height: 2rem;
`;

/**
 * @beta
 * @since 2.47.0
 */
export const CardLinkDetail: FC<ComponentProps<typeof Link>> = ({ children, className, ref, ...props }) => (
  <StyledCardLinkDetail
    {...props}
    className={classNames(
      "is-flex is-align-items-center has-gap-1 p-1 is-borderless has-background-transparent is-relative has-hover-background-blue has-rounded-border is-clickable",
      className,
    )}
    ref={ref}
  >
    {children}
  </StyledCardLinkDetail>
);

type CardDetailsLabelProps = HTMLAttributes<HTMLSpanElement> & { ref?: Ref<HTMLSpanElement> };

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetailLabel: FC<CardDetailsLabelProps> = ({ children, className, ref, ...props }) => (
  <span {...props} className={classNames("has-text-secondary is-size-7", className)} ref={ref}>
    {children}
  </span>
);

type CardDetailTagProps = HTMLAttributes<HTMLSpanElement> & CardVariantProps & { ref?: Ref<HTMLSpanElement> };

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetailTag: FC<CardDetailTagProps> = ({ children, className, ref, ...props }) => (
  <span
    {...props}
    className={classNames("tag is-rounded", createCardVariantClasses(props.cardVariant), className)}
    ref={ref}
  >
    {children}
  </span>
);

/**
 * @beta
 * @since 2.47.0
 */
export const CardDetailIcon: FC<ComponentProps<typeof Icon>> = ({ children, className, ref, ...props }) => (
  <Icon {...props} className={classNames("is-size-5", className)} ref={ref}>
    {children}
  </Icon>
);

const CardDetailsRowContainer = styled.div`
  gap: 0.125rem 1rem;

  &:first-child {
    margin-left: -0.25rem;
  }
`;

type CardDetailsProps = HTMLAttributes<HTMLDivElement> & { ref?: Ref<HTMLDivElement> };

/**
 * @beta
 * @since 2.46.0
 */
export const CardDetails: FC<CardDetailsProps> = ({ children, className, ref, ...props }) => (
  <CardDetailsRowContainer
    {...props}
    className={classNames("is-flex is-flex-wrap-wrap is-align-items-center", className)}
    ref={ref}
  >
    {children}
  </CardDetailsRowContainer>
);
