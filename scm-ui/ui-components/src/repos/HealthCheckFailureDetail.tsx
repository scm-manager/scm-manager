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

import React, { FC } from "react";
import { Modal } from "../modals";
import { HealthCheckFailure } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { Button } from "../buttons";
import HealthCheckFailureList from "./HealthCheckFailureList";

type Props = {
  active: boolean;
  closeFunction: () => void;
  failures?: HealthCheckFailure[];
};

const HealthCheckFailureDetail: FC<Props> = ({ active, closeFunction, failures }) => {
  const [t] = useTranslation("repos");

  const footer = <Button label={t("healthCheckFailure.close")} action={closeFunction} color="secondary" />;

  return (
    <Modal
      body={
        <div className="content">
          <HealthCheckFailureList failures={failures} />
        </div>
      }
      title={t("healthCheckFailure.title")}
      closeFunction={closeFunction}
      active={active}
      footer={footer}
      headColor={"danger"}
      headTextColor="secondary-least"
    />
  );
};

export default HealthCheckFailureDetail;
