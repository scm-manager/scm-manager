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
import {
  Button,
  ButtonGroup,
  Checkbox,
  DateFromNow,
  ErrorNotification,
  Icon,
  InputField,
  Level,
  Notification,
  Subtitle
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { ExportInfo, Link, Repository } from "@scm-manager/ui-types";
import { useExportInfo, useExportRepository } from "@scm-manager/ui-api";
import styled from "styled-components";
import classNames from "classnames";

const InfoBox = styled.div`
  white-space: pre-line;
  border-radius: 2px;
  border-left: 0.2rem solid;
  border-color: #33b2e8;
`;

type Props = {
  repository: Repository;
};

const ExportInterruptedNotification = () => {
  const [t] = useTranslation("repos");
  return <Notification type="warning">{t("export.exportInfo.interrupted")}</Notification>;
};

const ExportInfoBox: FC<{ exportInfo: ExportInfo }> = ({ exportInfo }) => {
  const [t] = useTranslation("repos");
  return (
    <InfoBox className={classNames("my-4", "p-4", "has-background-info-25", "repository-export-info-box")}>
      <strong>{t("export.exportInfo.infoBoxTitle")}</strong>
      <p>{t("export.exportInfo.exporter", { username: exportInfo.exporterName })}</p>
      <p>
        {t("export.exportInfo.created")}
        <DateFromNow date={exportInfo.created} />
      </p>
      <br />
      <p>{exportInfo.withMetadata ? t("export.exportInfo.repositoryArchive") : t("export.exportInfo.repository")}</p>
      {exportInfo.encrypted && (
        <>
          <br />
          <p>{t("export.exportInfo.encrypted")}</p>
        </>
      )}
    </InfoBox>
  );
};

const LinkedExport: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("repos");
  const label = (
    <>
      <Icon name="download" color="inherit" />
      {t("export.downloadExportButton")}
    </>
  );

  return (
    <Button link={link} disabled={!link} color="info">
      {label}
    </Button>
  );
};

const ExportRepository: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const [compressed, setCompressed] = useState(true);
  const [fullExport, setFullExport] = useState(false);
  const [encrypt, setEncrypt] = useState(false);
  const [password, setPassword] = useState("");
  const { isLoading: isLoadingInfo, error: errorInfo, data: exportInfo } = useExportInfo(repository);
  const {
    isLoading: isLoadingExport,
    error: errorExport,
    data: exportedInfo,
    exportRepository
  } = useExportRepository();

  useEffect(() => {
    if (exportedInfo && exportedInfo?._links.download) {
      window.location.href = (exportedInfo?._links.download as Link).href;
    }
  }, [exportedInfo]);

  if (!repository._links.export) {
    return null;
  }

  const renderExportInfo = () => {
    if (!exportInfo) {
      return null;
    }

    if (exportInfo.status === "INTERRUPTED") {
      return <ExportInterruptedNotification />;
    } else {
      return <ExportInfoBox exportInfo={exportInfo} />;
    }
  };

  return (
    <>
      <hr />
      <Subtitle subtitle={t("export.subtitle")} />
      <ErrorNotification error={errorInfo} />
      <ErrorNotification error={errorExport} />
      <Notification type="inherit">{t("export.notification")}</Notification>
      <Checkbox
        checked={fullExport || compressed}
        label={t("export.compressed.label")}
        onChange={setCompressed}
        helpText={t("export.compressed.helpText")}
        disabled={fullExport}
      />
      {repository?._links?.fullExport && (
        <Checkbox
          checked={fullExport}
          label={t("export.fullExport.label")}
          onChange={setFullExport}
          helpText={t("export.fullExport.helpText")}
        />
      )}
      <Checkbox
        checked={encrypt}
        label={t("export.encrypt.label")}
        onChange={setEncrypt}
        helpText={t("export.encrypt.helpText")}
      />
      {encrypt && (
        <div className={classNames("columns", "column", "is-half")}>
          <InputField
            label={t("export.password.label")}
            helpText={t("export.password.helpText")}
            value={password}
            onChange={setPassword}
            type="password"
          />
        </div>
      )}
      {renderExportInfo()}
      <Level
        right={
          <ButtonGroup>
            <LinkedExport link={(exportInfo?._links.download as Link)?.href} />
            <Button
              color="primary"
              action={() =>
                exportRepository(repository, {
                  compressed,
                  password: encrypt ? password : "",
                  withMetadata: fullExport
                })
              }
              loading={isLoadingInfo || isLoadingExport}
              disabled={isLoadingInfo || isLoadingExport}
              label={t("export.createExportButton")}
              icon="file-export"
            />
          </ButtonGroup>
        }
      />
    </>
  );
};

export default ExportRepository;
