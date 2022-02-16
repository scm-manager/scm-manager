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
