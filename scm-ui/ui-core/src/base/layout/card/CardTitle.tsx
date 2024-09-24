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

import React, { HTMLAttributes } from "react";

type Props = HTMLAttributes<HTMLHeadingElement> & {
  /**
   * @default 3
   */
  level?: number;
};

/**
 * A card title may contain a link as its only child which will be automatically stretched to cover the whole card area.
 *
 * If a card title has a link, individual card elements which should be interactive have to get the `is-relative` class.
 *
 * The card title (or enclosed link) content must be an accessible text and must not contain any other interactive elements.
 *
 * You can wrap the title in a {@link CardList.Card.Row} to introduce other elements next to the title.
 *
 * The title (or its enclosing row) must be the first element in a {@link CardList.Card}.
 *
 * @beta
 * @since 2.44.0
 */
const CardTitle = React.forwardRef<HTMLHeadingElement, Props>(({ children, level = 3, ...props }, ref) =>
  React.createElement(
    `h${level}`,
    {
      ref,
      ...props,
    },
    children
  )
);

export default CardTitle;
