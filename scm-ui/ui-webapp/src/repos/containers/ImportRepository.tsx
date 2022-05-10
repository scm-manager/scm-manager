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

import React, { useState } from "react";
import { Link, Repository, RepositoryType } from "@scm-manager/ui-types";

import { useTranslation } from "react-i18next";
import ImportRepositoryTypeSelect from "../components/ImportRepositoryTypeSelect";
import ImportTypeSelect from "../components/ImportTypeSelect";
import ImportRepositoryFromUrl from "../components/ImportRepositoryFromUrl";
import { Loading, Notification, useNavigationLock } from "@scm-manager/ui-components";

import ImportRepositoryFromBundle from "../components/ImportRepositoryFromBundle";
import ImportFullRepository from "../components/ImportFullRepository";
import { Prompt, Redirect } from "react-router-dom";
import { extensionPoints } from "@scm-manager/ui-extensions";

const ImportPendingLoading = ({ importPending }: { importPending: boolean }) => {
  const [t] = useTranslation("repos");
  if (!importPending) {
    return null;
  }
  return (
    <>
      <Notification type="info">{t("import.pending.infoText")}</Notification>
      <Loading />
      <hr />
    </>
  );
};

const ImportRepository: extensionPoints.RepositoryCreatorExtension["component"] = ({
  repositoryTypes,
  nameForm,
  informationForm,
}) => {
  const [importPending, setImportPending] = useState(false);
  const [importedRepository, setImportedRepository] = useState<Repository>();
  const [repositoryType, setRepositoryType] = useState<RepositoryType | undefined>();
  const [importType, setImportType] = useState("");
  const [t] = useTranslation("repos");

  useNavigationLock(importPending);

  const changeRepositoryType = (repositoryType: RepositoryType) => {
    setRepositoryType(repositoryType);
    setImportType(((repositoryType._links?.import as Link[])[0] as Link)?.name || "");
  };

  const renderImportComponent = () => {
    if (!repositoryType) {
      throw new Error("Missing import type");
    }
    if (importType === "url") {
      return (
        <ImportRepositoryFromUrl
          repositoryType={repositoryType}
          setImportPending={setImportPending}
          setImportedRepository={setImportedRepository}
          nameForm={nameForm}
          informationForm={informationForm}
        />
      );
    }

    if (importType === "bundle") {
      return (
        <ImportRepositoryFromBundle
          repositoryType={repositoryType}
          setImportPending={setImportPending}
          setImportedRepository={setImportedRepository}
          nameForm={nameForm}
          informationForm={informationForm}
        />
      );
    }

    if (importType === "fullImport") {
      return (
        <ImportFullRepository
          repositoryType={repositoryType}
          setImportPending={setImportPending}
          setImportedRepository={setImportedRepository}
          nameForm={nameForm}
          informationForm={informationForm}
        />
      );
    }
    throw new Error("Unknown import type");
  };

  if (importedRepository) {
    return <Redirect to={`/repo/${importedRepository.namespace}/${importedRepository.name}/code/sources`} />;
  }

  return (
    <>
      <Prompt when={importPending} message={t("import.navigationWarning")} />
      <ImportPendingLoading importPending={importPending} />
      <ImportRepositoryTypeSelect
        repositoryTypes={repositoryTypes?._embedded?.repositoryTypes || []}
        repositoryType={repositoryType}
        setRepositoryType={changeRepositoryType}
        disabled={importPending}
      />
      {repositoryType && (
        <>
          <hr />
          <ImportTypeSelect
            repositoryType={repositoryType}
            importType={importType}
            setImportType={setImportType}
            disabled={importPending}
          />
          <hr />
        </>
      )}
      {importType && renderImportComponent()}
    </>
  );
};

export default ImportRepository;
