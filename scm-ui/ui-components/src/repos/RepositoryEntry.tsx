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
import { Repository } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
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
