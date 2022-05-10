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
import React, { FC, FormEvent, useCallback, useEffect, useState } from "react";
import { Repository, RepositoryCreation, RepositoryType } from "@scm-manager/ui-types";
import { ErrorNotification, Level, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import ImportFullRepositoryForm from "./ImportFullRepositoryForm";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useImportFullRepository } from "@scm-manager/ui-api";

type Props = {
  repositoryType: RepositoryType;
  setImportPending: (pending: boolean) => void;
  setImportedRepository: (repository: Repository) => void;
  nameForm: extensionPoints.RepositoryCreatorComponentProps["nameForm"];
  informationForm: extensionPoints.RepositoryCreatorComponentProps["informationForm"];
};

const ImportFullRepository: FC<Props> = ({
  repositoryType,
  setImportPending,
  setImportedRepository,
  nameForm: NameForm,
  informationForm: InformationForm,
}) => {
  const [repo, setRepo] = useState<RepositoryCreation>({
    name: "",
    namespace: "",
    type: repositoryType.name,
    contact: "",
    description: "",
  });
  const [password, setPassword] = useState("");
  const [valid, setValid] = useState({ namespaceAndName: false, contact: true, file: false });
  const [file, setFile] = useState<File | null>(null);
  const [t] = useTranslation("repos");
  const { importFullRepository, importedRepository, isLoading, error } = useImportFullRepository(repositoryType);
  const setContactValid = useCallback(
    (contact: boolean) => setValid((currentValid) => ({ ...currentValid, contact })),
    [setValid]
  );
  const setNamespaceAndNameValid = useCallback(
    (namespaceAndName: boolean) => setValid((currentValid) => ({ ...currentValid, namespaceAndName })),
    [setValid]
  );
  const setFileValid = useCallback(
    (file: boolean) => setValid((currentValid) => ({ ...currentValid, file })),
    [setValid]
  );

  useEffect(() => setImportPending(isLoading), [isLoading, setImportPending]);
  useEffect(() => {
    if (importedRepository) {
      setImportedRepository(importedRepository);
    }
  }, [importedRepository, setImportedRepository]);

  const isValid = () => Object.values(valid).every((v) => v);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!file) {
      throw new Error("File is required for import");
    }
    importFullRepository({ ...repo, type: repositoryType.name }, file, password);
  };

  return (
    <form onSubmit={submit}>
      <ErrorNotification error={error} />
      <ImportFullRepositoryForm
        setFile={setFile}
        password={password}
        setPassword={setPassword}
        setValid={setFileValid}
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

export default ImportFullRepository;
