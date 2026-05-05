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
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { Loading, ErrorNotification, Notification } from "@scm-manager/ui-core";
import { Repository, Changeset } from "@scm-manager/ui-types";
import CompareSelectorListEntry from "./CompareSelectorListEntry";
import { CompareProps, CompareFunction } from "./CompareSelectBar";
import { useLookupRevision } from "@scm-manager/ui-api";

type Props = {
  selected: CompareProps;
  onSelect: CompareFunction;
  repository: Repository;
  filter: string;
};

const Container = styled.div`
  width: 100%;
`;

const FixedWidthNotification = styled(Notification)`
  width: 100%;
  margin-top: 0.5rem;
`;

const ScrollableUl = styled.ul`
  max-height: 15.65rem;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  margin-top: 0.5rem;
`;

const RevisionTab: FC<Props> = ({ selected, onSelect, repository, filter }) => {
  const [t] = useTranslation("repos");

  const { isLoading, error, data } = useLookupRevision(repository, { prefix: filter });

  const changesets: Changeset[] = (data?._embedded?.changesets as Changeset[]) || [];

  const onSelectEntry = (name: string) => {
    onSelect("revision", name);
  };

  const renderContent = () => {
    if (filter.length < 3) {
      return <FixedWidthNotification type="info">{t("compare.selector.revision.input")}</FixedWidthNotification>;
    }

    if (isLoading) {
      return <Loading />;
    }

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (changesets.length === 0) {
      return <FixedWidthNotification type="info">{t("compare.selector.emptyResult")}</FixedWidthNotification>;
    }

    return (
      <ScrollableUl className="py-2 pr-2" role="listbox">
        {changesets.map((changeset) => (
          <CompareSelectorListEntry
            isSelected={selected.type === "revision" && selected.name === changeset.id}
            onClick={() => onSelectEntry(changeset.id)}
            key={changeset.id}
          >
            <div className="is-flex is-flex-direction-column is-overflow-hidden">
              <span className="has-text-weight-bold is-family-monospace">{changeset.id}</span>
              <span className="is-ellipsis-overflow is-size-7">{changeset.description}</span>
            </div>
          </CompareSelectorListEntry>
        ))}
      </ScrollableUl>
    );
  };

  return <Container className="mt-2">{renderContent()}</Container>;
};

export default RevisionTab;
