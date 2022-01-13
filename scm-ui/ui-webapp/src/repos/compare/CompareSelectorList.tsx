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
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { useBranches, useTags } from "@scm-manager/ui-api";
import { Branch, Repository, Tag } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, NoStyleButton, Notification } from "@scm-manager/ui-components";
import DefaultBranchTag from "../branches/components/DefaultBranchTag";
import CompareSelectorListEntry from "./CompareSelectorListEntry";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";

type Props = {
  onSelect: CompareFunction;
  selected: CompareProps;
  repository: Repository;
  filter: string;
};

type TabContentBranchProps = {
  elements: Branch[];
  selection: CompareProps;
  onSelectEntry: CompareFunction;
};

type TabContentTagProps = {
  elements: Tag[];
  selection: CompareProps;
  onSelectEntry: CompareFunction;
};

type AvailableTabs = "branches" | "tags" | "revision";

const TabStyleButton = styled(NoStyleButton)`
  align-items: center;
  border-bottom-color: #dbdbdb;
  border-bottom-style: solid;
  border-bottom-width: 1px;
  color: #4a4a4a;
  display: flex;
  justify-content: center;
  margin-bottom: -1px;
  padding: 0.5em 1em;
  vertical-align: top;

  &:hover {
    border-bottom-color: #363636;
    color: #363636;
  }

  &.is-active {
    border-bottom-color: #33b2e8;
    color: #33b2e8;
  }

  &:focus {
    background-color: var(--scm-column-selection);
  }
`;

const ScrollableUl = styled.ul`
  max-height: 250px;
  width: 300px;
  overflow-x: hidden;
  overflow-y: scroll;
`;

const SizedDiv = styled.div`
  width: 300px;
`;

const BranchTabContent: FC<TabContentBranchProps> = ({ elements, selection, onSelectEntry }) => {
  const [t] = useTranslation("repos");

  if (elements.length === 0) {
    return (
      <Notification className="m-4" type="info">
        {t("compare.selector.emptyResult")}
      </Notification>
    );
  }

  return (
    <>
      {elements.map((branch, index) => {
        return (
          <CompareSelectorListEntry
            isSelected={selection.type === "b" && selection.name === branch.name}
            onClick={() => onSelectEntry("b", branch.name)}
            key={index}
          >
            <span className="is-ellipsis-overflow">{branch.name}</span>
            <DefaultBranchTag className="ml-2" defaultBranch={branch.defaultBranch} />
          </CompareSelectorListEntry>
        );
      })}
    </>
  );
};

const TagTabContent: FC<TabContentTagProps> = ({ elements, selection, onSelectEntry }) => {
  const [t] = useTranslation("repos");

  if (elements.length === 0) {
    return (
      <Notification className="m-4" type="info">
        {t("compare.selector.emptyResult")}
      </Notification>
    );
  }

  return (
    <>
      {elements.map((tag, index) => {
        return (
          <CompareSelectorListEntry
            isSelected={selection.type === "t" && selection.name === tag.name}
            onClick={() => onSelectEntry("t", tag.name)}
            key={index}
          >
            <span className="is-ellipsis-overflow">{tag.name}</span>
          </CompareSelectorListEntry>
        );
      })}
    </>
  );
};

const ScrollableList: FC<{ selectedTab: AvailableTabs } & Props> = ({
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

  if (selectedTab !== "revision") {
    return (
      <ScrollableUl aria-expanded="true" role="listbox">
        {selectedTab === "branches" && (
          <BranchTabContent
            elements={branches.filter(branch => branch.name.includes(filter))}
            selection={selection}
            onSelectEntry={onSelectEntry}
          />
        )}
        {selectedTab === "tags" && (
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
  const [selectedTab, setSelectedTab] = useState<AvailableTabs>("branches");
  const tabs: AvailableTabs[] = ["branches", "tags"]; // TODO add revision tab

  return (
    <>
      <div className="tabs is-small mt-3 mb-0">
        <ul>
          {tabs.map((tab, index) => {
            return (
              <li key={index}>
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
      {selectedTab === "revision" && (
        <SizedDiv className="mt-2">
          <input className="input is-small" placeholder={t("compare.selector.revision")} />
        </SizedDiv>
      )}
    </>
  );
};

export default CompareSelectorList;
