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
import { Repository } from "@scm-manager/ui-types";
import DateFromNow from "../DateFromNow";
import RepositoryAvatar from "./RepositoryAvatar";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import RepositoryFlags from "./RepositoryFlags";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";
import { Card } from "@scm-manager/ui-layout";
import { Link } from "react-router-dom";
import { Menu } from "@scm-manager/ui-overlays";
import { Icon } from "@scm-manager/ui-buttons";

type DateProp = Date | string;

type Props = {
  repository: Repository;
  // @VisibleForTesting
  // the baseDate is only to avoid failing snapshot tests
  baseDate?: DateProp;
};

const Avatar = styled.div`
  .predefined-avatar {
    height: 48px;
    width: 48px;
    font-size: 1.75rem;
  }
`;

const StyledLink = styled(Link)`
  overflow-wrap: anywhere;
`;

const DescriptionRow = styled(Card.Row)`
  text-wrap: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
`;

const DetailsRow = styled(Card.Row)`
  gap: 0.5rem;
`;

const RepositoryEntry: FC<Props> = ({ repository, baseDate }) => {
  const [t] = useTranslation("repos");
  const ref = useKeyboardIteratorTarget();

  const actions = () => (
    <Menu>
      <Menu.DialogButton
        title={t("overview.clone")}
        description={
          <ExtensionPoint<extensionPoints.RepositoryDetailsInformation>
            name="repos.repository-details.information"
            renderAll={true}
            props={{
              repository,
            }}
          />
        }
      >
        <Icon>download</Icon>
        {t("overview.clone")}
      </Menu.DialogButton>
      {repository.contact ? (
        <Menu.ExternalLink
          href={`mailto:${repository.contact}`}
          target="_blank"
          rel="noreferrer"
          title={t("overview.contact", { contact: repository.contact })}
        >
          <Icon>envelope</Icon>
          {t("overview.sendMailToContact")}
        </Menu.ExternalLink>
      ) : null}
    </Menu>
  );

  const repositoryLink = `/repo/${repository.namespace}/${repository.name}/`;

  return (
    <Card
      as="li"
      aria-label={t("overview.ariaLabel", { name: repository.name })}
      action={<>{actions()}</>}
      rowGap="0.25rem"
      avatar={
        <Avatar className="is-align-self-flex-start">
          <RepositoryAvatar repository={repository} size={48} />
        </Avatar>
      }
    >
      <Card.Row className="is-flex">
        <ExtensionPoint<extensionPoints.RepositoryCardBeforeTitle>
          name="repository.card.beforeTitle"
          props={{ repository }}
        />
        <Card.Title level={4}>
          <StyledLink to={repositoryLink} ref={ref}>
            {repository.name}{" "}
          </StyledLink>
        </Card.Title>
      </Card.Row>
      <DescriptionRow className="is-size-7">{repository.description}</DescriptionRow>
      <DetailsRow className="is-flex is-align-items-center is-justify-content-space-between is-flex-wrap-wrap">
        <span className="is-size-7 has-text-secondary is-relative">
          {t("overview.lastModified")}{" "}
          <DateFromNow baseDate={baseDate} date={repository.lastModified ?? repository.creationDate} />
        </span>
        <RepositoryFlags repository={repository} />
      </DetailsRow>
    </Card>
  );
};

export default RepositoryEntry;
