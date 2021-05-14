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
import React, { FC, FormEvent, useState } from "react";
import { RepositoryCreation, RepositoryUrlImport } from "@scm-manager/ui-types";
import ImportFromUrlForm from "./ImportFromUrlForm";
import { apiClient, ErrorNotification, Level, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { SubFormProps } from "../types";

type Props = {
  url: string;
  repositoryType: string;
  setImportPending: (pending: boolean) => void;
  nameForm: React.ComponentType<SubFormProps>;
  informationForm: React.ComponentType<SubFormProps>;
};

const ImportRepositoryFromUrl: FC<Props> = ({
  url,
  repositoryType,
  setImportPending,
  nameForm: NameForm,
  informationForm: InformationForm
}) => {
  const [repo, setRepo] = useState<RepositoryUrlImport>({
    name: "",
    namespace: "",
    type: repositoryType,
    contact: "",
    description: "",
    importUrl: "",
    username: "",
    password: "",
    contextEntries: []
  });

  const [valid, setValid] = useState({ namespaceAndName: false, contact: true, importUrl: false });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | undefined>();
  const history = useHistory();
  const [t] = useTranslation("repos");

  const isValid = () => Object.values(valid).every(v => v);

  const handleImportLoading = (loading: boolean) => {
    setImportPending(loading);
    setLoading(loading);
  };

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(undefined);
    const currentPath = history.location.pathname;
    handleImportLoading(true);
    apiClient
      .post(url, repo, "application/vnd.scmm-repository+json;v=2")
      .then(response => {
        const location = response.headers.get("Location");
        return apiClient.get(location!);
      })
      .then(response => response.json())
      .then(repo => {
        handleImportLoading(false);
        if (history.location.pathname === currentPath) {
          history.push(`/repo/${repo.namespace}/${repo.name}/code/sources`);
        }
      })
      .catch(error => {
        setError(error);
        handleImportLoading(false);
      });
  };

  return (
    <form onSubmit={submit}>
      <ErrorNotification error={error} />
      <ImportFromUrlForm
        repository={repo}
        onChange={setRepo}
        setValid={(importUrl: boolean) => setValid({ ...valid, importUrl })}
        disabled={loading}
      />
      <hr />
      <NameForm
        repository={repo}
        onChange={setRepo as React.Dispatch<React.SetStateAction<RepositoryCreation>>}
        setValid={(namespaceAndName: boolean) => setValid({ ...valid, namespaceAndName })}
        disabled={loading}
      />
      <InformationForm
        repository={repo}
        onChange={setRepo as React.Dispatch<React.SetStateAction<RepositoryCreation>>}
        disabled={loading}
        setValid={(contact: boolean) => setValid({ ...valid, contact })}
      />
      <Level
        right={<SubmitButton disabled={!isValid()} loading={loading} label={t("repositoryForm.submitImport")} />}
      />
    </form>
  );
};

export default ImportRepositoryFromUrl;
