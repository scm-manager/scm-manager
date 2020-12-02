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
import { useHistory } from "react-router-dom";
import { apiClient, ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { Link, Repository, Tag } from "@scm-manager/ui-types";

type Props = {
  repository: Repository;
  tag: Tag;
};

const DeleteTag: FC<Props> = ({ tag, repository }) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [error, setError] = useState<Error | undefined>();
  const [t] = useTranslation("repos");
  const history = useHistory();

  const deleteBranch = () => {
    apiClient
      .delete((tag._links.delete as Link).href)
      .then(() => history.push(`/repo/${repository.namespace}/${repository.name}/tags/`))
      .catch(setError);
  };

  if (!tag._links.delete) {
    return null;
  }

  let confirmAlert = null;
  if (showConfirmAlert) {
    confirmAlert = (
      <ConfirmAlert
        title={t("tag.delete.confirmAlert.title")}
        message={t("tag.delete.confirmAlert.message", { tag: tag.name })}
        buttons={[
          {
            className: "is-outlined",
            label: t("tag.delete.confirmAlert.submit"),
            onClick: () => deleteBranch()
          },
          {
            label: t("tag.delete.confirmAlert.cancel"),
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
            <strong>{t("tag.delete.subtitle")}</strong>
            <br />
            {t("tag.delete.description")}
          </p>
        }
        right={<DeleteButton label={t("tag.delete.button")} action={() => setShowConfirmAlert(true)} />}
      />
    </>
  );
};

export default DeleteTag;
