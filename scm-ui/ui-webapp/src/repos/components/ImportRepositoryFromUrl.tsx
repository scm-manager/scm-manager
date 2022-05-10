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
import { Repository, RepositoryCreation, RepositoryType, RepositoryUrlImport } from "@scm-manager/ui-types";
import ImportFromUrlForm from "./ImportFromUrlForm";
import { ErrorNotification, Level, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useImportRepositoryFromUrl } from "@scm-manager/ui-api";
import { extensionPoints } from "@scm-manager/ui-extensions";

type Props = {
  repositoryType: RepositoryType;
  setImportPending: (pending: boolean) => void;
  setImportedRepository: (repository: Repository) => void;
  nameForm: extensionPoints.RepositoryCreatorComponentProps["nameForm"];
  informationForm: extensionPoints.RepositoryCreatorComponentProps["informationForm"];
};

const ImportRepositoryFromUrl: FC<Props> = ({
  repositoryType,
  setImportPending,
  setImportedRepository,
  nameForm: NameForm,
  informationForm: InformationForm,
}) => {
  const [repo, setRepo] = useState<RepositoryUrlImport>({
    name: "",
    namespace: "",
    type: repositoryType.name,
    contact: "",
    description: "",
    importUrl: "",
    username: "",
    password: "",
  });

  const [valid, setValid] = useState({ namespaceAndName: false, contact: true, importUrl: false });
  const [t] = useTranslation("repos");
  const { importRepositoryFromUrl, importedRepository, error, isLoading } = useImportRepositoryFromUrl(repositoryType);
  const setContactValid = useCallback(
    (contact: boolean) => setValid((currentValid) => ({ ...currentValid, contact })),
    [setValid]
  );
  const setNamespaceAndNameValid = useCallback(
    (namespaceAndName: boolean) => setValid((currentValid) => ({ ...currentValid, namespaceAndName })),
    [setValid]
  );
  const setImportUrlValid = useCallback(
    (importUrl: boolean) => setValid((currentValid) => ({ ...currentValid, importUrl })),
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
    importRepositoryFromUrl({ ...repo, type: repositoryType.name });
  };

  return (
    <form onSubmit={submit}>
      {error ? <ErrorNotification error={error} /> : null}
      <ImportFromUrlForm repository={repo} onChange={setRepo} setValid={setImportUrlValid} disabled={isLoading} />
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

export default ImportRepositoryFromUrl;
