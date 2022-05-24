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

import React, { FC, useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { HalRepresentationWithEmbedded, PluginSet } from "@scm-manager/ui-types";
import { apiClient, requiredLink, waitForRestartAfter } from "@scm-manager/ui-api";
import { useMutation } from "react-query";
import { useForm } from "react-hook-form";
import { Checkbox, ErrorNotification, Icon, SubmitButton } from "@scm-manager/ui-components";
import styled from "styled-components";

const HighlightedImg = styled.img`
  &:hover {
    filter: drop-shadow(0px 2px 2px #00c79b);
  }
`;

const HiddenInput = styled.input`
  display: none;
`;

const HeroSection = styled.section`
  padding-top: 2em;
`;

const BorderedDiv = styled.div`
  border-radius: 4px;
  border-width: 1px;
  border-style: solid;
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

type PluginSetCardProps = {
  pluginSet: PluginSet;
};
const PluginSetCard: FC<PluginSetCardProps> = ({ pluginSet, children }) => {
  const [t] = useTranslation("initialization");
  const [collapsed, setCollapsed] = useState(true);

  return (
    <div className="card block">
      <div className="card-image pt-5">{children}</div>
      <div className="card-content">
        <h5 className="subtitle is-5">{pluginSet.name}</h5>
        <div className="content">
          <ul>
            {pluginSet.features.map((feature) => (
              <li>{feature}</li>
            ))}
          </ul>
        </div>
        <div className="block pl-4">
          <div
            className="is-flex is-justify-content-space-between has-cursor-pointer"
            onClick={() => setCollapsed((value) => !value)}
          >
            <span>{t(`pluginWizardStep.pluginSet.${collapsed ? "expand" : "collapse"}`)}</span>
            <Icon color="inherit" name={collapsed ? "angle-down" : "angle-up"} />
          </div>
          {!collapsed ? (
            <ul className="pt-2">
              {pluginSet.plugins.map((plugin, idx) => (
                <li key={idx} className="py-2">
                  <div className="is-size-6 has-text-weight-semibold">{plugin.displayName}</div>
                  <div className="is-size-6"><a href={`https://scm-manager.org/plugins/${plugin.name}`} target="_blank">{plugin.description}</a></div>
                </li>
              ))}
            </ul>
          ) : null}
        </div>
      </div>
    </div>
  );
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
  const [skipInstallation, setSkipInstallation] = useState(false);

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
  const isSelected = useCallback((pluginSetId: string) => pluginSetIds.includes(pluginSetId), [pluginSetIds]);
  const hasPluginSets = useMemo(() => pluginSetIds.length > 0, [pluginSetIds]);

  useEffect(() => {
    if (hasPluginSets) {
      setSkipInstallation(false);
    }
  }, [hasPluginSets]);

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
      <form onSubmit={handleSubmit(submit)} className="is-flex is-flex-direction-column">
        <div className="block">
          {data?.map((pluginSet, idx) => (
            <PluginSetCard pluginSet={pluginSet} key={idx}>
              <label htmlFor={`plugin-set-${idx}`} className="has-cursor-pointer">
                <HighlightedImg
                  alt={pluginSet.name}
                  src={`data:image/svg+xml;base64,${pluginSet.images[isSelected(pluginSet.id) ? "check" : "standard"]}`}
                />
              </label>
              <HiddenInput type="checkbox" id={`plugin-set-${idx}`} {...register(pluginSet.id)} />
            </PluginSetCard>
          ))}
          <BorderedDiv className="card-block has-border-danger px-4 pt-3">
            <Checkbox
              disabled={hasPluginSets}
              checked={skipInstallation}
              onChange={setSkipInstallation}
              title={t("pluginWizardStep.skip.title")}
              label={t("pluginWizardStep.skip.subtitle")}
            ></Checkbox>
          </BorderedDiv>
        </div>
        <SubmitButton
          disabled={isInstalling || !(hasPluginSets || skipInstallation)}
          loading={isInstalling}
          className="is-align-self-flex-end"
        >
          {t("pluginWizardStep.submit")}
        </SubmitButton>
      </form>
    );
  }

  return (
    <HeroSection className="hero">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-centered">
            <div className="column is-8 box has-background-secondary-less">
              <h3 className="title">{t("title")}</h3>
              <h4 className="subtitle">{t("pluginWizardStep.title")}</h4>
              <p className="is-size-6 block">{t("pluginWizardStep.description")}</p>
              {content}
            </div>
          </div>
        </div>
      </div>
    </HeroSection>
  );
};

export default InitializationPluginWizardStep;
