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
import { Repository } from "@scm-manager/ui-types";
import { useIncomingChangesets } from "@scm-manager/ui-api";
import { ChangesetsPanel, usePage } from "../containers/Changesets";

type Props = {
  repository: Repository;
  source: string;
  target: string;
  url: string;
};

const IncomingChangesets: FC<Props> = ({ repository, source, target, url }) => {
  const page = usePage();
  const { data, error, isLoading } = useIncomingChangesets(repository, source, target, { page: page - 1, limit: 25 });

  return <ChangesetsPanel repository={repository} error={error} isLoading={isLoading} data={data} url={url} />;
};

export default IncomingChangesets;
