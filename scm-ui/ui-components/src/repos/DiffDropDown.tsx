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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-core";
type DiffDropDownProps = {
  collapseDiffs: () => void;
  ignoreWhitespaces: () => void;
  renderOnMount: boolean;
};
const DiffDropDown: FC<DiffDropDownProps> = ({ collapseDiffs, ignoreWhitespaces, renderOnMount }) => {
  const [t] = useTranslation("repos");
  const [isOpen, setOpen] = useState(false);

  // This is a hack and it needs to be here until we fix the re rendering problem upon changing the whitespaces in the diffs
  useEffect(() => {
    if (renderOnMount) {
      ignoreWhitespaces();
      ignoreWhitespaces();
    }
  }, []);

  const handleOpen = () => {
    setOpen(!isOpen);
  };
  return (
    <div className={"dropdown" + (isOpen ? " is-active" : "")}>
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
            <Checkbox onChange={ignoreWhitespaces} label={t("changesets.checkBoxHideWhitespaceChanges")}></Checkbox>
          </div>
          <hr className="dropdown-divider" />
          <div className="dropdown-item">
            <span className="has-text-weight-semibold">{t("changesets.checkBoxHeadingOtherSettings")}</span>
          </div>
          <div className="dropdown-item">
            <Checkbox onChange={collapseDiffs} label={t("changesets.checkBoxCollapseOption")}></Checkbox>
          </div>
        </div>
      </div>
    </div>
  );
};
export default DiffDropDown;
