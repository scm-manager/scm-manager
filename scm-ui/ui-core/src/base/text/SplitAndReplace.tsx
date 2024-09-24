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

import React, { FC, ReactNode } from "react";
import textSplitAndReplace from "./textSplitAndReplace";

export type Replacement = {
  textToReplace: string;
  replacement: ReactNode;
  replaceAll?: boolean;
};

export type Props = {
  text: string;
  replacements: Replacement[];
  textWrapper?: (s: string) => ReactNode;
};

const defaultTextWrapper = (s: string) => {
  const first = s.startsWith(" ") ? <>&nbsp;</> : "";
  const last = s.endsWith(" ") ? <>&nbsp;</> : "";
  return (
    <>
      {first}
      {s}
      {last}
    </>
  );
};

const SplitAndReplace: FC<Props> = ({ text, replacements, textWrapper = defaultTextWrapper }) => {
  const parts = textSplitAndReplace<ReactNode>(text, replacements, textWrapper);
  if (parts.length === 0) {
    return <>{parts[0]}</>;
  }
  return (
    <>
      {parts.map((part, index) => (
        <React.Fragment key={index}>{part}</React.Fragment>
      ))}
    </>
  );
};

export default SplitAndReplace;
