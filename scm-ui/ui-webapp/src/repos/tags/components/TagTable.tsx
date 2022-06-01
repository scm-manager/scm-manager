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
import { Repository, Tag } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import TagRow from "./TagRow";
import { ConfirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { useDeleteTag } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  baseUrl: string;
  tags: Tag[];
};

const TagTable: FC<Props> = ({ repository, baseUrl, tags }) => {
  const { isLoading, error, remove, isDeleted } = useDeleteTag(repository);
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
              isLoading,
              onClick: () => deleteTag()
            },
            {
              className: "is-info",
              label: t("tag.delete.confirmAlert.cancel"),
              onClick: () => abortDelete(),
              autofocus: true
            }
          ]}
          close={() => abortDelete()}
        />
      ) : null}
      {error ? <ErrorNotification error={error} /> : null}
      <table className="card-table table is-hoverable is-fullwidth is-word-break">
        <thead>
          <tr>
            <th>{t("tags.table.tags")}</th>
          </tr>
        </thead>
        <tbody>
          {tags.map(tag => (
            <TagRow key={tag.name} baseUrl={baseUrl} tag={tag} onDelete={onDelete} />
          ))}
        </tbody>
      </table>
    </>
  );
};

export default TagTable;
