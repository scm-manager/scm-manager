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
import { Repository } from "@scm-manager/ui-types";
import { DateFromNow, Modal } from "@scm-manager/ui-components";
import RepositoryAvatar from "./RepositoryAvatar";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import GroupEntry from "../layout/GroupEntry";
import RepositoryFlags from "./RepositoryFlags";
import styled from "styled-components";
import Icon from "../Icon";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { EXTENSION_POINT } from "../avatar/Avatar";

type DateProp = Date | string;

type Props = {
  repository: Repository;
  // @VisibleForTesting
  // the baseDate is only to avoid failing snapshot tests
  baseDate?: DateProp;
};

const ContentRightContainer = styled.div`
  height: calc(80px - 1.5rem);
`;

const QuickAction = styled(Icon)`
  margin-top: 0.2rem;
`;

const ContactAvatar = styled.img`
  max-width: 20px;
`;

const ContactActionWrapper = styled.a`
  height: 20px;
  width: 20px;
  padding-right: 2rem;
`;

const Name = styled.strong`
  text-overflow: ellipsis;
  overflow-x: hidden;
  overflow-y: visible;
  white-space: nowrap;
`;

const RepositoryEntry: FC<Props> = ({ repository, baseDate }) => {
  const [t] = useTranslation("repos");
  const [openCloneModal, setOpenCloneModal] = useState(false);

  const avatarFactory = binder.getExtension(EXTENSION_POINT);

  const renderContactIcon = () => {
    if (avatarFactory) {
      return (
        <ContactAvatar
          className="has-rounded-border"
          src={avatarFactory({ mail: repository.contact })}
          alt={repository.contact}
        />
      );
    }
    return <QuickAction className={classNames("is-clickable", "has-hover-visible")} name="envelope" color="info" />;
  };

  const createContentRight = () => (
    <ContentRightContainer
      className={classNames(
        "is-flex",
        "is-flex-direction-column",
        "is-justify-content-space-between",
        "is-relative",
        "mr-4"
      )}
    >
      {openCloneModal && (
        <Modal
          size="L"
          active={openCloneModal}
          title={t("overview.clone")}
          body={
            <ExtensionPoint<extensionPoints.RepositoryDetailsInformation>
              name="repos.repository-details.information"
              renderAll={true}
              props={{
                repository,
              }}
            />
          }
          closeFunction={() => setOpenCloneModal(false)}
        />
      )}
      <span className={classNames("is-flex", "is-justify-content-flex-end", "is-align-items-center")}>
        {repository.contact ? (
          <ContactActionWrapper
            href={`mailto:${repository.contact}`}
            target="_blank"
            className={"is-size-5"}
            title={t("overview.contact", { contact: repository.contact })}
            tabIndex={1}
          >
            {renderContactIcon()}
          </ContactActionWrapper>
        ) : null}
        <QuickAction
          className={classNames("is-clickable", "is-size-5", "has-hover-visible")}
          name="download"
          color="info"
          onClick={() => setOpenCloneModal(true)}
          title={t("overview.clone")}
        />
      </span>
      <small className="pb-1">
        <DateFromNow baseDate={baseDate} date={repository.lastModified || repository.creationDate} />
      </small>
    </ContentRightContainer>
  );

  const repositoryLink = `/repo/${repository.namespace}/${repository.name}/`;
  const actions = createContentRight();
  const name = (
    <div className="is-flex">
      <ExtensionPoint<extensionPoints.RepositoryCardBeforeTitle>
        name="repository.card.beforeTitle"
        props={{ repository }}
      />
      <Name>{repository.name}</Name> <RepositoryFlags repository={repository} className="is-hidden-mobile" />
    </div>
  );

  return (
    <>
      <GroupEntry
        avatar={<RepositoryAvatar repository={repository} size={48} />}
        name={name}
        description={repository.description}
        contentRight={actions}
        link={repositoryLink}
        ariaLabel={repository.name}
      />
    </>
  );
};

export default RepositoryEntry;
