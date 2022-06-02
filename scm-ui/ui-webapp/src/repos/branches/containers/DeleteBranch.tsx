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
import { Redirect } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Branch, Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteBranch } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  branch: Branch;
};

const DeleteBranch: FC<Props> = ({ repository, branch }: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");
  const { isLoading, error, remove, isDeleted } = useDeleteBranch(repository);

  if (isDeleted) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}/branches/`} />;
  }

  if (!branch._links.delete) {
    return null;
  }

  let confirmAlert = null;
  if (showConfirmAlert) {
    confirmAlert = (
      <ConfirmAlert
        title={t("branch.delete.confirmAlert.title")}
        message={t("branch.delete.confirmAlert.message", { branch: branch.name })}
        buttons={[
          {
            label: t("branch.delete.confirmAlert.submit"),
            onClick: () => remove(branch),
            isLoading
          },
          {
            className: "is-info",
            label: t("branch.delete.confirmAlert.cancel"),
            onClick: () => null,
            autofocus: true
          }
        ]}
        close={() => setShowConfirmAlert(false)}
      />
    );
  }

  return (
    <>
      <ErrorNotification error={error} />
      {showConfirmAlert && confirmAlert}
      <Level
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("branch.delete.subtitle")}</h4>
            <p>{t("branch.delete.description")}</p>
          </div>
        }
        right={<DeleteButton label={t("branch.delete.button")} action={() => setShowConfirmAlert(true)} />}
      />
    </>
  );
};

export default DeleteBranch;
