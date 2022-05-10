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

import React, { FC, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Repository } from "@scm-manager/ui-types";
import { devices, Icon } from "@scm-manager/ui-components";
import CompareSelectorList from "./CompareSelectorList";
import { CompareFunction, CompareProps, CompareTypes } from "./CompareSelectBar";

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
                <Icon name="angle-down" color="inherit" />
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
                <CompareSelectorList
                  onSelect={onSelectEntry}
                  selected={selected}
                  repository={repository}
                  filter={filter}
                />
              </div>
            </BorderedMenu>
          </div>
        </MaxWidthDiv>
      </MaxWidthDiv>
    </ResponsiveWrapper>
  );
};

export default CompareSelector;
