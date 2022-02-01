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

import React, { FC, KeyboardEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Branch, Repository, Tag } from "@scm-manager/ui-types";
import { useBranches, useTags } from "@scm-manager/ui-api";
import { Button, ErrorNotification, Loading, NoStyleButton, Notification } from "@scm-manager/ui-components";
import DefaultBranchTag from "../branches/components/DefaultBranchTag";
import CompareSelectorListEntry from "./CompareSelectorListEntry";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";

type Props = {
  onSelect: CompareFunction;
  selected: CompareProps;
  repository: Repository;
  filter: string;
};

const TabStyleButton = styled(NoStyleButton)`
  align-items: center;
  border-bottom: var(--scm-border);
  color: var(--scm-secondary-text);
  display: flex;
  justify-content: center;
  margin-bottom: -1px;
  padding: 0.5rem 1rem;
  vertical-align: top;

  &:hover {
    border-bottom-color: var(--scm-hover-color);
    color: var(--scm-hover-color);
  }

  &.is-active {
    border-bottom-color: var(--scm-info-color);
    color: var(--scm-info-color);
  }

  &:focus-visible {
    background-color: var(--scm-column-selection);
  }
`;

const ScrollableUl = styled.ul`
  max-height: 15.65rem;
  width: 18.5rem;
  overflow-x: hidden;
  overflow-y: scroll;
`;

const SizedDiv = styled.div`
  width: 18.5rem;
`;

const SmallButton = styled(Button)`
  height: 1.875rem;
`;

type BranchTabContentProps = {
  elements: Branch[];
  selection: CompareProps;
  onSelectEntry: CompareFunction;
};

const EmptyResultNotification: FC = () => {
  const [t] = useTranslation("repos");

  return <Notification type="info">{t("compare.selector.emptyResult")}</Notification>;
};

const BranchTabContent: FC<BranchTabContentProps> = ({ elements, selection, onSelectEntry }) => {
  if (elements.length === 0) {
    return <EmptyResultNotification />;
  }

  return (
    <>
      {elements.map(branch => {
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
    </>
  );
};

type TagTabContentProps = {
  elements: Tag[];
  selection: CompareProps;
  onSelectEntry: CompareFunction;
};

const TagTabContent: FC<TagTabContentProps> = ({ elements, selection, onSelectEntry }) => {
  if (elements.length === 0) {
    return <EmptyResultNotification />;
  }

  return (
    <>
      {elements.map(tag => (
        <CompareSelectorListEntry
          isSelected={selection.type === "t" && selection.name === tag.name}
          onClick={() => onSelectEntry("t", tag.name)}
          key={tag.name}
        >
          <span className="is-ellipsis-overflow">{tag.name}</span>
        </CompareSelectorListEntry>
      ))}
    </>
  );
};

type RevisionTabContentProps = {
  selected: CompareProps;
  onSelect: CompareFunction;
};

const RevisionTabContent: FC<RevisionTabContentProps> = ({ selected, onSelect }) => {
  const [t] = useTranslation("repos");
  const defaultValue = selected.type === "r" ? selected.name : "";
  const [revision, setRevision] = useState(defaultValue);

  const handleKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      event.preventDefault();
      handleSubmit();
    }
  };

  const handleSubmit = () => {
    if (revision) {
      onSelect("r", revision);
    }
  };

  return (
    <SizedDiv className="mt-2">
      <div className="field has-addons is-justify-content-center">
        <div className="control">
          <input
            className="input is-small"
            placeholder={t("compare.selector.revision.input")}
            onChange={e => setRevision(e.target.value.trim())}
            onKeyPress={handleKeyPress}
            value={revision.trim()}
          />
        </div>
        <div className="control">
          <SmallButton className="is-info is-small" action={handleSubmit} disabled={!revision}>
            {t("compare.selector.revision.submit")}
          </SmallButton>
        </div>
      </div>
    </SizedDiv>
  );
};

const ScrollableList: FC<{ selectedTab: CompareTypes } & Props> = ({
  selectedTab,
  onSelect,
  selected,
  repository,
  filter
}) => {
  const { isLoading: branchesIsLoading, error: branchesError, data: branchesData } = useBranches(repository);
  const branches: Branch[] = (branchesData?._embedded?.branches as Branch[]) || [];
  const { isLoading: tagsIsLoading, error: tagsError, data: tagsData } = useTags(repository);
  const tags: Tag[] = (tagsData?._embedded?.tags as Tag[]) || [];
  const [selection, setSelection] = useState(selected);

  const onSelectEntry = (type: CompareTypes, name: string) => {
    setSelection({ type, name });
    onSelect(type, name);
  };

  if (branchesIsLoading || tagsIsLoading) {
    return <Loading />;
  }
  if (branchesError || tagsError) {
    return <ErrorNotification error={branchesError || tagsError} />;
  }

  if (selectedTab !== "r") {
    return (
      <ScrollableUl className="py-2 pr-2" aria-expanded="true" role="listbox">
        {selectedTab === "b" && (
          <BranchTabContent
            elements={branches.filter(branch => branch.name.includes(filter))}
            selection={selection}
            onSelectEntry={onSelectEntry}
          />
        )}
        {selectedTab === "t" && (
          <TagTabContent
            elements={tags.filter(tag => tag.name.includes(filter))}
            selection={selection}
            onSelectEntry={onSelectEntry}
          />
        )}
      </ScrollableUl>
    );
  }
  return null;
};

const CompareSelectorList: FC<Props> = ({ onSelect, selected, repository, filter }) => {
  const [t] = useTranslation("repos");
  const [selectedTab, setSelectedTab] = useState<CompareTypes>(selected.type);
  const tabs: CompareTypes[] = ["b", "t", "r"];

  return (
    <>
      <div className="tabs is-small mt-3 mb-0">
        <ul>
          {tabs.map(tab => {
            return (
              <li key={tab}>
                <TabStyleButton
                  className={classNames({ "is-active": selectedTab === tab })}
                  onClick={() => setSelectedTab(tab)}
                >
                  {t("compare.selector.tabs." + tab)}
                </TabStyleButton>
              </li>
            );
          })}
        </ul>
      </div>
      <ScrollableList
        selectedTab={selectedTab}
        onSelect={onSelect}
        selected={selected}
        repository={repository}
        filter={filter}
      />
      {selectedTab === "r" && <RevisionTabContent onSelect={onSelect} selected={selected} />}
    </>
  );
};

export default CompareSelectorList;
