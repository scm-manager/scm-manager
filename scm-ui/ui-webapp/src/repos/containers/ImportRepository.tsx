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
  informationForm
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
