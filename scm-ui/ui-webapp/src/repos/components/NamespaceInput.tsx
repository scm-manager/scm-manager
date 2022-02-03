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

import React, { FC } from "react";
import { CUSTOM_NAMESPACE_STRATEGY } from "@scm-manager/ui-types";
import { Autocomplete } from "@scm-manager/ui-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { useNamespaceSuggestions } from "@scm-manager/ui-api";

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
  disabled
}) => {
  const [t] = useTranslation("repos");
  const loadNamespaceSuggestions = useNamespaceSuggestions();

  let informationMessage = undefined;
  if (namespace?.indexOf(" ") > 0) {
    informationMessage = t("validation.namespaceSpaceWarningText");
  }

  const repositorySelectValue = namespace ? { value: { id: namespace, displayName: "" }, label: namespace } : undefined;
  const props = {
    label: t("repository.namespace"),
    helpText: t("help.namespaceHelpText"),
    value: namespace,
    onChange: handleNamespaceChange,
    errorMessage: namespaceValidationError ? t("validation.namespace-invalid") : "",
    informationMessage: informationMessage,
    validationError: namespaceValidationError,
    disabled: disabled
  };

  if (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY) {
    return (
      <Autocomplete
        {...props}
        loadSuggestions={loadNamespaceSuggestions}
        value={repositorySelectValue}
        valueSelected={namespaceValue => handleNamespaceChange(namespaceValue.value.id)}
        placeholder={""}
        creatable={true}
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
