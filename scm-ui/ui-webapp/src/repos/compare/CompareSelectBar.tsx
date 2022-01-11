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
import { useHistory, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useBranches } from "@scm-manager/ui-api";
import { Branch, Repository } from "@scm-manager/ui-types";
import { BranchSelector, ErrorNotification, Icon, Loading } from "@scm-manager/ui-components";
import CompareSelector from "./CompareSelector";
import { CompareBranchesParams } from "./CompareView";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const CompareSelectBar: FC<Props> = ({ repository, baseUrl }) => {
  const [t] = useTranslation("repos");
  const match = useRouteMatch<CompareBranchesParams>();
  const history = useHistory();
  const { isLoading, error, data } = useBranches(repository);
  const [sources, setSources] = useState<string | undefined>(match?.params?.source);
  const [target, setTarget] = useState<string | undefined>(match?.params?.target);

  const branches: Branch[] = (data?._embedded?.branches as Branch[]) || [];

  useEffect(() => {
    if (sources && target) {
      history.push(baseUrl + "/" + encodeURIComponent(sources) + "/" + encodeURIComponent(target) + "/diff");
    }
  }, [sources, target, history, baseUrl]);

  if (isLoading) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <BranchSelector
        branches={branches}
        onSelectBranch={branch => setSources(branch?.name)}
        selectedBranch={sources}
        label="source"
      />
      <Icon name="arrow-right" />
      <BranchSelector
        branches={branches}
        onSelectBranch={branch => setTarget(branch?.name)}
        selectedBranch={target}
        label="target"
      />

      <hr />
      <div className="is-flex is-justify-content-space-around is-align-items-center">
        <CompareSelector repository={repository} /> <Icon name="arrow-right" title={t("compare.selector.with")} />{" "}
        <CompareSelector repository={repository} />
      </div>
    </>
  );
};

export default CompareSelectBar;
