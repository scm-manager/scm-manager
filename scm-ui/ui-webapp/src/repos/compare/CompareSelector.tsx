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
import { Repository } from "@scm-manager/ui-types";
import { devices } from "@scm-manager/ui-components";
import { Tabs } from "@scm-manager/ui-layout";
import { Icon } from "@scm-manager/ui-core";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";
import BranchTab from "./BranchTab";
import TagTab from "./TagTab";
import RevisionTab from "./RevisionTab";

type Props = {
  onSelect: CompareFunction;
  selected: CompareProps;
  label: string;
  repository: Repository;
};

const ResponsiveWrapper = styled.div`
  width: 100%;
  justify-content: flex-start;
  @media screen and (min-width: ${devices.tablet.width}px) {
    justify-content: space-between;
  }
`;

const BorderedMenu = styled.div`
  border: var(--scm-border);
`;

const MaxWidthDiv = styled.div`
  width: 100%;
`;

const CompareSelector: FC<Props> = ({ onSelect, selected, label, repository }) => {
  const [t] = useTranslation("repos");
  const [showDropdown, setShowDropdown] = useState(false);
  const [filter, setFilter] = useState("");
  const [selection, setSelection] = useState<CompareProps>(selected);
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
  });

  const getActionTypeName = (type: CompareTypes) => {
    switch (type) {
      case "b":
        return "Branch";
      case "t":
        return "Tag";
      case "r":
        return "Revision";
    }
  };

  return (
    <ResponsiveWrapper className="field mb-0 is-flex is-flex-direction-column is-fullwidth">
      <label className="label">{label}</label>
      <MaxWidthDiv className="control">
        <MaxWidthDiv className="dropdown is-active" ref={ref}>
          <MaxWidthDiv className="dropdown-trigger">
            <button
              className="button has-text-weight-normal has-text-secondary-more px-4 is-flex is-justify-content-space-between is-fullwidth"
              onClick={() => setShowDropdown(!showDropdown)}
            >
              <span className="is-ellipsis-overflow">
                <strong>{getActionTypeName(selection.type)}:</strong> {selection.name}
              </span>
              <span className="icon is-small">
                <Icon>angle-down</Icon>
              </span>
            </button>
          </MaxWidthDiv>
          <div className={classNames("dropdown-menu", { "is-hidden": !showDropdown })} role="menu">
            <BorderedMenu className="dropdown-content">
              <div className="dropdown-item">
                <h3 className="has-text-weight-bold">{t("compare.selector.title")}</h3>
              </div>
              <hr className="dropdown-divider my-1" />
              <div className="dropdown-item px-2">
                <input
                  className="input is-small"
                  placeholder={t("compare.selector.filter")}
                  onChange={(e) => setFilter(e.target.value)}
                  type="search"
                />
                <Tabs className="is-small mt-3 mb-0" defaultValue="branch">
                  <Tabs.List aria-label={t("compare.selector.title")}>
                    <Tabs.List.Trigger value="branch">{t("compare.selector.tabs.b")}</Tabs.List.Trigger>
                    <Tabs.List.Trigger value="tag">{t("compare.selector.tabs.t")}</Tabs.List.Trigger>
                    <Tabs.List.Trigger value="revision">{t("compare.selector.tabs.r")}</Tabs.List.Trigger>
                  </Tabs.List>
                  <Tabs.Content value="branch">
                    <BranchTab onSelect={onSelectEntry} selected={selected} repository={repository} filter={filter} />
                  </Tabs.Content>
                  <Tabs.Content value="tag">
                    <TagTab onSelect={onSelectEntry} selected={selected} repository={repository} filter={filter} />
                  </Tabs.Content>
                  <Tabs.Content value="revision">
                    <RevisionTab onSelect={onSelectEntry} selected={selected} />
                  </Tabs.Content>
                </Tabs>
              </div>
            </BorderedMenu>
          </div>
        </MaxWidthDiv>
      </MaxWidthDiv>
    </ResponsiveWrapper>
  );
};

export default CompareSelector;
