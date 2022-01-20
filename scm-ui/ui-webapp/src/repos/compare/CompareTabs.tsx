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
import { Link, useLocation, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { CompareBranchesParams } from "./CompareView";

type Props = {
  baseUrl: string;
};

const CompareTabs: FC<Props> = ({ baseUrl }) => {
  const [t] = useTranslation("repos");
  const location = useLocation();
  const match = useRouteMatch<CompareBranchesParams>();

  const url = `${baseUrl}/${match.params.sourceType}/${match.params.sourceName}/${match.params.targetType}/${match.params.targetName}`;

  const setIsActiveClassName = (path: string) => {
    const regex = new RegExp(url + path);
    return location.pathname.match(regex) ? "is-active" : "";
  };

  return (
    <div className="tabs mt-5">
      <ul>
        <li className={setIsActiveClassName("/diff/")}>
          <Link to={`${url}/diff/`}>{t("compare.tabs.diff")}</Link>
        </li>
        <li className={setIsActiveClassName("/changesets/")}>
          <Link to={`${url}/changesets/`}>{t("compare.tabs.commits")}</Link>
        </li>
      </ul>
    </div>
  );
};

export default CompareTabs;
