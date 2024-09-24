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

import React, { FC } from "react";

const PRE_TAG = "<|[[--";
const POST_TAG = "--]]|>";

type Props = {
  value: string;
};

const HighlightedFragment: FC<Props> = ({ value }) => {
  let content = value;

  const result = [];
  while (content.length > 0) {
    const start = content.indexOf(PRE_TAG);
    const end = content.indexOf(POST_TAG);
    if (start >= 0 && end > 0) {
      if (start > 0) {
        result.push(content.substring(0, start));
      }
      result.push(<mark>{content.substring(start + PRE_TAG.length, end)}</mark>);
      content = content.substring(end + POST_TAG.length);
    } else {
      result.push(content);
      break;
    }
  }
  return (
    <>
      {result.map((c, i) => (
        <React.Fragment key={i}>{c}</React.Fragment>
      ))}
    </>
  );
};

export default HighlightedFragment;
