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
import BranchRow from "./BranchRow";
import { Branch, BranchDetails, Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { useDeleteBranch } from "@scm-manager/ui-api";

type Props = {
  baseUrl: string;
  repository: Repository;
  branches: Branch[];
  type: string;
  branchesDetails: BranchDetails[];
};

const BranchTable: FC<Props> = ({ repository, baseUrl, branches, type, branchesDetails }) => {
  const { isLoading, error, remove, isDeleted } = useDeleteBranch(repository);
  const [t] = useTranslation("repos");
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [branchToBeDeleted, setBranchToBeDeleted] = useState<Branch | undefined>();
  useEffect(() => {
    if (isDeleted) {
      closeAndResetDialog();
    }
  }, [isDeleted]);

  const closeAndResetDialog = () => {
    setBranchToBeDeleted(undefined);
    setShowConfirmAlert(false);
  };

  const onDelete = (branch: Branch) => {
    setBranchToBeDeleted(branch);
    setShowConfirmAlert(true);
  };

  const abortDelete = () => {
    closeAndResetDialog();
  };

  const deleteBranch = () => {
    if (branchToBeDeleted) {
      remove(branchToBeDeleted);
    }
  };

  return (
    <>
      {showConfirmAlert ? (
        <ConfirmAlert
          title={t("branch.delete.confirmAlert.title")}
          message={t("branch.delete.confirmAlert.message", { branch: branchToBeDeleted?.name })}
          buttons={[
            {
              className: "is-outlined",
              label: t("branch.delete.confirmAlert.submit"),
              isLoading,
              onClick: () => deleteBranch(),
            },
            {
              label: t("branch.delete.confirmAlert.cancel"),
              onClick: () => abortDelete(),
              autofocus: true,
            },
          ]}
          close={() => abortDelete()}
        />
      ) : null}
      <ErrorNotification error={error} />
      <table className="card-table table is-hoverable is-fullwidth is-word-break">
        <thead>
          <tr>
            <th>{t(`branches.table.branches.${type}`)}</th>
          </tr>
        </thead>
        <tbody>
          {(branches || []).map((branch) => (
            <BranchRow
              key={branch.name}
              repository={repository}
              baseUrl={baseUrl}
              branch={branch}
              onDelete={onDelete}
              details={branchesDetails?.filter((b: BranchDetails) => b.branchName === branch.name)[0]}
            />
          ))}
        </tbody>
      </table>
    </>
  );
};

export default BranchTable;
