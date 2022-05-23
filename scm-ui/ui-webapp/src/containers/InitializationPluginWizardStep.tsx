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
import { useTranslation } from "react-i18next";
import { HalRepresentationWithEmbedded, PluginSet } from "@scm-manager/ui-types";
import { apiClient, requiredLink } from "@scm-manager/ui-api";
import { useMutation } from "react-query";

type PluginSetsInstallation = {
  pluginSetIds: string[];
};

const installPluginSets = (link: string) => (data: PluginSetsInstallation) =>
  apiClient.post(link, data, "application/json");

const useInstallPluginSets = (link: string) => {
  const { mutate, isLoading, error, isSuccess } = useMutation<unknown, Error, PluginSetsInstallation>(
    installPluginSets(link)
  );
  return {
    installPluginSets: mutate,
    isLoading,
    error,
    isInstalled: isSuccess,
  };
};

type Props = {
  data: HalRepresentationWithEmbedded<{ pluginSets: PluginSet[] }>;
};

const InitializationPluginWizardStep: FC<Props> = ({ data: initializationContext }) => {
  const {
    installPluginSets,
    isInstalled,
    isLoading: isInstalling,
    error: installationError,
  } = useInstallPluginSets(requiredLink(initializationContext, "installPluginSets"));
  const data = initializationContext._embedded?.pluginSets;
  const [t] = useTranslation("initialization");
  return (
    <>
      <h1>Hello World</h1>
      {data?.map((pluginSet) => (
        <div>{pluginSet.name}</div>
      ))}
    </>
  );
};

export default InitializationPluginWizardStep;
