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

import React, { HTMLAttributes, FC, Ref } from "react";

interface Props extends HTMLAttributes<HTMLParagraphElement> {
  descriptionText: string;
  extendedDescriptionText: string;
  id?: string;
  ref?: Ref<HTMLDetailsElement>;
}

/**
 * Displays a single line of text by default and allows users to expand it to view the full content.
 *
 * The description text is shown initially
 * with an icon to toggle the visibility of the extended text.
 *
 * @param desription - The text content that will always be displayed.
 * @param extendedDescriptionText - The text content that will be displayed after you toggle details.
 * @param id - Additional props to pass to the `<details>` element.
 * @param ref - A ref to the `<details>` element.
 *
 * @beta
 * @since 3.9.0
 */
const ExpandableText: FC<Props> = ({ descriptionText, extendedDescriptionText, id, ref }) => {
  return (
    <details className="mb-2" id={id} ref={ref}>
      <summary>{descriptionText}</summary>
      <span>{extendedDescriptionText}</span>
    </details>
  );
};

export default ExpandableText;
