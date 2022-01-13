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

const SizedDiv = styled.div`
  width: 250px;

  @media (max-width: ${devices.tablet.width}px) {
    width: 100%;
  }
`;

const MaxWidthButton = styled.button`
  max-width: 250px;
`;

const CompareSelector: FC<Props> = ({ onSelect, selected, label, repository }) => {
  const [t] = useTranslation("repos");
  const [showDropdown, setShowDropdown] = useState(false);
  const [filter, setFilter] = useState("");
  const [selection, setSelection] = useState<CompareProps>(selected);

  const onSelectEntry = (type: CompareTypes, name: string) => {
    setSelection({ type, name });
    setShowDropdown(false);
    onSelect(type, name);
  };

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
    <SizedDiv className="field mb-0">
      <label className="label">{label}</label>
      <div className="control">
        <div className="dropdown is-active">
          <div className="dropdown-trigger">
            <MaxWidthButton
              className="button has-text-weight-normal px-4"
              onClick={() => setShowDropdown(!showDropdown)}
            >
              <span className="is-ellipsis-overflow">
                <strong>{getActionTypeName(selection.type)}:</strong> {selection.name}
              </span>
              <span className="icon is-small">
                <Icon name="angle-down" color="inherit" />
              </span>
            </MaxWidthButton>
          </div>
          <div className={classNames("dropdown-menu", { "is-hidden": !showDropdown })} role="menu">
            <div className="dropdown-content">
              <div className="dropdown-item">
                <h3 className="has-text-weight-bold">{t("compare.selector.title")}</h3>
              </div>
              <hr className="dropdown-divider my-1" />
              <div className="dropdown-item px-2">
                <input
                  className="input is-small"
                  placeholder={t("compare.selector.filter")}
                  onChange={e => setFilter(e.target.value)}
                />
                <CompareSelectorList
                  onSelect={onSelectEntry}
                  selected={selected}
                  repository={repository}
                  filter={filter}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </SizedDiv>
  );
};

export default CompareSelector;
