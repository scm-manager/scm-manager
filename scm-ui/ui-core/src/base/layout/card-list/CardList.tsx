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

import React, { ComponentProps, FC, HTMLAttributes, Ref } from "react";
import classNames from "classnames";
import styled from "styled-components";
import Card from "../card/Card";

type CardListCardProps = Omit<ComponentProps<typeof Card>, "as">;

/**
 * @beta
 * @since 2.44.0
 * @see Card
 */
export const CardListCard: FC<CardListCardProps> = ({ ref, ...props }) => <Card ref={ref} {...props} as="li" />;

const CardListElement = styled.ul`
  > * + * {
    margin-top: calc(0.5rem + 1px);

    &::before {
      content: "";
      position: absolute;
      width: 100%;
      border-top: var(--scm-border);
      left: 0;
      top: calc(-0.25rem - 1px);
    }
  }
`;

type Props = HTMLAttributes<HTMLUListElement> & { ref?: Ref<HTMLUListElement> };

/**
 * @beta
 * @since 2.44.0
 */
const CardList: FC<Props> = ({ children, className, ref, ...props }) => (
  <CardListElement ref={ref} {...props} className={classNames(className, "is-flex", "is-flex-direction-column")}>
    {children}
  </CardListElement>
);

/**
 * @beta
 * @since 2.44.0
 */
export const CardListBox: FC<Props> = ({ className, children, ref, ...props }) => (
  <CardList className={classNames(className, "p-2 box")} ref={ref} {...props}>
    {children}
  </CardList>
);

export default CardList;
