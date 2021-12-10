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
import React, { FC } from "react";
import { Link as RouterLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Tag, Link } from "@scm-manager/ui-types";
import { DateFromNow, Icon } from "@scm-manager/ui-components";

type Props = {
  tag: Tag;
  baseUrl: string;
  onDelete: (tag: Tag) => void;
  // deleting: boolean;
};

const TagRow: FC<Props> = ({ tag, baseUrl, onDelete }) => {
  const [t] = useTranslation("repos");

  let deleteButton;
  if ((tag?._links?.delete as Link)?.href) {
    deleteButton = (
      <span
        className="icon is-small is-clickable"
        onClick={() => onDelete(tag)}
        onKeyDown={e => e.key === "Enter" && onDelete(tag)}
        tabIndex={0}
      >
        <Icon name="trash" className="fas" title={t("tag.delete.button")} />
      </span>
    );
  }

  const to = `${baseUrl}/${encodeURIComponent(tag.name)}/info`;
  return (
    <tr>
      <td>
        <RouterLink to={to} title={tag.name}>
          {tag.name}
          <span className={classNames("has-text-secondary", "is-ellipsis-overflow", "ml-2", "is-size-7")}>
            {t("tags.overview.created")} <DateFromNow date={tag.date} />
          </span>
        </RouterLink>
      </td>
      <td className="is-darker has-text-centered">{deleteButton}</td>
    </tr>
  );
};

export default TagRow;
