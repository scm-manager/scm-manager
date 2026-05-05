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

import React, { FC, PropsWithChildren, useState } from "react";
import { HalRepresentation, HalRepresentationWithEmbedded, Link } from "@scm-manager/ui-types";
import {
  Button,
  Title,
  Level,
  ErrorNotification,
  StatusIcon,
  StatusVariants,
  StatusVariant,
  InputField,
  Subtitle,
} from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { UseMutateAsyncFunction, useMutation } from "@tanstack/react-query";
import { apiClient, waitForRestartAfter } from "@scm-manager/ui-api";
import { validation } from "@scm-manager/ui-components";
import styled from "styled-components";

const StyledList = styled.ul`
  list-style: disc;
`;

type ConflictingPlugin = {
  name: string;
  version: string;
};

type CompatibilityResult = HalRepresentation & {
  status: "NO_ACTION_NEEDED" | "UPDATED" | "UPDATE_NOT_POSSIBLE" | "PLUGIN_CENTER_DEACTIVATED";
  conflictingPlugins: ConflictingPlugin[];
  currentPluginCenterUrl: string | null | undefined;
  defaultPluginCenterUrl: string;
};

type TokenDto = {
  token: string;
};

type PluginCenterUrlDto = TokenDto & {
  pluginCenterUrl: string;
};

type Props = {
  data: HalRepresentationWithEmbedded<{ scm4xCompatibility: CompatibilityResult }>;
};

type CompatibilityProps = {
  scm4xCompatibility: CompatibilityResult;
};

type TokenProps = {
  token: string;
  setToken: (value: string) => void;
  tokenError: string;
  setTokenError: (value: string) => void;
};

const UpdatePluginCenterFormIds = {
  FORM_ID: "set-plugin-center-form",
  PLUGIN_CENTER_INPUT_ID: "set-plugin-center-input",
};

type UpdatePluginCenterUrlFormProps = {
  scm4xCompatibility: CompatibilityResult;
  setPluginCenter: UseMutateAsyncFunction<unknown, Error, PluginCenterUrlDto>;
  isSettingUrl: boolean;
  isDeletingPlugins: boolean;
  token: string;
  tokenError: string;
};

const useDeleteConflictingPlugins = (link: string) => {
  const { mutate, isPending, error } = useMutation<unknown, Error, TokenDto>({
    mutationFn: (token: TokenDto) => waitForRestartAfter(apiClient.post(link, token, "application/json")),
  });

  return {
    deleteConflictingPlugins: mutate,
    isDeleting: isPending,
    deleteConflictingPluginsError: error,
  };
};

const useSetPluginCenter = (link: string) => {
  const { mutateAsync, isPending, error } = useMutation<unknown, Error, PluginCenterUrlDto>({
    mutationFn: (data: PluginCenterUrlDto) => waitForRestartAfter(apiClient.post(link, data, "application/json")),
  });

  return {
    setPluginCenter: mutateAsync,
    isSetting: isPending,
    setPluginCenterError: error,
  };
};

const CompatibilityTitle: FC<PropsWithChildren<{ statusVariant: StatusVariant }>> = ({ children, statusVariant }) => {
  return (
    <Subtitle className="is-flex">
      <StatusIcon className="mr-4" variant={statusVariant} />
      {children}
    </Subtitle>
  );
};

const ConflictingPluginsList: FC<{ conflictingPlugins: ConflictingPlugin[] }> = ({ conflictingPlugins }) => {
  const [t] = useTranslation("initialization");
  return (
    <>
      <p className="mb-2 has-text-weight-bold">
        {t("scm4CompatibilityStep.conflictingPlugins.list", { count: conflictingPlugins.length })}
      </p>
      <StyledList className="mb-4 pl-4">
        {conflictingPlugins.map((plugin) => (
          <li key={plugin.name}>
            {plugin.name} ({plugin.version})
          </li>
        ))}
      </StyledList>
    </>
  );
};

const DefaultPluginCenterUrl: FC<CompatibilityProps> = ({ scm4xCompatibility }) => {
  const [t] = useTranslation("initialization");

  return (
    <>
      <p className="has-text-weight-bold mb-2">{t("scm4CompatibilityStep.defaultPluginCenterUrl")}</p>
      <p className="mb-4">{scm4xCompatibility.defaultPluginCenterUrl}</p>
    </>
  );
};

const ErrorHandler: FC<{ error: Error | null }> = ({ error }) => {
  const [t] = useTranslation("initialization");

  if (!error) {
    return null;
  }

  let errorToRender: Error;
  if (error.message === "Forbidden") {
    errorToRender = new Error(t("scm4CompatibilityStep.token.wrongToken"));
  } else if (error.message === "Restart is not supported") {
    errorToRender = new Error(t("scm4CompatibilityStep.restartNotSupported"));
  } else {
    errorToRender = error;
  }

  return <ErrorNotification error={errorToRender} />;
};

const TokenInput: FC<TokenProps> = ({ token, setToken, tokenError, setTokenError }) => {
  const [t] = useTranslation("initialization");

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setToken(e.target.value);

    if (e.target.value.length === 0) {
      setTokenError(t("scm4CompatibilityStep.token.isEmptyError"));
    } else {
      setTokenError("");
    }
  };

  return (
    <>
      <p className="mb-2">{t("scm4CompatibilityStep.token.description")}</p>
      <div className="mb-2">
        <InputField label={t("scm4CompatibilityStep.token.label")} type="password" value={token} onChange={onChange} />
        {tokenError ? <p className="has-text-danger">{tokenError}</p> : null}
      </div>
    </>
  );
};

const UpdatePluginCenterUrlForm: FC<UpdatePluginCenterUrlFormProps> = ({
  scm4xCompatibility,
  setPluginCenter,
  isSettingUrl,
  isDeletingPlugins,
  token,
  tokenError,
}) => {
  const [t] = useTranslation("initialization");
  const [pluginCenterUrl, setPluginCenterUrl] = useState(scm4xCompatibility.currentPluginCenterUrl ?? "");
  const [pluginCenterUrlError, setPluginCenterUrlError] = useState("");

  const isDirty = pluginCenterUrl !== scm4xCompatibility.currentPluginCenterUrl && token !== "";
  const isValid = pluginCenterUrlError === "" && tokenError === "";

  const validateUrl = (url: string) => {
    if (!validation.isUrlValid(url)) {
      return t("scm4CompatibilityStep.pluginCenterDeactivated.pluginCenterUrl.invalidUrl");
    }
  };

  const onPluginCenterUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPluginCenterUrl(e.target.value);
    const validation = validateUrl(e.target.value);
    setPluginCenterUrlError(validation ? validation : "");
  };

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      await setPluginCenter(
        { pluginCenterUrl, token },
        {
          onSuccess: () => window.location.reload(),
        },
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  return (
    <form onSubmit={onSubmit} id={UpdatePluginCenterFormIds.FORM_ID} noValidate>
      <DefaultPluginCenterUrl scm4xCompatibility={scm4xCompatibility} />
      <div className="mb-2">
        <InputField
          label={t("scm4CompatibilityStep.pluginCenterDeactivated.pluginCenterUrl.label")}
          value={pluginCenterUrl}
          onChange={onPluginCenterUrlChange}
        />
        {pluginCenterUrlError ? <p className="has-text-danger">{pluginCenterUrlError}</p> : null}
      </div>
      <Level
        right={
          <Button
            type="submit"
            variant="primary"
            form={UpdatePluginCenterFormIds.FORM_ID}
            isLoading={isSettingUrl}
            disabled={isSettingUrl || isDeletingPlugins || !isDirty || !isValid}
          >
            {t("scm4CompatibilityStep.pluginCenterDeactivated.pluginCenterUrl.submit")}
          </Button>
        }
      />
    </form>
  );
};

const Header: FC = () => {
  const [t] = useTranslation("initialization");

  return (
    <>
      <Title>{t("scm4CompatibilityStep.header.title")}</Title>
      <p className="mb-4">{t("scm4CompatibilityStep.header.description")}</p>
      <hr />
    </>
  );
};

const UpdatedPlugins: FC = () => {
  const [t] = useTranslation("initialization");

  return (
    <>
      <Header />
      <CompatibilityTitle statusVariant={StatusVariants.SUCCESS}>
        {t("scm4CompatibilityStep.updated.title")}
      </CompatibilityTitle>
      <p className="mb-4">{t("scm4CompatibilityStep.updated.description")}</p>
    </>
  );
};

const DeleteConflictingPlugins: FC<CompatibilityProps> = ({ scm4xCompatibility }) => {
  const [t] = useTranslation("initialization");
  const { deleteConflictingPlugins, isDeleting, deleteConflictingPluginsError } = useDeleteConflictingPlugins(
    (scm4xCompatibility._links["uninstall"] as Link).href,
  );
  const { setPluginCenter, isSetting, setPluginCenterError } = useSetPluginCenter(
    (scm4xCompatibility._links["update-plugin-center"] as Link).href,
  );
  const [token, setToken] = useState("");
  const [tokenError, setTokenError] = useState("");

  const onSubmitPluginDeletion = () => {
    deleteConflictingPlugins({ token }, { onSuccess: () => window.location.reload() });
  };

  return (
    <>
      <ErrorHandler error={setPluginCenterError} />
      <ErrorHandler error={deleteConflictingPluginsError} />
      <Header />
      <CompatibilityTitle statusVariant={StatusVariants.DANGER}>
        {t("scm4CompatibilityStep.conflictingPlugins.title")}
      </CompatibilityTitle>
      <p className="mb-4">
        {t("scm4CompatibilityStep.conflictingPlugins.description", {
          count: scm4xCompatibility.conflictingPlugins.length,
        })}
      </p>
      <ConflictingPluginsList conflictingPlugins={scm4xCompatibility.conflictingPlugins} />
      <TokenInput token={token} setToken={setToken} tokenError={tokenError} setTokenError={setTokenError} />
      <Level
        right={
          <Button
            variant="primary"
            isLoading={isDeleting}
            onClick={onSubmitPluginDeletion}
            disabled={isDeleting || isSetting || token === "" || tokenError !== ""}
            className="mt-2"
          >
            {t("scm4CompatibilityStep.conflictingPlugins.submit", {
              count: scm4xCompatibility.conflictingPlugins.length,
            })}
          </Button>
        }
      />
      {scm4xCompatibility.currentPluginCenterUrl !== scm4xCompatibility.defaultPluginCenterUrl ? (
        <p className="mb-4 is-flex">
          <StatusIcon className="mr-2" variant={StatusVariants.WARNING} />
          {t("scm4CompatibilityStep.conflictingPlugins.nonDefaultPluginCenterUrl")}
        </p>
      ) : null}
      <UpdatePluginCenterUrlForm
        scm4xCompatibility={scm4xCompatibility}
        setPluginCenter={setPluginCenter}
        isSettingUrl={isSetting}
        isDeletingPlugins={isDeleting}
        token={token}
        tokenError={tokenError}
      />
    </>
  );
};

const PluginCenterDeactivated: FC<CompatibilityProps> = ({ scm4xCompatibility }) => {
  const [t] = useTranslation("initialization");
  const { setPluginCenter, isSetting, setPluginCenterError } = useSetPluginCenter(
    (scm4xCompatibility._links["update-plugin-center"] as Link).href,
  );

  const [token, setToken] = useState("");
  const [tokenError, setTokenError] = useState("");

  return (
    <>
      <ErrorHandler error={setPluginCenterError} />
      <Header />
      <CompatibilityTitle statusVariant={StatusVariants.WARNING}>
        {t("scm4CompatibilityStep.pluginCenterDeactivated.title")}
      </CompatibilityTitle>
      <p className="mb-4">
        {t("scm4CompatibilityStep.pluginCenterDeactivated.description", {
          count: scm4xCompatibility.conflictingPlugins.length,
        })}
      </p>
      <ConflictingPluginsList conflictingPlugins={scm4xCompatibility.conflictingPlugins} />
      <TokenInput token={token} setToken={setToken} tokenError={tokenError} setTokenError={setTokenError} />
      <UpdatePluginCenterUrlForm
        scm4xCompatibility={scm4xCompatibility}
        setPluginCenter={setPluginCenter}
        isSettingUrl={isSetting}
        isDeletingPlugins={false}
        token={token}
        tokenError={tokenError}
      />
    </>
  );
};

const InitializationScm4xCompatibilityStep: FC<Props> = ({ data: initContext }) => {
  if (!initContext._embedded) {
    return null;
  }

  return (
    <section className="section">
      <div className="container">
        {initContext._embedded.scm4xCompatibility.status === "UPDATED" ? <UpdatedPlugins /> : null}
        {initContext._embedded.scm4xCompatibility.status === "UPDATE_NOT_POSSIBLE" ? (
          <DeleteConflictingPlugins scm4xCompatibility={initContext._embedded.scm4xCompatibility} />
        ) : null}
        {initContext._embedded.scm4xCompatibility.status === "PLUGIN_CENTER_DEACTIVATED" ? (
          <PluginCenterDeactivated scm4xCompatibility={initContext._embedded.scm4xCompatibility} />
        ) : null}
      </div>
    </section>
  );
};

export default InitializationScm4xCompatibilityStep;
