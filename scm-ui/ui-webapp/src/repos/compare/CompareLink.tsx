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
