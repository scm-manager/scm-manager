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

import React, { HTMLAttributes, useState } from "react";
import { Icon } from "../../buttons";

interface Props extends HTMLAttributes<HTMLParagraphElement> {
  descriptionText: string;
  extendedDescriptionText: string;
}

/**
 * Displays a single line of text by default and allows users to expand it to view the full content.
 *
 * The description text is shown initially
 * with an icon to toggle the visibility of the extended text.
 *
 * @param desription - The text content that will always be displayed.
 * @param extended - The text content that will be displayed after you toggle the icon.
 * @param props - Additional props to pass to the `<p>` element.
 * @param ref - A ref to the `<p>` element.
 *
 * @beta
 * @since 3.9.0
 */
const ExpandableText = React.forwardRef<HTMLParagraphElement, Props>(
  ({ descriptionText, extendedDescriptionText, ...props }, ref) => {
    const [isExpanded, setIsExpanded] = useState(false);

    return (
      <p {...props} ref={ref} className="is-flex is-flex-direction-column mb-2">
        <div>
          <Icon className="is-clickable" onClick={() => setIsExpanded(!isExpanded)} aria-expanded={isExpanded}>
            {isExpanded ? "chevron-down" : "chevron-right"}
          </Icon>
          {descriptionText}
        </div>
        <>{isExpanded && <span className="mt-1">{extendedDescriptionText}</span>}</>
      </p>
    );
  }
);

export default ExpandableText;
