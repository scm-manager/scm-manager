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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Repository, Tag } from "@scm-manager/ui-types";
import { Subtitle, DateFromNow, SignatureIcon } from "@scm-manager/ui-components";
import TagButtonGroup from "./TagButtonGroup";
import CompareLink from "../../compare/CompareLink";

type Props = {
  repository: Repository;
  tag: Tag;
};

const TagDetail: FC<Props> = ({ repository, tag }) => {
  const [t] = useTranslation("repos");

  const encodedTag = encodeURIComponent(tag.name);

  return (
    <div className="media is-align-items-center">
      <div
        className={classNames(
          "media-content",
          "subtitle",
          "is-flex",
          "is-flex-wrap-wrap",
          "is-align-items-center",
          "mb-0"
        )}
      >
        <strong className="mr-1">{t("tag.name") + ": "}</strong> <Subtitle className="mb-0">{tag.name}</Subtitle>
        <SignatureIcon signatures={tag.signatures} className="ml-2 mb-5" />
        <div className={classNames("is-ellipsis-overflow", "is-size-7", "ml-2")}>
          {t("tags.overview.created")}{" "}
          <DateFromNow className={classNames("is-size-7", "has-text-secondary")} date={tag.date} />
        </div>
      </div>
      <CompareLink repository={repository} source={encodedTag} sourceType="t" target={encodedTag} targetType="t" />
      <div className="media-right">
        <TagButtonGroup repository={repository} tag={tag} />
      </div>
    </div>
  );
};

export default TagDetail;
