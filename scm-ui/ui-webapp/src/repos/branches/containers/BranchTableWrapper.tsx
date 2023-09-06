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
import { Collapsible, DataPageHeader } from "@scm-manager/ui-layout";
import { Select } from "@scm-manager/ui-forms";
import { SORT_OPTIONS, SortOption } from "../../tags/orderTags";

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
      <DataPageHeader>
        <DataPageHeader.Settings className="is-flex is-align-items-center">
          <DataPageHeader.Settings.Setting>
            {({ formFieldId }) => (
              <>
                <DataPageHeader.Settings.Setting.Label htmlFor={formFieldId}>
                  {t("branches.overview.sort.label")}
                </DataPageHeader.Settings.Setting.Label>
                <DataPageHeader.Settings.Setting.Field>
                  <Select id={formFieldId} onChange={(e) => setSort(e.target.value as SortOption)}>
                    {SORT_OPTIONS.map((sortOption) => (
                      <option key={sortOption} value={sortOption}>
                        {t(`branches.overview.sort.option.${sortOption}`)}
                      </option>
                    ))}
                  </Select>
                </DataPageHeader.Settings.Setting.Field>
              </>
            )}
          </DataPageHeader.Settings.Setting>
        </DataPageHeader.Settings>
        {showCreateButton ? (
          <DataPageHeader.CreateButton to="./create">{t("branches.overview.createButton")}</DataPageHeader.CreateButton>
        ) : null}
      </DataPageHeader>
      <div className="is-flex is-flex-direction-column has-gap-4">
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
      </div>
    </>
  );
};

export default BranchTableWrapper;
