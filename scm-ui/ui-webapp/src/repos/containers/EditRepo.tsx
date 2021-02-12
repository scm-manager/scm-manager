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
import { Redirect, useRouteMatch } from "react-router-dom";
import RepositoryForm from "../components/form";
import { Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Subtitle } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import RepositoryDangerZone from "./RepositoryDangerZone";
import { urls } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import ExportRepository from "./ExportRepository";
import { useIndexLinks, useUpdateRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
};

const EditRepo: FC<Props> = ({ repository }) => {
  const match = useRouteMatch();
  const { isLoading, error, update, isUpdated } = useUpdateRepository();
  const indexLinks = useIndexLinks();
  const [t] = useTranslation("repos");

  if (isUpdated) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}`} />;
  }

  const url = urls.matchedUrlFromMatch(match);
  const extensionProps = {
    repository,
    url
  };

  return (
    <>
      <Subtitle subtitle={t("repositoryForm.subtitle")} />
      <ErrorNotification error={error} />
      <RepositoryForm repository={repository} loading={isLoading} modifyRepository={update} />
      <ExportRepository repository={repository} />
      <ExtensionPoint name="repo-config.route" props={extensionProps} renderAll={true} />
      <RepositoryDangerZone repository={repository} indexLinks={indexLinks} />
    </>
  );
};

export default EditRepo;
