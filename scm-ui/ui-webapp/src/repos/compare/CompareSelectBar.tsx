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
import { useHistory, useLocation, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";
import CompareSelector from "./CompareSelector";
import { CompareBranchesParams } from "./CompareView";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const CompareSelectBar: FC<Props> = ({ repository, baseUrl }) => {
  const [t] = useTranslation("repos");
  const match = useRouteMatch<CompareBranchesParams>();
  const location = useLocation();
  const history = useHistory();
  const [sources, setSources] = useState<string | undefined>(decodeURIComponent(match?.params?.source));
  const [target, setTarget] = useState<string | undefined>(decodeURIComponent(match?.params?.target));

  useEffect(() => {
    if (sources && target) {
      const lastUriComponent = location.pathname.split("/").slice(-1)[0];
      history.push(
        baseUrl + "/" + encodeURIComponent(sources) + "/" + encodeURIComponent(target) + "/" + lastUriComponent
      );
    }
  }, [sources, target, history, baseUrl, location.pathname]);

  return (
    <div className="is-flex is-justify-content-space-around is-align-items-center is-flex-wrap-wrap">
      <CompareSelector
        repository={repository}
        label={t("compare.selector.source")}
        onSelect={setSources}
        selected={sources}
      />
      <div className="is-flex is-flex-direction-column is-align-items-center">
        <span className="is-hidden-touch">{t("compare.selector.with")}</span>
        <Icon name="arrow-right" className="fa-lg mt-2" title={t("compare.selector.with")} />
      </div>
      <CompareSelector
        repository={repository}
        label={t("compare.selector.target")}
        onSelect={setTarget}
        selected={target}
      />
    </div>
  );
};

export default CompareSelectBar;
