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
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Branch, Link, Repository } from "@scm-manager/ui-types";
import { apiClient, ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";

type Props = {
  repository: Repository;
  branch: Branch;
};

const DeleteBranch: FC<Props> = ({ repository, branch }: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [error, setError] = useState<Error | undefined>();
  const [t] = useTranslation("repos");
  const history = useHistory();

  console.log("branchview", repository, branch);

  const deleteBranch = () => {
    apiClient
      .delete((branch._links.delete as Link).href)
      .then(() => history.push(`/repo/${repository.namespace}/${repository.name}/branches/`))
      .catch(setError);
  };

  if (!branch._links.delete) {
    return null;
  }

  let confirmAlert = null;
  if (showConfirmAlert) {
    confirmAlert = (
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
            onClick: () => null
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
          <p>
            <strong>{t("branch.delete.subtitle")}</strong>
            <br />
            {t("branch.delete.description")}
          </p>
        }
        right={<DeleteButton label={t("branch.delete.button")} action={() => setShowConfirmAlert(true)} />}
      />
    </>
  );
};

export default DeleteBranch;
