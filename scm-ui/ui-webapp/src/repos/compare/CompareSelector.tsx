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

import React, { FC, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { devices } from "@scm-manager/ui-components";
import { Icon, Tabs } from "@scm-manager/ui-core";
import { Branch, Repository, Tag } from "@scm-manager/ui-types";
import BranchTab from "./BranchTab";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";
import RevisionTab from "./RevisionTab";
import TagTab from "./TagTab";
import { useBranches, useTags } from "@scm-manager/ui-api";

type Props = {
  onSelect: CompareFunction;
  selected: string;
  label?: string;
  repository: Repository;
};

const BorderedMenu = styled.div`
  border: var(--scm-border);
  width: 100%;
`;

const StyledDropdownMenu = styled.div`
  width: 650px;

  @media screen and (max-width: ${devices.tablet.width}px) {
    width: 100%;
  }
`;

const determineType = (branches: Branch[], tags: Tag[], selected: string) => {
  if (branches.find((b) => b.name === selected)) {
    return "branch";
  }
  if (tags.find((b) => b.name === selected)) {
    return "tag";
  }
  return "revision";
};

const CompareSelector: FC<Props> = ({ onSelect, selected, label, repository }) => {
  const [t] = useTranslation("repos");
  const [showDropdown, setShowDropdown] = useState(false);
  const [filter, setFilter] = useState("");

  const { isLoading: branchesIsLoading, error: branchesError, data: branchesData } = useBranches(repository);
  const branches: Branch[] = (branchesData?._embedded?.branches as Branch[]) || [];

  const { isLoading: tagsIsLoading, error: tagsError, data: tagsData } = useTags(repository);
  const tags: Tag[] = (tagsData?._embedded?.tags as Tag[]) || [];

  const [selection, setSelection] = useState<CompareProps>({
    type: determineType(branches, tags, selected),
    name: selected,
  });
  const ref = useRef<HTMLInputElement>(null);

  const onSelectEntry = (type: CompareTypes, name: string) => {
    setSelection({ type, name });
    setShowDropdown(false);
    onSelect(type, name);
  };

  const onMousedown = (e: Event) => {
    if (ref.current && !ref.current.contains(e.target as HTMLElement)) {
      setShowDropdown(false);
    }
  };

  const onKeyUp = (e: KeyboardEvent) => {
    if (e.which === 27) {
      // escape
      setShowDropdown(false);
    }
  };

  useEffect(() => {
    window.addEventListener("mousedown", onMousedown);
    window.addEventListener("keyup", onKeyUp);
    return () => {
      window.removeEventListener("mousedown", onMousedown);
      window.removeEventListener("keyup", onKeyUp);
    };
  }, []);

  return (
    <div className="field mb-0 is-flex is-align-items-center has-gap-3">
      {label && <label className="label mb-0">{label}</label>}
      <div className="control">
        <div className="dropdown is-active" ref={ref}>
          <div className="dropdown-trigger">
            <button
              className="button has-text-weight-normal has-text-secondary-more px-4 is-flex is-justify-content-space-between is-fullwidth"
              onClick={() => setShowDropdown(!showDropdown)}
            >
              <span className="is-ellipsis-overflow">
                <strong>{t(`compare.selector.typeTitle.${selection.type}`)}:</strong> {selection.name}
              </span>
              <span className="icon is-small">
                <Icon>angle-down</Icon>
              </span>
            </button>
          </div>
          <StyledDropdownMenu className={classNames("dropdown-menu", { "is-hidden": !showDropdown })} role="menu">
            <BorderedMenu className="dropdown-content">
              <div className="dropdown-item">
                <h3 className="has-text-weight-bold">
                  {t(repository.type === "hg" ? "compare.selector.title_hg" : "compare.selector.title")}
                </h3>
              </div>
              <hr className="dropdown-divider my-1" />
              <div className="dropdown-item px-2">
                <input
                  className="input is-small"
                  placeholder={t("compare.selector.filter")}
                  onChange={(e) => setFilter(e.target.value)}
                  type="search"
                />
                <Tabs defaultValue="branch">
                  <Tabs.List className="is-small mt-3 mb-0" aria-label={t("compare.selector.title")}>
                    <Tabs.List.Trigger value="branch">{t("compare.selector.tabs.branch")}</Tabs.List.Trigger>
                    <Tabs.List.Trigger value="tag">{t("compare.selector.tabs.tags")}</Tabs.List.Trigger>
                    {repository._links.lookupRevision && (
                      <Tabs.List.Trigger value="revision">{t("compare.selector.tabs.revision")}</Tabs.List.Trigger>
                    )}
                  </Tabs.List>
                  <Tabs.Content value="branch">
                    <BranchTab
                      branches={branches}
                      branchesIsLoading={branchesIsLoading}
                      branchesError={branchesError}
                      onSelect={onSelectEntry}
                      selected={selection}
                      filter={filter}
                    />
                  </Tabs.Content>
                  <Tabs.Content value="tag">
                    <TagTab
                      tags={tags}
                      tagsError={tagsError}
                      tagsIsLoading={tagsIsLoading}
                      onSelect={onSelectEntry}
                      selected={selection}
                      filter={filter}
                    />
                  </Tabs.Content>
                  {repository._links.lookupRevision && (
                    <Tabs.Content value="revision">
                      <RevisionTab
                        onSelect={onSelectEntry}
                        selected={selection}
                        repository={repository}
                        filter={filter}
                      />
                    </Tabs.Content>
                  )}
                </Tabs>
              </div>
            </BorderedMenu>
          </StyledDropdownMenu>
        </div>
      </div>
    </div>
  );
};

export default CompareSelector;
