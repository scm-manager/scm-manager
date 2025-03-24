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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation, useParams } from "react-router-dom";
import { RepositoryRevisionContextProvider, urls, useSources } from "@scm-manager/ui-api";
import { Branch, Repository } from "@scm-manager/ui-types";
import { Breadcrumb, useScrollToElement, useShortcut } from "@scm-manager/ui-components";
import { ErrorNotification, Loading, Notification, useDocumentTitle } from "@scm-manager/ui-core";
import FileTree from "../components/FileTree";
import Content from "./Content";
import CodeActionBar from "../../codeSection/components/CodeActionBar";
import replaceBranchWithRevision from "../ReplaceBranchWithRevision";
import FileSearchButton from "../../codeSection/components/FileSearchButton";
import { isEmptyDirectory, isRootFile } from "../utils/files";
import CompareLink from "../../compare/CompareLink";
import { encodePart } from "../components/content/FileLink";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { getFileSearchLink } from "../../codeSection/utils/fileSearchLink";

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
    path: path || "",
  };
};

const Sources: FC<Props> = ({ repository, branches, selectedBranch, baseUrl }) => {
  const { revision, path } = useUrlParams();
  const history = useHistory();
  const location = useLocation();
  const [t] = useTranslation("repos");
  const getDocumentTitle = () => {
    if (revision) {
      const getRevision = () => {
        return branches?.some((branch) => branch.name === revision) ? revision : revision.slice(0, 7);
      };
      if (path) {
        return t("sources.pathWithRevisionAndNamespaceName", {
          path: path,
          revision: getRevision(),
          namespace: repository.namespace,
          name: repository.name,
        });
      } else {
        return t("sources.sourcesWithRevisionAndNamespaceName", {
          revision: getRevision(),
          namespace: repository.namespace,
          name: repository.name,
        });
      }
    } else {
      return repository.namespace + "/" + repository.name;
    }
  };
  useDocumentTitle(getDocumentTitle());
  const [contentRef, setContentRef] = useState<HTMLElement | null>();

  useScrollToElement(contentRef, () => location.hash, location.hash);

  // redirect to default branch if no branch selected
  useEffect(() => {
    if (branches && branches.length > 0 && !selectedBranch) {
      const defaultBranch = branches?.filter((b) => b.defaultBranch === true)[0];
      history.replace(
        `${baseUrl}/sources/${defaultBranch ? encodePart(defaultBranch.name) : encodePart(branches[0].name)}/${
          location.hash
        }`
      );
    }
  }, [branches, selectedBranch, history, baseUrl, location.hash]);
  const {
    isLoading,
    error,
    data: file,
    isFetchingNextPage,
    fetchNextPage,
  } = useSources(repository, {
    revision,
    path,
    // we have to wait until a branch is selected,
    // expect if we have no branches (svn)
    enabled: !branches || !!selectedBranch,
  });

  useShortcut(
    "g f",
    () => {
      if (file) {
        history.push(getFileSearchLink(repository, file.revision, baseUrl, file));
      }
    },
    {
      description: t("shortcuts.fileSearch"),
      active: !!(repository._links.paths && file),
    }
  );

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || (!error && !file)) {
    return <Loading />;
  }

  if (!file) {
    return null;
  }

  const onSelectBranch = (branch?: Branch) => {
    let url;
    if (branch) {
      if (path) {
        url = `${baseUrl}/sources/${encodePart(branch.name)}/${path}`;
        url = !url.endsWith("/") ? url + "/" : url;
      } else {
        url = `${baseUrl}/sources/${encodePart(branch.name)}/`;
      }
    } else {
      return;
    }
    history.push(`${url}${location.hash}`);
  };

  const evaluateSwitchViewLink = () => {
    if (branches && selectedBranch && branches?.filter((b) => b.name === selectedBranch).length !== 0) {
      return `${baseUrl}/branch/${encodeURIComponent(selectedBranch)}/changesets/?${urls.createPrevSourcePathQuery(
        file.path
      )}`;
    }

    if (repository.type === "svn") {
      return `${baseUrl}/changesets/?${urls.createPrevSourcePathQuery(`${file.revision}/${file.path}`)}`;
    }

    return `${baseUrl}/changesets/`;
  };

  const renderBreadcrumb = () => {
    const permalink = file?.revision ? replaceBranchWithRevision(location.pathname, file.revision) : null;

    const buttons = [];
    if (repository._links.paths && file) {
      buttons.push(
        <FileSearchButton
          baseUrl={baseUrl}
          revision={revision || file.revision}
          currentSource={file}
          repository={repository}
        />
      );
    }

    return (
      <Breadcrumb
        preButtons={buttons}
        repository={repository}
        revision={revision || file.revision}
        path={path || ""}
        baseUrl={baseUrl + "/sources"}
        branch={branches?.filter((b) => b.name === selectedBranch)[0]}
        defaultBranch={branches?.filter((b) => b.defaultBranch === true)[0]}
        sources={file}
        permalink={permalink}
      />
    );
  };

  const renderPanelContent = () => {
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
          <FileTree
            repository={repository}
            directory={file}
            revision={revision || file.revision}
            baseUrl={baseUrl + "/sources"}
            isFetchingNextPage={isFetchingNextPage}
            fetchNextPage={fetchNextPage}
          />
        );
      }

      return (
        <div className="panel">
          {renderBreadcrumb()}
          {body}
        </div>
      );
    }

    return (
      <Content
        file={file}
        repository={repository}
        revision={revision || file.revision}
        breadcrumb={renderBreadcrumb()}
        error={error || undefined}
      />
    );
  };

  const hasBranchesWhenSupporting = (repository: Repository) => {
    return !repository._links.branches || (branches && branches.length !== 0);
  };

  return (
    <RepositoryRevisionContextProvider revision={revision}>
      <div ref={setContentRef}>
        {hasBranchesWhenSupporting(repository) && (
          <CodeActionBar
            selectedBranch={selectedBranch}
            branches={branches}
            onSelectBranch={onSelectBranch}
            switchViewLink={evaluateSwitchViewLink()}
            actions={
              branches && selectedBranch ? (
                <CompareLink repository={repository} source={encodeURIComponent(selectedBranch)} />
              ) : null
            }
          />
        )}
        {renderPanelContent()}
        <ExtensionPoint<extensionPoints.RepositoryCodeOverviewContent>
          name="repository.code.overview.content"
          props={{ sources: file, repository }}
          renderAll={true}
        />
      </div>
    </RepositoryRevisionContextProvider>
  );
};

export default Sources;
