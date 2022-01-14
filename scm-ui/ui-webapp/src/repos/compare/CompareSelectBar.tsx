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

export type CompareTypes = "b" | "t" | "r";

export type CompareFunction = (type: CompareTypes, name: string) => void;
export type CompareProps = {
  type: CompareTypes;
  name: string;
};

const CompareSelectBar: FC<Props> = ({ repository, baseUrl }) => {
  const [t] = useTranslation("repos");
  const match = useRouteMatch<CompareBranchesParams>();
  const location = useLocation();
  const history = useHistory();
  const [source, setSource] = useState<CompareProps>({
    type: match?.params?.sourceType as CompareTypes,
    name: decodeURIComponent(match?.params?.sourceName)
  });
  const [target, setTarget] = useState<CompareProps>({
    type: match?.params?.targetType as CompareTypes,
    name: decodeURIComponent(match?.params?.targetName)
  });

  useEffect(() => {
    const tabUriComponent = location.pathname.split("/")[9];
    if (source && target && tabUriComponent) {
      history.push(
        baseUrl +
          "/" +
          source.type +
          "/" +
          encodeURIComponent(source.name) +
          "/" +
          target.type +
          "/" +
          encodeURIComponent(target.name) +
          "/" +
          tabUriComponent +
          "/"
      );
    }
  }, [history, baseUrl, source, target]);

  return (
    <div className="is-flex is-justify-content-space-around is-align-items-center is-flex-wrap-wrap">
      <CompareSelector
        repository={repository}
        label={t("compare.selector.source")}
        onSelect={(type, name) => setSource({ type, name })}
        selected={source}
      />
      <Icon name="arrow-right" className="fa-lg mt-2" title={t("compare.selector.with")} />
      <CompareSelector
        repository={repository}
        label={t("compare.selector.target")}
        onSelect={(type, name) => setTarget({ type, name })}
        selected={target}
      />
    </div>
  );
};

export default CompareSelectBar;
