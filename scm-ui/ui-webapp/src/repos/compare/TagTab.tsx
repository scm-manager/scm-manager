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
import { Tag } from "@scm-manager/ui-types";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

type Props = {
  tags: Tag[];
  tagsIsLoading: boolean;
  tagsError: Error | null;
  onSelect: CompareFunction;
  selected: CompareProps;
  filter: string;
};

const FixedWidthNotification = styled(Notification)`
  width: 100%;
  margin-top: 0.5rem;
`;

const ScrollableUl = styled.ul`
  max-height: 15.65rem;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
`;

const TagTab: FC<Props> = ({ tags, tagsIsLoading, tagsError, onSelect, selected, filter }) => {
  const [t] = useTranslation("repos");

  const [selection, setSelection] = useState(selected);

  const onSelectEntry = (type: CompareTypes, name: string) => {
    setSelection({ type, name });
    onSelect(type, name);
  };

  if (tagsIsLoading) {
    return <Loading />;
  }
  if (tagsError) {
    return <ErrorNotification error={tagsError} />;
  }

  const elements = tags.filter((tag) => tag.name.includes(filter));

  if (elements.length === 0) {
    return <FixedWidthNotification>{t("compare.selector.emptyResult")}</FixedWidthNotification>;
  }

  return (
    <ScrollableUl className="py-2 pr-2" role="listbox">
      {elements.map((tag) => (
        <CompareSelectorListEntry
          isSelected={selection.type === "tag" && selection.name === tag.name}
          onClick={() => onSelectEntry("tag", tag.name)}
          key={tag.name}
        >
          <span className="is-ellipsis-overflow">{tag.name}</span>
        </CompareSelectorListEntry>
      ))}
    </ScrollableUl>
  );
};

export default TagTab;
