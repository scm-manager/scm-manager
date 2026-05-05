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

type CardRowProps = HTMLAttributes<HTMLDivElement> & { ref?: Ref<HTMLDivElement> };

/**
 * @beta
 * @since 2.44.0
 */
const CardRow: FC<CardRowProps> = ({ className, ref, ...props }) => (
  <div className={classNames(className, "ml-1")} {...props} ref={ref} />
);

export default CardRow;

export const SecondaryRow: FC<ComponentProps<typeof CardRow>> = ({ className, ref, ...props }) => (
  <CardRow className={classNames(className, "is-size-7")} {...props} ref={ref} />
);

export const TertiaryRow: FC<ComponentProps<typeof CardRow>> = ({ className, ref, ...props }) => (
  <CardRow className={classNames(className, "is-size-7", "has-text-secondary")} {...props} ref={ref} />
);
