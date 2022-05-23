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

import React, {FC, useCallback, useEffect, useMemo} from "react";
import { useTranslation } from "react-i18next";
import { HalRepresentationWithEmbedded, PluginSet } from "@scm-manager/ui-types";
import { apiClient, requiredLink, waitForRestartAfter } from "@scm-manager/ui-api";
import { useMutation } from "react-query";
import { useForm } from "react-hook-form";
import { Checkbox, ErrorNotification, SubmitButton } from "@scm-manager/ui-components";
import styled from "styled-components";

const HeroSection = styled.section`
  padding-top: 2em;
`;

type PluginSetsInstallation = {
  pluginSetIds: string[];
};

const installPluginSets = (link: string) => (data: PluginSetsInstallation) =>
  waitForRestartAfter(apiClient.post(link, data, "application/json"));

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

type FormValue = { [id: string]: boolean };

const InitializationPluginWizardStep: FC<Props> = ({ data: initializationContext }) => {
  const {
    installPluginSets,
    isLoading: isInstalling,
    isInstalled,
    error: installationError,
  } = useInstallPluginSets(requiredLink(initializationContext, "installPluginSets"));

  const { register, handleSubmit, watch } = useForm<FormValue>();
  const data = initializationContext._embedded?.pluginSets;
  const [t] = useTranslation("initialization");
  const values = watch();
  const pluginSetIds = useMemo(
    () =>
      Object.entries(values).reduce<string[]>((p, [id, flag]) => {
        if (flag) {
          p.push(id);
        }
        return p;
      }, []),
    [values]
  );
  const submit = useCallback(
    () =>
      installPluginSets({
        pluginSetIds,
      }),
    [installPluginSets, pluginSetIds]
  );

  useEffect(() => {
    if (isInstalled) {
      window.location.reload();
    }
  }, [isInstalled]);

  let content;

  if (installationError) {
    content = <ErrorNotification error={installationError} />;
  } else {
    content = (
      <form onSubmit={handleSubmit(submit)}>
        {data?.map((pluginSet) => (
          <Checkbox {...register(pluginSet.id)} label={pluginSet.name} />
        ))}
        <SubmitButton disabled={isInstalling} loading={isInstalling}>
          {pluginSetIds.length ? "Submit" : "Skip"}
        </SubmitButton>
      </form>
    );
  }

  return (
    <HeroSection className="hero">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-centered">
            <div className="column is-8 box  has-background-secondary-less">
              <h3 className="title">{t("title")}</h3>
              <h4 className="subtitle">{t("pluginWizardStep.title")}</h4>
              {content}
            </div>
          </div>
        </div>
      </div>
    </HeroSection>
  );
};

export default InitializationPluginWizardStep;
