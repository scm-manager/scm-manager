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
