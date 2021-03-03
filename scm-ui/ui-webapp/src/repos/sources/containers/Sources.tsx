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
import React, { FC, useEffect } from "react";
import { Branch, Repository } from "@scm-manager/ui-types";
import { Breadcrumb, ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import FileTree from "../components/FileTree";
import Content from "./Content";
import CodeActionBar from "../../codeSection/components/CodeActionBar";
import replaceBranchWithRevision from "../ReplaceBranchWithRevision";
import { useSources } from "@scm-manager/ui-api";
import { useHistory, useLocation, useParams } from "react-router-dom";
import { isEmptyDirectory, isRootFile } from "../utils/files";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
  branches?: Branch[];
  selectedBranch?: string;
  baseUrl: string;
};

type Params = {
  revision?: string;
  path: string;
};

const useUrlParams = () => {
  const { revision, path } = useParams<Params>();
  return {
    revision: revision ? decodeURIComponent(revision) : undefined,
    path: path || ""
  };
};

const Sources: FC<Props> = ({ repository, branches, selectedBranch, baseUrl }) => {
  const { revision, path } = useUrlParams();
  const history = useHistory();
  const location = useLocation();
  const [t] = useTranslation("repos");
  // redirect to default branch is non branch selected
  useEffect(() => {
    if (branches && branches.length > 0 && !selectedBranch) {
      const defaultBranch = branches?.filter(b => b.defaultBranch === true)[0];
      history.replace(`${baseUrl}/sources/${encodeURIComponent(defaultBranch.name)}/`);
    }
  }, [branches, selectedBranch]);
  const { isLoading, error, data: file, isFetchingNextPage, fetchNextPage } = useSources(repository, {
    revision,
    path,
    // we have to wait until a branch is selected,
    // expect if we have no branches (svn)
    enabled: !branches || !!selectedBranch
  });

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || !file) {
    return <Loading />;
  }

  const onSelectBranch = (branch?: Branch) => {
    let url;
    if (branch) {
      if (path) {
        url = `${baseUrl}/sources/${encodeURIComponent(branch.name)}/${path}`;
        url = !url.endsWith("/") ? url + "/" : url;
      } else {
        url = `${baseUrl}/sources/${encodeURIComponent(branch.name)}/`;
      }
    } else {
      return;
    }
    history.push(url);
  };

  const evaluateSwitchViewLink = () => {
    if (branches && selectedBranch && branches?.filter(b => b.name === selectedBranch).length !== 0) {
      return `${baseUrl}/branch/${encodeURIComponent(selectedBranch)}/changesets/`;
    }
    return `${baseUrl}/changesets/`;
  };

  const renderBreadcrumb = () => {
    const permalink = file?.revision ? replaceBranchWithRevision(location.pathname, file.revision) : null;

    return (
      <Breadcrumb
        repository={repository}
        revision={revision || file.revision}
        path={path || ""}
        baseUrl={baseUrl + "/sources"}
        branch={branches?.filter(b => b.name === selectedBranch)[0]}
        defaultBranch={branches?.filter(b => b.defaultBranch === true)[0]}
        sources={file}
        permalink={permalink}
      />
    );
  };

  if (file.directory) {
    let body;
    if (isRootFile(file) && isEmptyDirectory(file)) {
      body = (
        <div className="panel-block">
          <Notification type="info">{t("sources.noSources")}</Notification>
        </div>
      );
    } else {
      body = (
        <>
          {renderBreadcrumb()}
          <FileTree
            directory={file}
            revision={revision || file.revision}
            baseUrl={baseUrl + "/sources"}
            isFetchingNextPage={isFetchingNextPage}
            fetchNextPage={fetchNextPage}
          />
        </>
      );
    }

    return (
      <>
        <CodeActionBar
          selectedBranch={selectedBranch}
          branches={branches}
          onSelectBranch={onSelectBranch}
          switchViewLink={evaluateSwitchViewLink()}
        />
        <div className="panel">{body}</div>
      </>
    );
  } else {
    return (
      <>
        <CodeActionBar
          selectedBranch={selectedBranch}
          branches={branches}
          onSelectBranch={onSelectBranch}
          switchViewLink={evaluateSwitchViewLink()}
        />
        <Content
          file={file}
          repository={repository}
          revision={revision || file.revision}
          path={path}
          breadcrumb={renderBreadcrumb()}
        />
      </>
    );
  }
};

export default Sources;
