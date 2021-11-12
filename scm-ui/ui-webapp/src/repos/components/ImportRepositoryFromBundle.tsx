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
import React, { FC, FormEvent, useEffect, useState } from "react";
import { Repository, RepositoryCreation, RepositoryType } from "@scm-manager/ui-types";
import { ErrorNotification, Level, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import ImportFromBundleForm from "./ImportFromBundleForm";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useImportRepositoryFromBundle } from "@scm-manager/ui-api";

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
  informationForm: InformationForm,
}) => {
  const [repo, setRepo] = useState<RepositoryCreation>({
    name: "",
    namespace: "",
    type: repositoryType.name,
    contact: "",
    description: "",
    contextEntries: [],
  });
  const [password, setPassword] = useState("");
  const [valid, setValid] = useState({ namespaceAndName: false, contact: true, file: false });
  const [file, setFile] = useState<File | null>(null);
  const [compressed, setCompressed] = useState(true);
  const [t] = useTranslation("repos");
  const { importRepositoryFromBundle, importedRepository, error, isLoading } =
    useImportRepositoryFromBundle(repositoryType);

  useEffect(() => setRepo({ ...repo, type: repositoryType.name }), [repositoryType]);
  useEffect(() => setImportPending(isLoading), [isLoading]);
  useEffect(() => {
    if (importedRepository) {
      setImportedRepository(importedRepository);
    }
  }, [importedRepository]);

  const isValid = () => Object.values(valid).every((v) => v);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    importRepositoryFromBundle(repo, file!, compressed, password);
  };

  return (
    <form onSubmit={submit}>
      <ErrorNotification error={error} />
      <ImportFromBundleForm
        setFile={setFile}
        setValid={(file: boolean) => setValid({ ...valid, file })}
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
        setValid={(namespaceAndName: boolean) => setValid({ ...valid, namespaceAndName })}
        disabled={isLoading}
      />
      <InformationForm
        repository={repo}
        onChange={setRepo as React.Dispatch<React.SetStateAction<RepositoryCreation>>}
        disabled={isLoading}
        setValid={(contact: boolean) => setValid({ ...valid, contact })}
      />
      <Level
        right={<SubmitButton disabled={!isValid()} loading={isLoading} label={t("repositoryForm.submitImport")} />}
      />
    </form>
  );
};

export default ImportRepositoryFromBundle;
