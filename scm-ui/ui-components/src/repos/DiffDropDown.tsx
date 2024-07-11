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
