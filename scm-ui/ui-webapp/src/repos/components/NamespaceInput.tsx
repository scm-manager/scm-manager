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

import React, { FC, useState } from "react";
import { CUSTOM_NAMESPACE_STRATEGY } from "@scm-manager/ui-types";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { ComboboxField } from "@scm-manager/ui-forms";
import { useNamespaceOptions } from "@scm-manager/ui-api";

type Props = {
  namespace: string;
  handleNamespaceChange: (namespace: string) => void;
  namespaceStrategy?: string;
  namespaceValidationError?: boolean;
  disabled?: boolean;
};

const NamespaceInput: FC<Props> = ({
  namespace,
  handleNamespaceChange,
  namespaceStrategy,
  namespaceValidationError,
  disabled,
}) => {
  const [t] = useTranslation("repos");
  const [query, setQuery] = useState("");
  const { isLoading, data } = useNamespaceOptions(query);

  let informationMessage = undefined;
  if (namespace?.indexOf(" ") > 0) {
    informationMessage = t("validation.namespaceSpaceWarningText");
  }

  const repositorySelectValue = namespace ? { value: { id: namespace, displayName: "" }, label: namespace } : null;
  const props = {
    label: t("repository.namespace"),
    helpText: t("help.namespaceHelpText"),
    value: namespace,
    onChange: handleNamespaceChange,
    errorMessage: namespaceValidationError ? t("validation.namespace-invalid") : "",
    informationMessage: informationMessage,
    validationError: namespaceValidationError,
    disabled: disabled,
  };

  if (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY) {
    return (
      // @ts-ignore
      <ComboboxField
        aria-label={props.label}
        label={props.label}
        helpText={props.helpText}
        value={repositorySelectValue}
        onChange={(option) => handleNamespaceChange(option?.value.id ?? "")}
        onQueryChange={setQuery}
        options={data}
        isLoading={isLoading}
        required={true}
        aria-required={true}
        nullable
        ref={(el) => el?.focus()}
      />
    );
  }

  return (
    <ExtensionPoint<extensionPoints.ReposCreateNamespace>
      name="repos.create.namespace"
      props={props}
      renderAll={false}
    />
  );
};

export default NamespaceInput;
