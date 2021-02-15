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
import { Button, ButtonGroup, ErrorNotification, Modal, Notification } from "@scm-manager/ui-components";
import { PendingPlugins } from "@scm-manager/ui-types";
import { useTranslation, WithTranslation } from "react-i18next";
import SuccessNotification from "./SuccessNotification";
import PendingPluginsQueue from "./PendingPluginsQueue";
import { useExecutePendingPlugins } from "@scm-manager/ui-api";
import waitForRestart from "./waitForRestart";

type Props = WithTranslation & {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

const ExecutePendingModal: FC<Props> = ({ pendingPlugins, onClose }) => {
  const [t] = useTranslation("admin");
  const { isExecuted, error, isLoading, update } = useExecutePendingPlugins();
  const [restarted, setRestarted] = useState<boolean>(false);
  const [restarting, setRestarting] = useState<boolean>(false);
  const loading = isLoading || restarting;

  useEffect(() => {
    if (isExecuted) {
      setRestarting(true);
      waitForRestart().then(() => {
        setRestarting(false);
        setRestarted(true);
      });
    }
  }, [isExecuted]);

  const renderNotifications = () => {
    if (error) {
      return <ErrorNotification error={error} />;
    } else if (restarted) {
      return <SuccessNotification />;
    } else {
      return <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>;
    }
  };

  const renderBody = () => {
    return (
      <>
        <div className="media">
          <div className="content">
            <p>{t("plugins.modal.executePending")}</p>
            <PendingPluginsQueue pendingPlugins={pendingPlugins} />
          </div>
        </div>
        <div className="media">{renderNotifications()}</div>
      </>
    );
  };

  const renderFooter = () => {
    return (
      <ButtonGroup>
        <Button
          color="warning"
          label={t("plugins.modal.executeAndRestart")}
          loading={loading}
          action={() => update(pendingPlugins)}
          disabled={!!error}
        />
        <Button label={t("plugins.modal.abort")} action={onClose} />
      </ButtonGroup>
    );
  };

  return (
    <Modal
      title={t("plugins.modal.executeAndRestart")}
      closeFunction={onClose}
      body={renderBody()}
      footer={renderFooter()}
      active={true}
    />
  );
};

export default ExecutePendingModal;
