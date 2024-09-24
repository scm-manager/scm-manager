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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect } from "react-router-dom";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { Repository, Tag } from "@scm-manager/ui-types";
import { useDeleteTag } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  tag: Tag;
};

const DeleteTag: FC<Props> = ({ tag, repository }) => {
  const { isLoading, error, remove, isDeleted } = useDeleteTag(repository);
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");

  if (isDeleted) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}/tags/`} />;
  }

  return (
    <>
      <ErrorNotification error={error} />
      {showConfirmAlert ? (
        <ConfirmAlert
          title={t("tag.delete.confirmAlert.title")}
          message={t("tag.delete.confirmAlert.message", { tag: tag.name })}
          buttons={[
            {
              label: t("tag.delete.confirmAlert.submit"),
              isLoading,
              onClick: () => remove(tag)
            },
            {
              className: "is-info",
              label: t("tag.delete.confirmAlert.cancel"),
              onClick: () => null,
              autofocus: true
            }
          ]}
          close={() => setShowConfirmAlert(false)}
        />
      ) : null}
      <Level
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("tag.delete.subtitle")}</h4>
            <p>{t("tag.delete.description")}</p>
          </div>
        }
        right={<DeleteButton label={t("tag.delete.button")} action={() => setShowConfirmAlert(true)} />}
      />
    </>
  );
};

export default DeleteTag;
