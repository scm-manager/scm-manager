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
import React, { FC, useEffect, useState } from "react";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Repository, RepositoryImport, RepositoryType } from "@scm-manager/ui-types";
import { Checkbox, InputField, Level, Select, SubmitButton, Textarea } from "@scm-manager/ui-components";
import * as validator from "./repositoryValidation";
import { CUSTOM_NAMESPACE_STRATEGY } from "../../modules/repos";

const CheckboxWrapper = styled.div`
  margin-top: 2em;
  flex: 1;
`;

const SelectWrapper = styled.div`
  flex: 1;
`;

const SpaceBetween = styled.div`
  display: flex;
  justify-content: space-between;
`;

const Column = styled.div`
  padding: 0 0.75rem;
`;

const Columns = styled.div`
  padding: 0.75rem 0 0;
`;

type Props = {
  createRepository?: (repo: RepositoryCreation, shouldInit: boolean) => void;
  importRepository?: (repo: RepositoryImport) => void;
  modifyRepository?: (repo: Repository) => void;
  repository?: Repository;
  repositoryTypes?: RepositoryType[];
  namespaceStrategy?: string;
  loading?: boolean;
  indexResources?: any;
  creationMode?: "CREATE" | "IMPORT";
};

type RepositoryCreation = Repository & {
  contextEntries: object;
};

const RepositoryForm: FC<Props> = ({
  createRepository,
  modifyRepository,
  importRepository,
  repository,
  repositoryTypes,
  namespaceStrategy,
  loading,
  indexResources,
  creationMode
}) => {
  const [repo, setRepo] = useState<Repository>({
    name: "",
    namespace: "",
    type: "",
    contact: "",
    description: "",
    _links: {}
  });
  const [initRepository, setInitRepository] = useState(false);
  const [namespaceValidationError, setNamespaceValidationError] = useState(false);
  const [nameValidationError, setNameValidationError] = useState(false);
  const [contactValidationError, setContactValidationError] = useState(false);
  const [contextEntries, setContextEntries] = useState({});
  const [importUrl, setImportUrl] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [t] = useTranslation("repos");

  useEffect(() => {
    if (repository) {
      setRepo({ ...repository });
    }
  }, [repository]);

  const isImportMode = () => creationMode === "IMPORT";
  const isCreateMode = () => creationMode === "CREATE";
  const isEditMode = () => !!repository;
  const isModifiable = () => !!repository && !!repository._links.update;
  const disabled = (!isModifiable() && isEditMode()) || loading;

  const isValid = () => {
    return !(
      namespaceValidationError ||
      nameValidationError ||
      contactValidationError ||
      !repo.name ||
      (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY && !repo.namespace) ||
      (isImportMode() && !importUrl)
    );
  };

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid()) {
      if (importRepository && isImportMode()) {
        importRepository({ ...repo, url: importUrl, username, password });
      } else if (createRepository && isCreateMode()) {
        createRepository({ ...repo, contextEntries }, initRepository);
      } else if (modifyRepository) {
        modifyRepository(repo);
      }
    }
  };

  const createSelectOptions = (repositoryTypes?: RepositoryType[]) => {
    if (repositoryTypes) {
      return repositoryTypes.map(repositoryType => {
        return {
          label: repositoryType.displayName,
          value: repositoryType.name
        };
      });
    }
    return [];
  };

  const renderNamespaceField = () => {
    const props = {
      label: t("repository.namespace"),
      helpText: t("help.namespaceHelpText"),
      value: repo ? repo.namespace : "",
      onChange: handleNamespaceChange,
      errorMessage: t("validation.namespace-invalid"),
      validationError: namespaceValidationError
    };

    if (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY) {
      return <InputField {...props} />;
    }

    return <ExtensionPoint name="repos.create.namespace" props={props} renderAll={false} />;
  };

  const renderUrlImportFields = () => {
    if (!isImportMode()) {
      return null;
    }

    return (
      <>
        <Columns className="columns is-multiline">
          <Column className="column is-full">
            <InputField
              label={t("import.importUrl")}
              onChange={handleImportUrlChange}
              value={importUrl}
              helpText={t("help.importUrlHelpText")}
              disabled={disabled}
            />
          </Column>
          <Column className="column is-half">
            <InputField
              label={t("import.username")}
              onChange={setUsername}
              value={username}
              helpText={t("help.usernameHelpText")}
              disabled={disabled}
            />
          </Column>
          <Column className="column is-half">
            <InputField
              label={t("import.password")}
              onChange={setPassword}
              value={password}
              type="password"
              helpText={t("help.passwordHelpText")}
              disabled={disabled}
            />
          </Column>
        </Columns>
        <hr />
      </>
    );
  };

  const renderCreateOnlyFields = () => {
    if (isEditMode()) {
      return null;
    }
    const extensionProps = {
      repository: repo,
      setCreationContextEntry: setCreationContextEntry,
      indexResources
    };
    return (
      <>
        {renderUrlImportFields()}
        {renderNamespaceField()}
        <InputField
          label={t("repository.name")}
          onChange={handleNameChange}
          value={repo ? repo.name : ""}
          validationError={nameValidationError}
          errorMessage={t("validation.name-invalid")}
          helpText={t("help.nameHelpText")}
          disabled={disabled}
        />
        <SpaceBetween>
          <SelectWrapper>
            <Select
              label={t("repository.type")}
              onChange={type => setRepo({ ...repo, type })}
              value={repo ? repo.type : ""}
              options={createSelectOptions(repositoryTypes)}
              helpText={t("help.typeHelpText")}
              disabled={disabled}
            />
          </SelectWrapper>
          {!isImportMode() && (
            <CheckboxWrapper>
              <Checkbox
                label={t("repositoryForm.initializeRepository")}
                checked={initRepository}
                onChange={toggleInitCheckbox}
                helpText={t("help.initializeRepository")}
                disabled={disabled}
              />
              {initRepository && (
                <ExtensionPoint name="repos.create.initialize" props={extensionProps} renderAll={true} />
              )}
            </CheckboxWrapper>
          )}
        </SpaceBetween>
      </>
    );
  };

  const toggleInitCheckbox = () => {
    setInitRepository(!initRepository);
  };

  const setCreationContextEntry = (key: string, value: any) => {
    setContextEntries({
      ...contextEntries,
      [key]: value
    });
  };

  const handleNamespaceChange = (namespace: string) => {
    setNamespaceValidationError(!validator.isNamespaceValid(namespace));
    setRepo({ ...repo, namespace });
  };

  const handleNameChange = (name: string) => {
    setNameValidationError(!validator.isNameValid(name));
    setRepo({ ...repo, name });
  };

  const handleContactChange = (contact: string) => {
    setContactValidationError(!validator.isContactValid(contact));
    setRepo({ ...repo, contact });
  };

  const handleImportUrlChange = (url: string) => {
    if (!repo.name) {
      // If the repository name is not fill we set a name suggestion
      const match = url.match(/([^\/]+)\.git/i);
      if (match && match[1]) {
        handleNameChange(match[1]);
      }
    }
    setImportUrl(url);
  };

  const submitButton = () => {
    if (disabled) {
      return null;
    }
    const translationKey = isImportMode() ? "repositoryForm.submitImport" : "repositoryForm.submitCreate";
    return <Level right={<SubmitButton disabled={!isValid()} loading={loading} label={t(translationKey)} />} />;
  };

  return (
    <>
      <form onSubmit={submit}>
        {renderCreateOnlyFields()}
        <InputField
          label={t("repository.contact")}
          onChange={handleContactChange}
          value={repo ? repo.contact : ""}
          validationError={contactValidationError}
          errorMessage={t("validation.contact-invalid")}
          helpText={t("help.contactHelpText")}
          disabled={disabled}
        />
        <Textarea
          label={t("repository.description")}
          onChange={description => setRepo({ ...repo, description })}
          value={repo ? repo.description : ""}
          helpText={t("help.descriptionHelpText")}
          disabled={disabled}
        />
        {submitButton()}
      </form>
    </>
  );
};

export default RepositoryForm;
