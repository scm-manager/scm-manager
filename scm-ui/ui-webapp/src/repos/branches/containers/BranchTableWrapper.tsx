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

import React, { FC, useMemo, useState } from "react";
import { Branch, HalRepresentation, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Notification, Subtitle } from "@scm-manager/ui-components";
import { orderBranches } from "../util/orderBranches";
import { useTranslation } from "react-i18next";
import { useBranchDetailsCollection } from "@scm-manager/ui-api";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";
import BranchList from "../components/BranchList";
import { Collapsible } from "@scm-manager/ui-layout";
import { LinkButton } from "@scm-manager/ui-buttons";
import { Select } from "@scm-manager/ui-forms";
import { SORT_OPTIONS, SortOption } from "../../tags/orderTags";
import styled from "styled-components";

const BranchListWrapper = styled.div`
  gap: 1rem;
`;

const HeaderWrapper = styled.div`
  gap: 0.5rem 1rem;
`;

type Props = {
  repository: Repository;
  baseUrl: string;
  data: HalRepresentation;
};

const BranchTableWrapper: FC<Props> = ({ repository, baseUrl, data }) => {
  const [t] = useTranslation("repos");
  const [sort, setSort] = useState<SortOption | undefined>();
  const branches: Branch[] = useMemo(
    () => orderBranches((data?._embedded?.branches as Branch[]) || [], sort),
    [data, sort]
  );
  const staleBranches = branches.filter((b) => b.stale);
  const activeBranches = branches.filter((b) => !b.stale);
  const { error, data: branchesDetails } = useBranchDetailsCollection(repository, [
    ...activeBranches,
    ...staleBranches,
  ]);

  if (branches.length === 0) {
    return <Notification type="info">{t("branches.overview.noBranches")}</Notification>;
  }

  const showCreateButton = !!data._links.create;

  return (
    <>
      <Subtitle subtitle={t("branches.overview.title")} />
      <ErrorNotification error={error} />
      <HeaderWrapper className="is-flex is-flex-wrap-wrap is-justify-content-space-between mb-3">
        <div className="is-flex is-align-items-center">
          <label className="mr-2" htmlFor="branches-overview-sort">
            {t("branches.overview.sort.label")}
          </label>
          <Select id="branches-overview-sort" onChange={(e) => setSort(e.target.value as SortOption)}>
            {SORT_OPTIONS.map((sortOption) => (
              <option key={sortOption} value={sortOption}>
                {t(`branches.overview.sort.option.${sortOption}`)}
              </option>
            ))}
          </Select>
        </div>
        {showCreateButton ? (
          <LinkButton variant="primary" to="./create">
            {t("branches.overview.createButton")}
          </LinkButton>
        ) : null}
      </HeaderWrapper>
      <BranchListWrapper className="is-flex is-flex-direction-column">
        <KeyboardIterator>
          {activeBranches.length > 0 ? (
            <Collapsible header={t("branches.table.branches.active")}>
              <BranchList
                repository={repository}
                baseUrl={baseUrl}
                branches={activeBranches}
                branchesDetails={branchesDetails}
              />
            </Collapsible>
          ) : null}
          {staleBranches.length > 0 ? (
            <Collapsible header={t("branches.table.branches.stale")} defaultCollapsed>
              <BranchList
                repository={repository}
                baseUrl={baseUrl}
                branches={staleBranches}
                branchesDetails={branchesDetails}
              />
            </Collapsible>
          ) : null}
        </KeyboardIterator>
      </BranchListWrapper>
    </>
  );
};

export default BranchTableWrapper;
