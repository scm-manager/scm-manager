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

import React, { FC, useState } from "react";
import CompareSelectorListEntry from "./CompareSelectorListEntry";
import DefaultBranchTag from "../branches/components/DefaultBranchTag";
import { Branch, Repository } from "@scm-manager/ui-types";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";
import { useBranches } from "@scm-manager/ui-api";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

type Props = {
  onSelect: CompareFunction;
  selected: CompareProps;
  repository: Repository;
  filter: string;
};

const FixedWidthNotification = styled(Notification)`
  width: 18.5rem;
  margin-top: 0.5rem;
`;

const ScrollableUl = styled.ul`
  max-height: 15.65rem;
  width: 18.5rem;
  overflow-x: hidden;
  overflow-y: auto;
`;

const BranchTab: FC<Props> = ({ onSelect, selected, repository, filter }) => {
  const [t] = useTranslation("repos");
  const { isLoading: branchesIsLoading, error: branchesError, data: branchesData } = useBranches(repository);
  const branches: Branch[] = (branchesData?._embedded?.branches as Branch[]) || [];

  const [selection, setSelection] = useState(selected);

  const onSelectEntry = (type: CompareTypes, name: string) => {
    setSelection({ type, name });
    onSelect(type, name);
  };

  if (branchesIsLoading) {
    return <Loading />;
  }
  if (branchesError) {
    return <ErrorNotification error={branchesError} />;
  }

  const elements = branches.filter((branch) => branch.name.includes(filter));

  if (elements.length === 0) {
    return <FixedWidthNotification>{t("compare.selector.emptyResult")}</FixedWidthNotification>;
  }

  return (
    <ScrollableUl className="py-2 pr-2" role="listbox">
      {elements.map((branch) => {
        return (
          <CompareSelectorListEntry
            isSelected={selection.type === "b" && selection.name === branch.name}
            onClick={() => onSelectEntry("b", branch.name)}
            key={branch.name}
          >
            <span className="is-ellipsis-overflow">{branch.name}</span>
            <DefaultBranchTag className="ml-2" defaultBranch={branch.defaultBranch} />
          </CompareSelectorListEntry>
        );
      })}
    </ScrollableUl>
  );
};

export default BranchTab;
