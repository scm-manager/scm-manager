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
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Alert } from "@scm-manager/ui-types";
import { DateFromNow, Icon } from "@scm-manager/ui-components";
import { useAlerts } from "@scm-manager/ui-api";
import HeaderDropDown, { Column, OnlyMobileWrappingColumn, Table } from "../components/HeaderDropDown";

const FullHeightTable = styled(Table)`
  height: 100%;
`;

const RightColumn = styled(OnlyMobileWrappingColumn)`
  height: 100%;
`;

type EntryProps = {
  alert: ComponentAlert;
};

const AlertsEntry: FC<EntryProps> = ({ alert }) => {
  const navigateTo = () => {
    if (alert.link) {
      window.open(alert.link)?.focus();
    }
  };

  return (
    <tr onClick={navigateTo} className={classNames("is-danger", { "is-clickable": !!alert.link })}>
      <Column>
        <p className="has-text-weight-bold">{alert.title}</p>
        <p>{alert.description}</p>
      </Column>
      <RightColumn className="has-text-right">
        <div className="is-flex is-flex-direction-column is-justify-content-space-between">
          <p className="has-text-weight-semibold">
            {alert.component} {alert.affectedVersions}
          </p>
          <DateFromNow date={alert.issuedAt} className="is-size-7" />
        </div>
      </RightColumn>
    </tr>
  );
};

type Props = {
  data: ComponentAlert[];
};

const AlertsList: FC<Props> = ({ data }) => (
  <div className="dropdown-content p-0">
    <FullHeightTable className="table card-table mb-0 is-fullheight">
      <tbody>
        {data.map((a, i) => (
          <AlertsEntry key={i} alert={a} />
        ))}
      </tbody>
    </FullHeightTable>
  </div>
);

const ShieldNotificationIcon: FC = () => {
  const [t] = useTranslation("commons");
  return <Icon className="is-size-4" name="shield-alt" color="inherit" alt={t("alerts.shieldTitle")} />;
};

type ComponentAlert = Alert & {
  component: string;
};

const useFlattenedAlerts = () => {
  const { data, error } = useAlerts();

  if (data) {
    const flattenedAlerts: ComponentAlert[] = data.alerts?.map(a => ({ ...a, component: "core" })) || [];
    data.plugins?.forEach(p => flattenedAlerts.push(...(p.alerts || []).map(a => ({ ...a, component: p.name }))));
    flattenedAlerts.sort((a, b) => {
      if (new Date(a.issuedAt) < new Date(b.issuedAt)) {
        return 1;
      }
      return -1;
    });
    return {
      data: flattenedAlerts,
      error
    };
  }

  return {
    data,
    error
  };
};

type AlertsProps = {
  className?: string;
};

const Alerts: FC<AlertsProps> = ({ className }) => {
  const { data, error } = useFlattenedAlerts();
  if ((!data || data.length === 0) && !error) {
    return null;
  }
  return (
    <HeaderDropDown
      icon={<ShieldNotificationIcon />}
      count={data ? data.length.toString() : "?"}
      error={error}
      className={className}
      mobilePosition="right"
    >
      {data ? <AlertsList data={data} /> : null}
    </HeaderDropDown>
  );
};

export default Alerts;
