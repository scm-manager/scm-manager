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
import * as React from "react";
import { FC, useRef } from "react";
import { useTranslation } from "react-i18next";
import { PendingPlugins, PluginCollection } from "@scm-manager/ui-types";
import { Button, ButtonGroup, ErrorNotification, Modal } from "@scm-manager/ui-components";
import SuccessNotification from "./SuccessNotification";

type Props = {
  onClose: () => void;
  pendingPlugins?: PendingPlugins;
  installedPlugins?: PluginCollection;
  execute: () => void;
  description: string;
  label: string;
  loading: boolean;
  error?: Error | null;
  success: boolean;
};

const PluginActionModal: FC<Props> = ({
  error,
  success,
  children,
  installedPlugins,
  pendingPlugins,
  description,
  label,
  loading,
  onClose,
  execute,
}) => {
  const [t] = useTranslation("admin");
  const initialFocusRef = useRef<HTMLButtonElement>(null);

  let notifications;
  if (error) {
    notifications = <ErrorNotification error={error} />;
  } else if (success) {
    notifications = <SuccessNotification />;
  } else {
    notifications = children;
  }

  const updatable = (
    <>
      {installedPlugins && installedPlugins._embedded && installedPlugins._embedded.plugins && (
        <>
          <strong>{t("plugins.modal.updateQueue")}</strong>
          <ul>
            {installedPlugins._embedded.plugins
              .filter((plugin) => plugin._links && plugin._links.update)
              .map((plugin) => (
                <li key={plugin.name}>{plugin.name}</li>
              ))}
          </ul>
        </>
      )}
    </>
  );

  const installQueue = (
    <>
      {pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.new.length > 0 && (
        <>
          <strong>{t("plugins.modal.installQueue")}</strong>
          <ul>
            {pendingPlugins._embedded.new.map((plugin) => (
              <li key={plugin.name}>{plugin.name}</li>
            ))}
          </ul>
        </>
      )}
    </>
  );

  const updateQueue = pendingPlugins && pendingPlugins._embedded && pendingPlugins._embedded.update.length > 0 && (
    <>
      <strong>{t("plugins.modal.updateQueue")}</strong>
      <ul>
        {pendingPlugins._embedded.update.map((plugin) => (
          <li key={plugin.name}>{plugin.name}</li>
        ))}
      </ul>
    </>
  );

  const uninstallQueue = pendingPlugins &&
    pendingPlugins._embedded &&
    pendingPlugins._embedded.uninstall.length > 0 && (
      <>
        <strong>{t("plugins.modal.uninstallQueue")}</strong>
        <ul>
          {pendingPlugins._embedded.uninstall.map((plugin) => (
            <li key={plugin.name}>{plugin.name}</li>
          ))}
        </ul>
      </>
    );

  const content = (
    <>
      {updatable}
      {installQueue}
      {updateQueue}
      {uninstallQueue}
    </>
  );

  const body = (
    <>
      <div className="media">
        <div className="content">
          <p>{description}</p>
          {content}
        </div>
      </div>
      <div className="media">{notifications}</div>
    </>
  );

  const footer = (
    <ButtonGroup>
      {success ? (
        <Button
          label={t("plugins.modal.reload")}
          action={() => window.location.reload()}
          color="success"
          icon="sync-alt"
        />
      ) : (
        <>
          <Button
            color="warning"
            label={label}
            loading={loading}
            action={execute}
            disabled={!!error || success}
            ref={initialFocusRef}
          />
          <Button label={t("plugins.modal.abort")} action={onClose} />
        </>
      )}
    </ButtonGroup>
  );

  return (
    <Modal
      title={label}
      closeFunction={onClose}
      body={body}
      footer={footer}
      active={true}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default PluginActionModal;
