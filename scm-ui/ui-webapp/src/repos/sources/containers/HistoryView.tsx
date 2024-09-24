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

import React, { FC, useState } from "react";
import { File, Repository } from "@scm-manager/ui-types";
import { ChangesetList, ErrorNotification, Loading, StatePaginator } from "@scm-manager/ui-components";
import { useHistory } from "@scm-manager/ui-api";

type Props = {
  file: File;
  repository: Repository;
  revision: string;
};

const HistoryView: FC<Props> = ({ repository, file, revision }) => {
  const [page, setPage] = useState(0);
  const { error, isLoading, data: history } = useHistory(repository, revision, file, { page });

  if (!history || isLoading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <div className="panel-block">
        <ChangesetList repository={repository} changesets={history?._embedded?.changesets || []} file={file} />
      </div>
      <div className="panel-footer">
        <StatePaginator page={page + 1} collection={history} updatePage={(newPage: number) => setPage(newPage - 1)} />
      </div>
    </>
  );
};

export default HistoryView;
