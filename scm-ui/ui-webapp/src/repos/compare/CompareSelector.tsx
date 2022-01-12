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
import classNames from "classnames";
import { Branch, Repository, Tag } from "@scm-manager/ui-types";
import { useBranches, useTags } from "@scm-manager/ui-api";
import { ErrorNotification, Icon, Loading } from "@scm-manager/ui-components";
import CompareSelectorList from "./CompareSelectorList";

type Props = {
  repository: Repository;
};

const CompareSelector: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const [hidden, setHidden] = useState(true); // Todo TEMP
  const { isLoading: branchesIsLoading, error: branchesError, data: branchesData } = useBranches(repository);
  const branches: Branch[] = (branchesData?._embedded?.branches as Branch[]) || [];
  const { isLoading: tagsIsLoading, error: tagsError, data: tagsData } = useTags(repository);
  const tags: Tag[] = (tagsData?._embedded?.tags as Tag[]) || [];

  //TODO loader in select componente ziehen
  if (branchesIsLoading || tagsIsLoading) {
    return <Loading />;
  }
  if (branchesError || tagsError) {
    return <ErrorNotification error={branchesError || tagsError} />;
  }

  return (
    <div className="dropdown is-active">
      <div className="dropdown-trigger">
        <button className="button has-text-weight-normal px-4" onClick={() => setHidden(!hidden)}>
          <span>
            <strong>branch:</strong> main
          </span>
          <span className="icon is-small">
            <Icon name="angle-down" color="inherit" />
          </span>
        </button>
      </div>
      <div className={classNames("dropdown-menu", { "is-hidden": hidden })} id="dropdown-menu2" role="menu">
        <div className="dropdown-content">
          <div className="dropdown-item">
            <h3 className="has-text-weight-bold">Text first?</h3>
          </div>
          <hr className="dropdown-divider my-1" />
          <div className="dropdown-item px-2">
            <input className="input is-small" placeholder={t("compare.selector.filter")} />
            <CompareSelectorList branches={branches} tags={tags} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default CompareSelector;
