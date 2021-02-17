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
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Plugin } from "@scm-manager/ui-types";
import { Button, ButtonGroup, Checkbox, ErrorNotification, Modal, Notification } from "@scm-manager/ui-components";
import SuccessNotification from "./SuccessNotification";
import { useInstallPlugin, useUninstallPlugin, useUpdatePlugins } from "@scm-manager/ui-api";
import { PluginAction } from "../containers/PluginsOverview";

type Props = {
  plugin: Plugin;
  pluginAction: string;
  onClose: () => void;
};

const ListParent = styled.div`
  margin-right: 0;
  min-width: ${props => (props.pluginAction === PluginAction.INSTALL ? "5.5em" : "10em")};
  text-align: left;
`;

const ListChild = styled.div`
  flex-grow: 4;
`;

const PluginModal: FC<Props> = ({ onClose, pluginAction, plugin }) => {
  const [t] = useTranslation("admin");
  const [shouldRestart, setShouldRestart] = useState<boolean>(false);
  const { isLoading: isInstalling, error: installError, install, isInstalled } = useInstallPlugin();
  const { isLoading: isUninstalling, error: uninstallError, uninstall, isUninstalled } = useUninstallPlugin();
  const { isLoading: isUpdating, error: updateError, update, isUpdated } = useUpdatePlugins();
  const error = installError || uninstallError || updateError;
  const loading = isInstalling || isUninstalling || isUpdating;
  const isDone = isInstalled || isUninstalled || isUpdated;

  useEffect(() => {
    if (isDone && !shouldRestart) {
      onClose();
    }
  }, [isDone]);

  const handlePluginAction = (e: Event) => {
    e.preventDefault();
    switch (pluginAction) {
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
        throw new Error(`Unkown plugin action ${pluginAction}`);
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
        <Button
          label={t(label)}
          color={color}
          action={handlePluginAction}
          loading={loading}
          disabled={!!error || isDone}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  const renderDependencies = () => {
    let dependencies = null;
    if (plugin.dependencies && plugin.dependencies.length > 0) {
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
    if (plugin.optionalDependencies && plugin.optionalDependencies.length > 0) {
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
    } else {
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
            <ListParent className={classNames("field-label", "is-inline-flex")} pluginAction={pluginAction}>
              {t("plugins.modal.author")}:
            </ListParent>
            <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.author}</ListChild>
          </div>
          {pluginAction === PluginAction.INSTALL && (
            <div className="field is-horizontal">
              <ListParent className={classNames("field-label", "is-inline-flex")} pluginAction={pluginAction}>
                {t("plugins.modal.version")}:
              </ListParent>
              <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.version}</ListChild>
            </div>
          )}
          {(pluginAction === PluginAction.UPDATE || pluginAction === PluginAction.UNINSTALL) && (
            <div className="field is-horizontal">
              <ListParent className={classNames("field-label", "is-inline-flex")}>
                {t("plugins.modal.currentVersion")}:
              </ListParent>
              <ListChild className={classNames("field-body", "is-inline-flex")}>{plugin.version}</ListChild>
            </div>
          )}
          {pluginAction === PluginAction.UPDATE && (
            <div className="field is-horizontal">
              <ListParent className={classNames("field-label", "is-inline-flex")}>
                {t("plugins.modal.newVersion")}:
              </ListParent>
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
    />
  );
};

export default PluginModal;
