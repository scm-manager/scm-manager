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
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import GroupEntry from "../layout/GroupEntry";
import RepositoryFlags from "./RepositoryFlags";
import styled from "styled-components";
import Icon from "../Icon";
import { useTranslation } from "react-i18next";

type DateProp = Date | string;

type Props = {
  repository: Repository;
  // @VisibleForTesting
  // the baseDate is only to avoid failing snapshot tests
  baseDate?: DateProp;
};

const ContentRightContainer = styled.div`
  height: calc(80px - 1.5rem);
  margin-right: 1rem;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
`;

const DateWrapper = styled.small`
  padding-bottom: 0.25rem;
`;

const QuickActionbar = styled.span`
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
`;

const QuickAction = styled(Icon)`
  font-size: 1.25rem;

  :hover {
    color: #363636 !important;
  }
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

  const createContentRight = () => (
    <ContentRightContainer>
      <Modal
        size="L"
        active={openCloneModal}
        title={t("overview.clone")}
        body={
          <ExtensionPoint
            name="repos.repository-details.information"
            renderAll={true}
            props={{
              repository,
            }}
          />
        }
        closeFunction={() => setOpenCloneModal(false)}
      />
      <QuickActionbar>
        <QuickAction
          name="download"
          color="info"
          className="has-cursor-pointer"
          onClick={() => setOpenCloneModal(true)}
          title={t("overview.clone")}
        />
      </QuickActionbar>
      <DateWrapper>
        <DateFromNow baseDate={baseDate} date={repository.lastModified || repository.creationDate} />
      </DateWrapper>
    </ContentRightContainer>
  );

  const repositoryLink = `/repo/${repository.namespace}/${repository.name}/`;
  const actions = createContentRight();
  const name = (
    <div className="is-flex">
      <ExtensionPoint name="repository.card.beforeTitle" props={{ repository }} />
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
      />
    </>
  );
};

export default RepositoryEntry;
