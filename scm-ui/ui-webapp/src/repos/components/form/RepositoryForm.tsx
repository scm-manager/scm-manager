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
import { Repository, RepositoryType, RepositoryImport } from "@scm-manager/ui-types";
import { Checkbox, InputField, Level, Select, SubmitButton, Textarea } from "@scm-manager/ui-components";
import * as validator from "./repositoryValidation";
import { CUSTOM_NAMESPACE_STRATEGY } from "../../modules/repos";
import { useLocation } from "react-router-dom";
import RepositoryFormSwitcher from "./RepositoryFormSwitcher";

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
  indexResources
}) => {
  const [repo, setRepo] = useState<RepositoryCreation>({
    name: "",
    namespace: "",
    type: "",
    contact: "",
    description: "",
    contextEntries: {},
    _links: {}
  });
  const [initRepository, setInitRepository] = useState(false);
  const [namespaceValidationError, setNamespaceValidationError] = useState(false);
  const [nameValidationError, setNameValidationError] = useState(false);
  const [contactValidationError, setContactValidationError] = useState(false);
  const [importUrl, setImportUrl] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const location = useLocation();
  const [t] = useTranslation("repos");

  useEffect(() => {
    if (repository) {
      setRepo({ ...repository, contextEntries: {} });
    }
  }, [repository]);

  const isValid = () => {
    return !(
      namespaceValidationError ||
      nameValidationError ||
      contactValidationError ||
      !repo.name ||
      (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY && !repo.namespace) ||
      (isImportPage() && !importUrl)
    );
  };

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid()) {
      if (importRepository && isImportPage()) {
        importRepository({ ...repo, url: importUrl, username, password });
      } else if (createRepository && isCreatePage()) {
        createRepository(repo, initRepository);
      } else if (modifyRepository) {
        modifyRepository(repo);
      }
    }
  };

  const isEditMode = () => {
    return !!repository;
  };

  const isModifiable = () => {
    return !!repository && !!repository._links.update;
  };

  const toggleInitCheckbox = () => {
    setInitRepository(!initRepository);
  };

  const setCreationContextEntry = (key: string, value: any) => {
    setRepo({
      ...repo,
      contextEntries: {
        ...repo.contextEntries,
        [key]: value
      }
    });
  };

  const resolveLocation = () => {
    const currentUrl = location.pathname;
    if (currentUrl.includes("/repos/create")) {
      return "create";
    }
    if (currentUrl.includes("/repos/import")) {
      return "import";
    }
    return "";
  };

  const isImportPage = () => resolveLocation() === "import";
  const isCreatePage = () => resolveLocation() === "create";

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
    if (!isImportPage()) {
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
            />
          </Column>
          <Column className="column is-half">
            <InputField
              label={t("import.username")}
              onChange={setUsername}
              value={username}
              helpText={t("help.usernameHelpText")}
            />
          </Column>
          <Column className="column is-half">
            <InputField
              label={t("import.password")}
              onChange={setPassword}
              value={password}
              type="password"
              helpText={t("help.passwordHelpText")}
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
        />
        <SpaceBetween>
          <SelectWrapper>
            <Select
              label={t("repository.type")}
              onChange={type => setRepo({ ...repo, type })}
              value={repo ? repo.type : ""}
              options={createSelectOptions(repositoryTypes)}
              helpText={t("help.typeHelpText")}
            />
          </SelectWrapper>
          {!isImportPage() && (
            <CheckboxWrapper>
              <Checkbox
                label={t("repositoryForm.initializeRepository")}
                checked={initRepository}
                onChange={toggleInitCheckbox}
                helpText={t("help.initializeRepository")}
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
      const match = url.match(/([^\/]+)\.git/i);
      if (match && match[1]) {
        handleNameChange(match[1]);
      }
    }
    setImportUrl(url);
  };

  const disabled = !isModifiable() && isEditMode();

  const getSubmitButtonTranslationKey = () =>
    isImportPage() ? "repositoryForm.submitImport" : "repositoryForm.submitCreate";

  const submitButton = disabled ? null : (
    <Level
      right={<SubmitButton disabled={!isValid()} loading={loading} label={t(getSubmitButtonTranslationKey())} />}
    />
  );

  return (
    <>
      {!isEditMode() && (
        <RepositoryFormSwitcher repository={repository} createMode={isImportPage() ? "IMPORT" : "CREATE"} />
      )}
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
        {submitButton}
      </form>
    </>
  );
};

export default RepositoryForm;
