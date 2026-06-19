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
import { useSubpageContext } from "./SubpageContext";

type Props = {
  children?: ReactNode;
};

/**
 * Container for the read-only overview of a `SubpageForm`. Its content (e.g. read-only tables and
 * `Subpage.OpenButton` edit buttons) is only rendered while no subpage is active.
 *
 * @beta
 * @since 4.0.0
 */
const SubpageContainer: FC<Props> = ({ children }) => {
  const { activeSubpage } = useSubpageContext();

  if (activeSubpage !== null) {
    return null;
  }

  return <>{children}</>;
};

export default SubpageContainer;
