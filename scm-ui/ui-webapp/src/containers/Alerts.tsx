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
import { useAlerts } from "@scm-manager/ui-api";
import { DateFromNow } from "@scm-manager/ui-components";
import { Icon } from "@scm-manager/ui-core";
import { Alert } from "@scm-manager/ui-types";
import HeaderDropDown from "../components/HeaderDropDown";

const AlertDropdown = styled.div`
  @media screen and (max-width: 768px) {
    width: 100vw;
  }
`;

const AlertContainer = styled.ul`
  > *:not(:last-child) {
    border-bottom: solid 2px var(--scm-border-color);
  }
  border-left: 3px solid var(--scm-danger-color);
`;

type EntryProps = {
  alert: ComponentAlert;
};

const AlertsEntry: FC<EntryProps> = ({ alert }) => {
  return (
    <li className={classNames("is-danger has-text-secondary-more px-4 py-3")}>
      <a href={alert.link} target="_blank" rel="noreferrer">
        <section>
          <h2 className="has-text-weight-bold">
            {alert.title} ({alert.component} {alert.affectedVersions})
          </h2>
          <p>{alert.description}</p>
          <DateFromNow date={alert.issuedAt} className="is-size-7" />
        </section>
      </a>
    </li>
  );
};

type Props = {
  data: ComponentAlert[];
};

const AlertsList: FC<Props> = ({ data }) => (
  <AlertDropdown className="dropdown-content p-0 is-full-mobile">
    <AlertContainer className="card-table mb-0 is-flex is-flex-direction-column has-text-left">
      {data.map((a, i) => (
        <AlertsEntry key={i} alert={a} />
      ))}
    </AlertContainer>
  </AlertDropdown>
);

const ShieldNotificationIcon: FC = () => {
  const [t] = useTranslation("commons");
  return (
    <Icon className="is-size-4" alt={t("alerts.shieldTitle")}>
      shield-alt
    </Icon>
  );
};

type ComponentAlert = Alert & {
  component: string;
};

const useFlattenedAlerts = () => {
  const { data, error } = useAlerts();

  if (data) {
    const flattenedAlerts: ComponentAlert[] = data.alerts?.map((a) => ({ ...a, component: "core" })) || [];
    data.plugins?.forEach((p) => flattenedAlerts.push(...(p.alerts || []).map((a) => ({ ...a, component: p.name }))));
    flattenedAlerts.sort((a, b) => {
      if (new Date(a.issuedAt) < new Date(b.issuedAt)) {
        return 1;
      }
      return -1;
    });
    return {
      data: flattenedAlerts,
      error,
    };
  }

  return {
    data,
    error,
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
