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
import styled from "styled-components";
import { Branch, Tag } from "@scm-manager/ui-types";
import DefaultBranchTag from "../branches/components/DefaultBranchTag";
import CompareSelectorListEntry from "./CompareSelectorListEntry";

type Props = {
  branches: Branch[];
  tags: Tag[];
};

const ScrollableUl = styled.ul`
  max-height: 250px;
  overflow-x: hidden;
  overflow-y: scroll;
  width: 300px;
`;

const CompareSelectorList: FC<Props> = ({ branches, tags }) => {
  const [selectedTab, setSelectedTab] = useState<"branches" | "tags">("branches");
  const [t] = useTranslation("repos");

  return (
    <>
      <div className="tabs is-small mt-3 mb-0">
        <ul>
          <li className={selectedTab === "branches" ? "is-active" : ""}>
            <a onClick={() => setSelectedTab("branches")}>{t("compare.selector.branches")}</a>
          </li>
          <li className={selectedTab === "tags" ? "is-active" : ""}>
            <a onClick={() => setSelectedTab("tags")}>{t("compare.selector.tags")}</a>
          </li>
        </ul>
      </div>
      <ScrollableUl aria-expanded="true" role="listbox">
        {selectedTab === "branches" &&
          branches.map((branch, index) => {
            return (
              <CompareSelectorListEntry isSelected={false} key={index}>
                <span className="is-ellipsis-overflow">{branch.name}</span>{" "}
                <DefaultBranchTag defaultBranch={branch.defaultBranch} />
              </CompareSelectorListEntry>
            );
          })}
        {selectedTab === "tags" &&
          tags.map((tag, index) => {
            return (
              <CompareSelectorListEntry isSelected={false} key={index}>
                <span className="is-ellipsis-overflow">{tag.name}</span>
              </CompareSelectorListEntry>
            );
          })}
      </ScrollableUl>
    </>
  );
};

export default CompareSelectorList;
