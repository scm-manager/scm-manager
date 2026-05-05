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
import { Link, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { CompareBranchesParams } from "./CompareView";

const CompareTabs: FC<{ baseUrl: string }> = ({ baseUrl }) => {
  const [t] = useTranslation("repos");
  const { sourceType, sourceName, targetType, targetName } = useParams<CompareBranchesParams>();
  const url = `${baseUrl}/${sourceType}/${encodeURIComponent(sourceName!)}/${targetType}/${encodeURIComponent(targetName!)}/`;
  const location = useLocation();

  const setIsActiveClassName = (path: string) => {
    const regex = new RegExp(path);
    return location.pathname.match(regex) ? "is-active" : "";
  };

  return (
    <div className="tabs mt-5">
      <ul>
        <li className={setIsActiveClassName("/diff/")}>
          <Link to={url + "diff/"}>{t("compare.tabs.diff")}</Link>
        </li>
        <li className={setIsActiveClassName("/changesets/")}>
          <Link to={url + "changesets/"}>{t("compare.tabs.commits")}</Link>
        </li>
      </ul>
    </div>
  );
};

export default CompareTabs;
