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
import { useTranslation } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-core";
import { CollapseMode, WhitespaceMode } from "./LoadingDiff";

type DiffDropDownProps = {
  collapseMode: CollapseMode;
  setCollapseMode?: (collapseMode: CollapseMode) => void;
  renderOnMount: boolean;
  ignoreWhitespacesMode?: WhitespaceMode;
  setIgnoreWhitespacesMode?: (hideWhiteSpaceMode: WhitespaceMode) => void;
};
const DiffDropDown: FC<DiffDropDownProps> = ({
  collapseMode = "MANUAL",
  setCollapseMode,
  ignoreWhitespacesMode,
  setIgnoreWhitespacesMode,
}) => {
  const [t] = useTranslation("repos");

  const [isOpen, setOpen] = useState(false);

  const handleOpen = () => {
    setOpen(!isOpen);
  };

  const handleIgnoreWhitespacesModeChange: React.ChangeEventHandler<HTMLInputElement> = (event) => {
    if (setIgnoreWhitespacesMode) {
      setIgnoreWhitespacesMode(event.target.checked ? "ALL" : "NONE");
    }
    setOpen(false);
  };

  const handleCollapseModeChange = () => {
    if (setCollapseMode) {
      setCollapseMode(collapseMode === "MANUAL" ? "ALL" : "MANUAL");
    }
    setOpen(false);
  };

  return (
    <div className={"dropdown is-right " + (isOpen ? " is-active" : "")}>
      <div className="dropdown-trigger">
        <button onClick={handleOpen} className="button" aria-haspopup="true" aria-controls="dropdown-menu2">
          <span className="icon is-small">
            <i className="fas fa-cog" aria-hidden="true"></i>
          </span>
        </button>
      </div>
      <div className="dropdown-menu" id="dropdown-menu2" role="menu">
        <div className="dropdown-content">
          <div className="dropdown-item">
            <span className="has-text-weight-semibold">{t("changesets.checkBoxHeadingWhitespaces")}</span>
          </div>
          <div className="dropdown-item">
            {setIgnoreWhitespacesMode && ignoreWhitespacesMode ? (
              <Checkbox
                checked={ignoreWhitespacesMode === "ALL"}
                onChange={handleIgnoreWhitespacesModeChange}
                label={t("changesets.checkBoxHideWhitespaceChanges")}
              />
            ) : (
              <Checkbox label={t("changesets.checkBoxHideWhitespaceChanges")} />
            )}
          </div>
          <hr className="dropdown-divider" />
          <div className="dropdown-item">
            <span className="has-text-weight-semibold">{t("changesets.checkBoxHeadingOtherSettings")}</span>
          </div>
          <div className="dropdown-item">
            <Checkbox
              checked={collapseMode === "ALL"}
              onChange={handleCollapseModeChange}
              label={t("changesets.checkBoxCollapseOption")}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
export default DiffDropDown;
