/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, { FC, useState } from "react";
import CompareSelectorListEntry from "./CompareSelectorListEntry";
import { Repository, Tag } from "@scm-manager/ui-types";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";
import { useTags } from "@scm-manager/ui-api";
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

const TagTab: FC<Props> = ({ onSelect, selected, repository, filter }) => {
  const [t] = useTranslation("repos");
  const { isLoading: tagsIsLoading, error: tagsError, data: tagsData } = useTags(repository);
  const tags: Tag[] = (tagsData?._embedded?.tags as Tag[]) || [];

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
          isSelected={selection.type === "t" && selection.name === tag.name}
          onClick={() => onSelectEntry("t", tag.name)}
          key={tag.name}
        >
          <span className="is-ellipsis-overflow">{tag.name}</span>
        </CompareSelectorListEntry>
      ))}
    </ScrollableUl>
  );
};

export default TagTab;
