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
import { useTranslation } from "react-i18next";
import { Changeset, Repository } from "@scm-manager/ui-types";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import ChangesetDetails from "../components/changesets/ChangesetDetails";
import { FileControlFactory } from "@scm-manager/ui-components";
import { RepositoryRevisionContextProvider, useChangeset } from "@scm-manager/ui-api";
import { useParams } from "react-router-dom";

type Props = {
  repository: Repository;
  fileControlFactoryFactory?: (changeset: Changeset) => FileControlFactory;
};

type Params = {
  id: string;
};

const ChangesetView: FC<Props> = ({ repository, fileControlFactoryFactory }) => {
  const { id } = useParams<Params>();
  const { isLoading, error, data: changeset } = useChangeset(repository, id);
  const [t] = useTranslation("repos");

  if (error) {
    return <ErrorPage title={t("changesets.errorTitle")} subtitle={t("changesets.errorSubtitle")} error={error} />;
  }

  if (!changeset || isLoading) {
    return <Loading />;
  }

  return (
    <RepositoryRevisionContextProvider revision={changeset.id}>
      <ChangesetDetails
        changeset={changeset}
        repository={repository}
        fileControlFactory={fileControlFactoryFactory && fileControlFactoryFactory(changeset)}
      />
    </RepositoryRevisionContextProvider>
  );
};

export default ChangesetView;
