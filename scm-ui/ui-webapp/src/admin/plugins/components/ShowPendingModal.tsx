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
import { Modal, Notification } from "@scm-manager/ui-components";
import { PendingPlugins } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import PendingPluginsQueue from "./PendingPluginsQueue";
import { Button } from "@scm-manager/ui-core";

type ModalBodyProps = {
  pendingPlugins: PendingPlugins;
};

const ModalBody: FC<ModalBodyProps> = ({ pendingPlugins }) => {
  const [t] = useTranslation("admin");
  return (
    <>
      <div className="media">
        <div className="content">
          <p>{t("plugins.modal.showPending")}</p>
          <PendingPluginsQueue pendingPlugins={pendingPlugins} />
        </div>
      </div>
      <div className="media">
        <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
      </div>
    </>
  );
};

type Props = {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

const ShowPendingModal: FC<Props> = ({ pendingPlugins, onClose }) => {
  const [t] = useTranslation("admin");
  return (
    <Modal
      title={t("plugins.showPending")}
      closeFunction={onClose}
      body={<ModalBody pendingPlugins={pendingPlugins} />}
      footer={<Button onClick={onClose}>{t("plugins.modal.close")}</Button>}
      active={true}
    />
  );
};

export default ShowPendingModal;
