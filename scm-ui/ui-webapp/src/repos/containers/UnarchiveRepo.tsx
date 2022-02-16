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
import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Button, ConfirmAlert, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useUnarchiveRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  confirmDialog?: boolean;
};

const UnarchiveRepo: FC<Props> = ({ repository, confirmDialog = true }) => {
  const { isLoading, error, unarchive } = useUnarchiveRepository();
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");

  const unarchiveRepoCallback = () => {
    unarchive(repository);
  };

  const confirmUnarchive = () => {
    setShowConfirmAlert(true);
  };

  const action = confirmDialog ? confirmUnarchive : unarchiveRepoCallback;

  const confirmAlert = (
    <ConfirmAlert
      title={t("unarchiveRepo.confirmAlert.title")}
      message={t("unarchiveRepo.confirmAlert.message")}
      buttons={[
        {
          className: "is-outlined",
          label: t("unarchiveRepo.confirmAlert.submit"),
          isLoading,
          onClick: () => unarchiveRepoCallback(),
          autofocus: true
        },
        {
          label: t("unarchiveRepo.confirmAlert.cancel"),
          onClick: () => null
        }
      ]}
      close={() => setShowConfirmAlert(false)}
    />
  );

  return (
    <>
      <ErrorNotification error={error} />
      {showConfirmAlert && confirmAlert}
      <Level
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("unarchiveRepo.subtitle")}</h4>
            <p>{t("unarchiveRepo.description")}</p>
          </div>
        }
        right={
          <Button
            color="warning"
            icon="box-open"
            label={t("unarchiveRepo.button")}
            action={action}
            loading={isLoading}
          />
        }
      />
    </>
  );
};

export default UnarchiveRepo;
