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
import BranchRow from "./BranchRow";
import { Branch } from "@scm-manager/ui-types";
import { apiClient, ConfirmAlert, ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  baseUrl: string;
  branches: Branch[];
  fetchBranches: () => void;
};

const BranchTable: FC<Props> = ({ baseUrl, branches, fetchBranches }) => {
  const [t] = useTranslation("repos");
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [error, setError] = useState<Error | undefined>();
  const [deleteBranchUrl, setDeleteBranchUrl] = useState("");

  const onDelete = (url: string) => {
    setDeleteBranchUrl(url);
    setShowConfirmAlert(true);
  };

  const abortDelete = () => {
    setDeleteBranchUrl("");
    setShowConfirmAlert(false);
  };

  const deleteBranch = () => {
    apiClient
      .delete(deleteBranchUrl)
      .then(() => fetchBranches())
      .catch(setError);
  };

  const renderRow = () => {
    let rowContent = null;
    if (branches) {
      rowContent = branches.map((branch, index) => {
        return <BranchRow key={index} baseUrl={baseUrl} branch={branch} onDelete={onDelete} />;
      });
    }
    return rowContent;
  };

  const confirmAlert = (
    <ConfirmAlert
      title={t("branch.delete.confirmAlert.title")}
      message={t("branch.delete.confirmAlert.message")}
      buttons={[
        {
          className: "is-outlined",
          label: t("branch.delete.confirmAlert.submit"),
          onClick: () => deleteBranch()
        },
        {
          label: t("branch.delete.confirmAlert.cancel"),
          onClick: () => abortDelete()
        }
      ]}
      close={() => abortDelete()}
    />
  );

  return (
    <>
      {showConfirmAlert && confirmAlert}
      {error && <ErrorNotification error={error} />}
      <table className="card-table table is-hoverable is-fullwidth is-word-break">
        <thead>
          <tr>
            <th>{t("branches.table.branches")}</th>
          </tr>
        </thead>
        <tbody>{renderRow()}</tbody>
      </table>
    </>
  );
};

export default BranchTable;
