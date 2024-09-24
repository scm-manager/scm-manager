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
import { Changeset } from "@scm-manager/ui-types";
import { extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import { Replacement, SplitAndReplace } from "@scm-manager/ui-text";

type Props = {
  changeset: Changeset;
  value: string;
};

const ChangesetDescription: FC<Props> = ({ changeset, value }) => {
  const binder = useBinder();

  const replacements: ((changeset: Changeset, value: string) => Replacement[])[] =
    binder.getExtensions<extensionPoints.ChangesetDescriptionTokens>("changeset.description.tokens", {
      changeset,
      value,
    });

  return <SplitAndReplace text={value} replacements={replacements.flatMap((r) => r(changeset, value))} />;
};

export default ChangesetDescription;
