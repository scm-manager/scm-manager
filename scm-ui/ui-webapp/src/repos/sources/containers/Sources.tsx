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
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
  RepositoryRevisionContextProvider,
  urls,
  useBranchesIfAvailable,
  useSources,
  useTagsIfAvailable,
} from "@scm-manager/ui-api";
import { Repository, Tag } from "@scm-manager/ui-types";
import { Breadcrumb, useScrollToElement, useShortcut } from "@scm-manager/ui-components";
import {
  ErrorNotification,
  Loading,
  Notification,
  useDocumentTitle,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  REPOSITORY_SUB_ROUTES,
  REPOSITORY_CODE_SUB_ROUTES,
} from "@scm-manager/ui-core";
import FileTree from "../components/FileTree";
import Content from "./Content";
import CodeActionBar from "../../codeSection/components/CodeActionBar";
import replaceBranchWithRevision from "../ReplaceBranchWithRevision";
import FileSearchButton from "../../codeSection/components/FileSearchButton";
import { isEmptyDirectory, isRootFile } from "../utils/files";
import CompareLink from "../../compare/CompareLink";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { getFileSearchLink } from "../../codeSection/utils/fileSearchLink";
import { encodeFilePath } from "../components/content/FileLink";
import { CompareTypes } from "../../compare/CompareSelectBar";

type Params = {
  sourceName?: string;
  "*": string;
};

const useUrlParams = () => {
  const { sourceName, "*": path } = useParams<Params>();
  return {
    sourceName,
    path: path || "",
  };
};

const Sources: FC = () => {
  const repository = useRepositoryFromParams();
  const { data: branchesData } = useBranchesIfAvailable(repository);
  const branches = branchesData?._embedded?.branches?.length === 0 ? undefined : branchesData?._embedded?.branches;
  const { data: tagsData } = useTagsIfAvailable(repository);
  const tags: Tag[] = (tagsData?._embedded?.tags as Tag[]) || undefined;
  const baseUrl = `${useCurrentRepositoryBaseUrl()}/${REPOSITORY_SUB_ROUTES.CODE}`;

  const { sourceName, path } = useUrlParams();

  const navigate = useNavigate();
  const location = useLocation();
  const [t] = useTranslation("repos");
  const getDocumentTitle = () => {
    if (sourceName) {
      const getRevision = () => {
        return branches?.some((branch) => branch.name === sourceName) ? sourceName : sourceName.slice(0, 7);
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
    if (branches && branches.length > 0 && !sourceName) {
      const defaultBranch = branches?.find((b) => b.defaultBranch === true);
      const targetName = defaultBranch ? defaultBranch.name : branches[0].name;
      navigate(`${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${encodeURIComponent(targetName)}/${location.hash}`, {
        replace: true,
      });
    }
  }, [branches, sourceName, navigate, baseUrl, location.hash]);

  const {
    isLoading,
    error,
    data: file,
    isFetchingNextPage,
    fetchNextPage,
  } = useSources(repository, {
    revision: sourceName,
    path,
    // we have to wait until a branch is selected,
    // except if we have no branches (svn)
    enabled: !branches || !!sourceName,
  });

  useShortcut(
    "g f",
    () => {
      if (file) {
        navigate(getFileSearchLink(repository, file.revision, baseUrl, file));
      }
    },
    {
      description: t("shortcuts.fileSearch"),
      active: !!(repository._links.paths && file),
    },
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

  const onSelect = (type: CompareTypes, name: string) => {
    if (!type || !name) return;

    let url = `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${encodeURIComponent(name)}/`;
    if (path) {
      url += `${encodeFilePath(path)}`;
      url = !url.endsWith("/") ? url + "/" : url;
    }
    navigate(`${url}${location.hash}`);
  };

  const evaluateSwitchViewLink = () => {
    if (branches && sourceName && branches.find((b) => b.name === sourceName)) {
      return `${baseUrl}/${REPOSITORY_SUB_ROUTES.BRANCH}/${encodeURIComponent(sourceName)}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/?${urls.createPrevSourcePathQuery(
        file.path,
      )}`;
    }
    if (tags && sourceName && tags.find((t) => t.name === sourceName)) {
      return `${baseUrl}/${REPOSITORY_SUB_ROUTES.TAG}/${encodeURIComponent(sourceName)}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/?${urls.createPrevSourcePathQuery(
        file.path,
      )}`;
    }
    if (sourceName) {
      return `${baseUrl}/${REPOSITORY_SUB_ROUTES.REVISION}/${encodeURIComponent(sourceName)}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/?${urls.createPrevSourcePathQuery(
        file.path,
      )}`;
    }

    if (repository.type === "svn") {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/?${urls.createPrevSourcePathQuery(`${file.revision}/${file.path}`)}`;
    }

    return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/`;
  };

  const renderBreadcrumb = () => {
    const permalink = file?.revision ? replaceBranchWithRevision(location.pathname, file.revision) : null;

    const buttons = [];
    if (repository._links.paths && file) {
      buttons.push(
        <FileSearchButton
          baseUrl={baseUrl}
          sourceName={sourceName || file.revision}
          currentSource={file}
          repository={repository}
          key={baseUrl}
        />,
      );
    }

    return (
      <Breadcrumb
        preButtons={buttons}
        repository={repository}
        revision={sourceName || file.revision}
        path={path || ""}
        baseUrl={`${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}`}
        branch={branches?.find((b) => b.name === sourceName)}
        defaultBranch={branches?.find((b) => b.defaultBranch === true)}
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
            <ExtensionPoint<extensionPoints.RepositoryCodeOverviewNoSourcesBanner>
              name="repository.code.overview.noSources.banner"
              props={{ repository }}
              renderAll={true}
            >
              <Notification type="info">{t("sources.noSources")}</Notification>
            </ExtensionPoint>
          </div>
        );
      } else {
        body = (
          <>
            <ExtensionPoint<extensionPoints.RepositoryCodeOverviewBanner>
              name="repository.code.overview.banner"
              props={{ sources: file, repository }}
              renderAll={true}
            />
            <FileTree
              repository={repository}
              directory={file}
              revision={sourceName || file.revision}
              baseUrl={`${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}`}
              isFetchingNextPage={isFetchingNextPage}
              fetchNextPage={fetchNextPage}
            />
          </>
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
        revision={sourceName || file.revision}
        breadcrumb={renderBreadcrumb()}
        error={error || undefined}
      />
    );
  };

  const hasBranchesWhenSupporting = (repository: Repository) => {
    return !repository._links.branches || (branches && branches.length !== 0);
  };

  return (
    <RepositoryRevisionContextProvider revision={sourceName}>
      <div ref={setContentRef}>
        {hasBranchesWhenSupporting(repository) && (
          <CodeActionBar
            hasContent={hasBranchesWhenSupporting(repository)}
            onSelect={onSelect}
            switchViewLink={evaluateSwitchViewLink()}
            actions={
              sourceName ? <CompareLink repository={repository} source={encodeURIComponent(sourceName)} /> : null
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
