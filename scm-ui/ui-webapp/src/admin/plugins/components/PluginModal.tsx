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

import React, { FC, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Link, Plugin } from "@scm-manager/ui-types";
import { Button, ButtonGroup, Checkbox, ErrorNotification, Modal, Notification } from "@scm-manager/ui-components";
import SuccessNotification from "./SuccessNotification";
import { useInstallPlugin, usePluginCenterAuthInfo, useUninstallPlugin, useUpdatePlugins } from "@scm-manager/ui-api";
import { PluginAction } from "../containers/PluginsOverview";
import CloudoguPlatformTag from "./CloudoguPlatformTag";

type Props = {
  plugin: Plugin;
  pluginAction: PluginAction;
  onClose: () => void;
};

type ParentWithPluginAction = {
  pluginAction?: PluginAction;
};

const ListParent = styled.div.attrs(props => ({
  className: "field-label is-inline-flex mr-0 has-text-left"
}))<ParentWithPluginAction>`
  min-width: ${props => (props.pluginAction === PluginAction.INSTALL ? "5.5em" : "10em")};
`;

const ListChild = styled.div`
  flex-grow: 4;
`;

const PluginModal: FC<Props> = ({ onClose, pluginAction, plugin }) => {
  const [t] = useTranslation("admin");
  const [shouldRestart, setShouldRestart] = useState<boolean>(false);
  const {
    data: pluginCenterAuthInfo,
    isLoading: isLoadingPluginCenterAuthInfo,
    error: pluginCenterAuthInfoError
  } = usePluginCenterAuthInfo();
  const { isLoading: isInstalling, error: installError, install, isInstalled } = useInstallPlugin();
  const { isLoading: isUninstalling, error: uninstallError, uninstall, isUninstalled } = useUninstallPlugin();
  const { isLoading: isUpdating, error: updateError, update, isUpdated } = useUpdatePlugins();
  const error = installError || uninstallError || updateError || pluginCenterAuthInfoError;
  const loading = isInstalling || isUninstalling || isUpdating || isLoadingPluginCenterAuthInfo;
  const isDone = isInstalled || isUninstalled || isUpdated;
  const initialFocusRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isDone && !shouldRestart) {
      onClose();
    }
  }, [isDone, onClose, shouldRestart]);

  const handlePluginAction = (e: React.MouseEvent<Element, MouseEvent>) => {
    e.preventDefault();
    switch (pluginAction) {
      case PluginAction.CLOUDOGU:
        window.open((pluginCenterAuthInfo?._links?.login as Link).href, "_self");
        break;
      case PluginAction.INSTALL:
        install(plugin, { restart: shouldRestart });
        break;
      case PluginAction.UNINSTALL:
        uninstall(plugin, { restart: shouldRestart });
        break;
      case PluginAction.UPDATE:
        update(plugin, { restart: shouldRestart });
        break;
      default:
        throw new Error(`Unknown plugin action ${pluginAction}`);
    }
  };

  const footer = () => {
    let color = pluginAction === PluginAction.UNINSTALL ? "warning" : "primary";
    let label = `plugins.modal.${pluginAction}`;
    if (shouldRestart) {
      color = "warning";
      label = `plugins.modal.${pluginAction}AndRestart`;
    }
    return (
      <ButtonGroup>
        {isDone ? (
          <Button
            label={t("plugins.modal.reload")}
            action={() => window.location.reload()}
            color="success"
            icon="sync-alt"
          />
        ) : (
          <>
            <Button
              label={t(label)}
              color={color}
              action={handlePluginAction}
              loading={loading}
              disabled={!!error || isDone}
              ref={initialFocusRef}
            />
            <Button label={t("plugins.modal.abort")} action={onClose} />
          </>
        )}
      </ButtonGroup>
    );
  };

  const renderDependencies = () => {
    let dependencies = null;
    if (pluginAction !== PluginAction.UNINSTALL && plugin.dependencies && plugin.dependencies.length > 0) {
      dependencies = (
        <div className="media">
          <Notification type="warning">
            <strong>{t("plugins.modal.dependencyNotification")}</strong>
            <ul>
              {plugin.dependencies.map((dependency, index) => {
                return <li key={index}>{dependency}</li>;
              })}
            </ul>
          </Notification>
        </div>
      );
    }
    return dependencies;
  };

  const renderOptionalDependencies = () => {
    let optionalDependencies = null;
    if (
      pluginAction !== PluginAction.UNINSTALL &&
      plugin.optionalDependencies &&
      plugin.optionalDependencies.length > 0
    ) {
      optionalDependencies = (
        <div className="media">
          <Notification type="warning">
            <strong>{t("plugins.modal.optionalDependencyNotification")}</strong>
            <ul>
              {plugin.optionalDependencies.map((optionalDependency, index) => {
                return <li key={index}>{optionalDependency}</li>;
              })}
            </ul>
          </Notification>
        </div>
      );
    }
    return optionalDependencies;
  };

  const renderNotifications = () => {
    if (error) {
      return (
        <div className="media">
          <ErrorNotification error={error} />
        </div>
      );
    } else if (isDone && shouldRestart) {
      return (
        <div className="media">
          <SuccessNotification pluginAction={pluginAction} />
        </div>
      );
    } else if (shouldRestart) {
      return (
        <div className="media">
          <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
        </div>
      );
    }
    return null;
  };

  const createRestartSectionContent = () => {
    if (plugin._links[pluginAction + "WithRestart"]) {
      return (
        <Checkbox
          checked={shouldRestart}
          label={t("plugins.modal.restart")}
          onChange={setShouldRestart}
          disabled={false}
        />
      );
    } else if (pluginAction !== PluginAction.CLOUDOGU) {
      return <Notification type="warning">{t("plugins.modal.manualRestartRequired")}</Notification>;
    }
  };

  const body = (
    <>
      <div className="media">
        <div className="media-content">
          <p>{plugin.description}</p>
        </div>
      </div>
      <div className="media">
        <div className="media-content">
          <div className="field is-horizontal">
            <ListParent pluginAction={pluginAction}>{t("plugins.modal.author")}:</ListParent>
            <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.author}</ListChild>
          </div>
          {pluginAction === PluginAction.CLOUDOGU && (
            <>
              <div className="field is-horizontal">
                <CloudoguPlatformTag />
              </div>
              <div className="field is-horizontal">
                <Notification type="info" className="is-full-width">
                  {t("plugins.modal.cloudoguInstallInfo")}
                </Notification>
              </div>
            </>
          )}
          {pluginAction === PluginAction.INSTALL && (
            <div className="field is-horizontal">
              <ListParent pluginAction={pluginAction}>{t("plugins.modal.version")}:</ListParent>
              <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.version}</ListChild>
            </div>
          )}
          {(pluginAction === PluginAction.UPDATE || pluginAction === PluginAction.UNINSTALL) && (
            <div className="field is-horizontal">
              <ListParent>{t("plugins.modal.currentVersion")}:</ListParent>
              <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.version}</ListChild>
            </div>
          )}
          {pluginAction === PluginAction.UPDATE && (
            <div className="field is-horizontal">
              <ListParent>{t("plugins.modal.newVersion")}:</ListParent>
              <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.newVersion}</ListChild>
            </div>
          )}
          {renderDependencies()}
          {renderOptionalDependencies()}
        </div>
      </div>
      <div className="media">
        <div className="media-content">{createRestartSectionContent()}</div>
      </div>
      {renderNotifications()}
    </>
  );

  return (
    <Modal
      title={t(`plugins.modal.title.${pluginAction}`, {
        name: plugin.displayName ? plugin.displayName : plugin.name
      })}
      closeFunction={onClose}
      body={body}
      footer={footer()}
      active={true}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default PluginModal;
