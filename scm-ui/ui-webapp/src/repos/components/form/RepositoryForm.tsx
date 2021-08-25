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
import React, { FC, useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { IndexResources, Repository, RepositoryType, CUSTOM_NAMESPACE_STRATEGY } from "@scm-manager/ui-types";
import { Checkbox, Level, Select, SubmitButton } from "@scm-manager/ui-components";
import NamespaceAndNameFields from "../NamespaceAndNameFields";
import RepositoryInformationForm from "../RepositoryInformationForm";

const FlexibleDiv = styled.div`
  flex: 1;
`;

type Props = {
  createRepository?: (repo: RepositoryCreation, shouldInit: boolean) => void;
  modifyRepository?: (repo: Repository) => void;
  repository?: Repository;
  repositoryTypes?: RepositoryType[];
  namespaceStrategy?: string;
  loading: boolean;
  indexResources?: IndexResources;
};

type RepositoryCreation = Repository & {
  contextEntries: object;
};

const RepositoryForm: FC<Props> = ({
  createRepository,
  modifyRepository,
  repository,
  repositoryTypes,
  namespaceStrategy,
  loading,
  indexResources,
}) => {
  const [repo, setRepo] = useState<Repository>({
    name: "",
    namespace: "",
    type: "",
    contact: "",
    description: "",
    _links: {},
  });
  const [initRepository, setInitRepository] = useState(false);
  const [contextEntries, setContextEntries] = useState({});
  const setCreationContextEntry = useCallback(
    (key: string, value: any) => {
      setContextEntries((entries) => ({
        ...entries,
        [key]: value,
      }));
    },
    [setContextEntries]
  );
  const [valid, setValid] = useState({ namespaceAndName: true, contact: true });
  const [t] = useTranslation("repos");

  useEffect(() => {
    if (repository) {
      setRepo({ ...repository });
    }
  }, [repository]);

  const isEditMode = () => !!repository;
  const isModifiable = () => !!repository && !!repository._links.update;
  const disabled = (!isModifiable() && isEditMode()) || loading;

  const isValid = () => {
    return (
      !(!repo.name || (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY && !repo.namespace)) &&
      Object.values(valid).every((v) => v)
    );
  };

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid()) {
      if (createRepository) {
        createRepository({ ...repo, contextEntries }, initRepository);
      } else if (modifyRepository) {
        modifyRepository(repo);
      }
    }
  };

  const createSelectOptions = (repositoryTypes?: RepositoryType[]) => {
    if (repositoryTypes) {
      return repositoryTypes.map((repositoryType) => {
        return {
          label: repositoryType.displayName,
          value: repositoryType.name,
        };
      });
    }
    return [];
  };

  const renderCreateOnlyFields = () => {
    if (isEditMode()) {
      return null;
    }
    const indexResourcesWithLinks = { ...indexResources, links: indexResources?._links };
    const extensionProps = {
      repository: repo,
      setCreationContextEntry: setCreationContextEntry,
      indexResources: indexResourcesWithLinks,
    };
    return (
      <>
        <NamespaceAndNameFields
          repository={repo}
          onChange={setRepo}
          setValid={(namespaceAndName) => setValid({ ...valid, namespaceAndName })}
          disabled={disabled}
        />
        <div className="is-flex is-justify-content-space-between">
          <FlexibleDiv>
            <Select
              label={t("repository.type")}
              onChange={(type) => setRepo({ ...repo, type })}
              value={repo ? repo.type : ""}
              options={createSelectOptions(repositoryTypes)}
              helpText={t("help.typeHelpText")}
              disabled={disabled}
            />
          </FlexibleDiv>
          <FlexibleDiv className="mt-5">
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
          </FlexibleDiv>
        </div>
      </>
    );
  };

  const toggleInitCheckbox = () => {
    setInitRepository(!initRepository);
  };

  const submitButton = () => {
    if (!isModifiable() && isEditMode()) {
      return null;
    }
    return (
      <Level
        right={
          <SubmitButton disabled={!isValid() || loading} loading={loading} label={t("repositoryForm.submitCreate")} />
        }
      />
    );
  };

  return (
    <form onSubmit={submit}>
      {renderCreateOnlyFields()}
      <RepositoryInformationForm
        repository={repo}
        onChange={setRepo}
        disabled={disabled}
        setValid={(contact) => setValid({ ...valid, contact })}
      />
      {submitButton()}
    </form>
  );
};

export default RepositoryForm;
