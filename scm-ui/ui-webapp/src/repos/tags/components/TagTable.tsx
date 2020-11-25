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

import React, {FC, useState} from "react";
import {Link, Tag} from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import TagRow from "./TagRow";
import {apiClient, ConfirmAlert, ErrorNotification} from "@scm-manager/ui-components";

type Props = {
  baseUrl: string;
  tags: Tag[];
  fetchTags: () => void;
};

const TagTable: FC<Props> = ({ baseUrl, tags, fetchTags }) => {
  const [t] = useTranslation("repos");
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [error, setError] = useState<Error | undefined>();
  const [tagToBeDeleted, setTagToBeDeleted] = useState<Tag | undefined>();

  const onDelete = (tag: Tag) => {
    setTagToBeDeleted(tag);
    setShowConfirmAlert(true);
  };

  const abortDelete = () => {
    setTagToBeDeleted(undefined);
    setShowConfirmAlert(false);
  };

  const deleteTag = () => {
    apiClient
      .delete((tagToBeDeleted?._links.delete as Link).href)
      .then(() => fetchTags())
      .catch(setError);
  };

  const renderRow = () => {
    let rowContent = null;
    if (tags) {
      rowContent = tags.map((tag, index) => {
        return <TagRow key={index} baseUrl={baseUrl} tag={tag} onDelete={onDelete} />;
      });
    }
    return rowContent;
  };

  const confirmAlert = (
    <ConfirmAlert
      title={t("tag.delete.confirmAlert.title")}
      message={t("tag.delete.confirmAlert.message", { tag: tagToBeDeleted?.name })}
      buttons={[
        {
          className: "is-outlined",
          label: t("tag.delete.confirmAlert.submit"),
          onClick: () => deleteTag()
        },
        {
          label: t("tag.delete.confirmAlert.cancel"),
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
          <th>{t("tags.table.tags")}</th>
        </tr>
      </thead>
      <tbody>{renderRow()}</tbody>
    </table>
  </>);
};

export default TagTable;
