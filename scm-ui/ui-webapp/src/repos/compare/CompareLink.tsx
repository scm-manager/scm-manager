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
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";
import { CompareTypes } from "./CompareSelectBar";

type Props = {
  repository: Repository;
  source: string;
  sourceType?: CompareTypes;
  target?: string;
  targetType?: CompareTypes;
};

const CompareLink: FC<Props> = ({ repository, source, sourceType = "b", target, targetType = "b" }) => {
  const [t] = useTranslation("repos");

  const icon = <Icon name="retweet" title={t("compare.linkTitle")} color="inherit" className="mr-2" />;

  if (!target) {
    return (
      <Link to={`/repo/${repository.namespace}/${repository.name}/compare/${sourceType}/${source}`} className="px-1">
        {icon}
      </Link>
    );
  }

  return (
    <Link
      to={`/repo/${repository.namespace}/${repository.name}/compare/${sourceType}/${source}/${targetType}/${target}`}
    >
      {icon}
    </Link>
  );
};

export default CompareLink;
