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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDeleteTag } from "@scm-manager/ui-api";
import { ConfirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { CardListBox, KeyboardIterator } from "@scm-manager/ui-core";
import { Repository, Tag } from "@scm-manager/ui-types";
import TagRow from "./TagRow";

type Props = {
  repository: Repository;
  baseUrl: string;
  tags: Tag[];
};

const TagTable: FC<Props> = ({ repository, baseUrl, tags }) => {
  const { isPending, error, remove, isDeleted } = useDeleteTag(repository);
  const [t] = useTranslation("repos");
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [tagToBeDeleted, setTagToBeDeleted] = useState<Tag | undefined>();
  useEffect(() => {
    if (isDeleted) {
      resetAndCloseDialog();
    }
  }, [isDeleted]);

  const onDelete = (tag: Tag) => {
    setTagToBeDeleted(tag);
    setShowConfirmAlert(true);
  };

  const abortDelete = () => {
    resetAndCloseDialog();
  };

  const resetAndCloseDialog = () => {
    setTagToBeDeleted(undefined);
    setShowConfirmAlert(false);
  };

  const deleteTag = () => {
    if (tagToBeDeleted) {
      remove(tagToBeDeleted);
    }
  };

  return (
    <>
      {showConfirmAlert ? (
        <ConfirmAlert
          title={t("tag.delete.confirmAlert.title")}
          message={t("tag.delete.confirmAlert.message", { tag: tagToBeDeleted?.name })}
          buttons={[
            {
              label: t("tag.delete.confirmAlert.submit"),
              isLoading: isPending,
              onClick: () => deleteTag(),
            },
            {
              className: "is-info",
              label: t("tag.delete.confirmAlert.cancel"),
              onClick: () => abortDelete(),
              autofocus: true,
            },
          ]}
          close={() => abortDelete()}
        />
      ) : null}
      {error ? <ErrorNotification error={error} /> : null}
      <CardListBox>
        <KeyboardIterator>
          {tags.map((tag) => (
            <TagRow key={tag.name} baseUrl={baseUrl} tag={tag} onDelete={onDelete} />
          ))}
        </KeyboardIterator>
      </CardListBox>
    </>
  );
};

export default TagTable;
