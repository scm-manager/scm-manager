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

import React, { FC, FormEvent, useCallback, useEffect, useState } from "react";
import { Repository, RepositoryCreation, RepositoryType } from "@scm-manager/ui-types";
import { ErrorNotification, Level, SubmitButton, urls } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import ImportFromBundleForm from "./ImportFromBundleForm";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useImportRepositoryFromBundle } from "@scm-manager/ui-api";
import { useLocation } from "react-router-dom";

type Props = {
  repositoryType: RepositoryType;
  setImportPending: (pending: boolean) => void;
  setImportedRepository: (repository: Repository) => void;
  nameForm: extensionPoints.RepositoryCreatorComponentProps["nameForm"];
  informationForm: extensionPoints.RepositoryCreatorComponentProps["informationForm"];
};

const ImportRepositoryFromBundle: FC<Props> = ({
  repositoryType,
  setImportPending,
  setImportedRepository,
  nameForm: NameForm,
  informationForm: InformationForm
}) => {
  const location = useLocation();
  const [repo, setRepo] = useState<RepositoryCreation>({
    name: "",
    namespace: urls.getValueStringFromLocationByKey(location, "namespace") || "",
    type: repositoryType.name,
    contact: "",
    description: ""
  });
  const [password, setPassword] = useState("");
  const [valid, setValid] = useState({ namespaceAndName: false, contact: true, file: false });
  const [file, setFile] = useState<File | null>(null);
  const [compressed, setCompressed] = useState(true);
  const [t] = useTranslation("repos");
  const { importRepositoryFromBundle, importedRepository, error, isLoading } = useImportRepositoryFromBundle(
    repositoryType
  );
  const setContactValid = useCallback((contact: boolean) => setValid(currentValid => ({ ...currentValid, contact })), [
    setValid
  ]);
  const setNamespaceAndNameValid = useCallback(
    (namespaceAndName: boolean) => setValid(currentValid => ({ ...currentValid, namespaceAndName })),
    [setValid]
  );
  const setFileValid = useCallback((file: boolean) => setValid(currentValid => ({ ...currentValid, file })), [
    setValid
  ]);

  useEffect(() => setImportPending(isLoading), [isLoading, setImportPending]);
  useEffect(() => {
    if (importedRepository) {
      setImportedRepository(importedRepository);
    }
  }, [importedRepository, setImportedRepository]);

  const isValid = () => Object.values(valid).every(v => v);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!file) {
      throw new Error("File is required for import");
    }
    importRepositoryFromBundle({ ...repo, type: repositoryType.name }, file, compressed, password);
  };

  return (
    <form onSubmit={submit}>
      <ErrorNotification error={error} />
      <ImportFromBundleForm
        setFile={setFile}
        setValid={setFileValid}
        compressed={compressed}
        setCompressed={setCompressed}
        password={password}
        setPassword={setPassword}
        disabled={isLoading}
      />
      <hr />
      <NameForm
        repository={repo}
        onChange={setRepo as React.Dispatch<React.SetStateAction<RepositoryCreation>>}
        setValid={setNamespaceAndNameValid}
        disabled={isLoading}
      />
      <InformationForm
        repository={repo}
        onChange={setRepo as React.Dispatch<React.SetStateAction<RepositoryCreation>>}
        disabled={isLoading}
        setValid={setContactValid}
      />
      <Level
        right={<SubmitButton disabled={!isValid()} loading={isLoading} label={t("repositoryForm.submitImport")} />}
      />
    </form>
  );
};

export default ImportRepositoryFromBundle;
