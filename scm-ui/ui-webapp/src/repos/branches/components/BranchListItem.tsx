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

import { Dialog, Menu } from "@scm-manager/ui-overlays";
import { Icon } from "@scm-manager/ui-buttons";
import { CardList } from "@scm-manager/ui-layout";
import { Link } from "react-router-dom";
import { encodePart } from "../../sources/components/content/FileLink";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";
import { Trans, useTranslation } from "react-i18next";
import { DateFromNow, useGeneratedId } from "@scm-manager/ui-components";
import { extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import React, { FC } from "react";
import { Branch, BranchDetails, Repository } from "@scm-manager/ui-types";
import styled from "styled-components";

const DetailsContainer = styled(CardList.Card.Row)`
  gap: 0.5rem 1rem;
`;

const BranchDetail: FC<{
  branch: Branch;
  repository: Repository;
  detail: extensionPoints.BranchListDetail["type"];
}> = ({ repository, detail, branch }) => {
  const labelId = useGeneratedId();
  const renderedDetail = detail.render({ branch, repository, labelId });
  if (!renderedDetail) {
    return null;
  }
  return (
    <span key={detail.name}>
      <span className="has-text-secondary mr-1" id={labelId}>
        {detail.name}
      </span>
      {renderedDetail}
    </span>
  );
};

type Props = {
  branch: Branch;
  defaultBranchDetails: extensionPoints.BranchListDetail["type"][];
  remove: (branch: Branch) => void;
  isLoading: boolean;
  baseUrl: string;
  repository: Repository;
  branchDetails?: BranchDetails;
};

const BranchListItem: FC<Props> = ({ branch, defaultBranchDetails, remove, isLoading, baseUrl, repository }) => {
  const binder = useBinder();
  const [t] = useTranslation("repos");

  return (
    <CardList.Card
      key={branch.name}
      action={
        "delete" in branch._links ? (
          <Menu>
            <Menu.DialogButton
              title={t("branch.delete.confirmAlert.title")}
              description={t("branch.delete.confirmAlert.message", { branch: branch.name })}
              footer={[
                <Dialog.CloseButton key="yes" onClick={() => remove(branch)} isLoading={isLoading}>
                  {t("branch.delete.confirmAlert.submit")}
                </Dialog.CloseButton>,
                <Dialog.CloseButton key="no" variant="primary" autoFocus>
                  {t("branch.delete.confirmAlert.cancel")}
                </Dialog.CloseButton>,
              ]}
            >
              <Icon>trash</Icon>
              {t("branch.delete.button")}
            </Menu.DialogButton>
          </Menu>
        ) : undefined
      }
    >
      <CardList.Card.Row className="is-flex">
        <CardList.Card.Title>
          <Link to={`${baseUrl}/${encodePart(branch.name)}/info`} ref={useKeyboardIteratorTarget()}>
            {branch.name}
          </Link>
        </CardList.Card.Title>
      </CardList.Card.Row>
      <CardList.Card.Row className="is-flex is-flex-wrap-wrap is-size-7 has-text-secondary is-white-space-pre">
        <Trans
          t={t}
          i18nKey="branches.table.branches.subtitle"
          values={{
            author: branch.lastCommitter?.name,
          }}
          components={{
            date: <DateFromNow className="is-relative" date={branch.lastCommitDate} />,
            space: <span />,
          }}
        />
      </CardList.Card.Row>
      <DetailsContainer className="is-flex is-flex-wrap-wrap is-align-items-center">
        {[
          ...defaultBranchDetails,
          ...binder.getExtensions<extensionPoints.BranchListDetail>("branches.list.detail", {
            branch,
            repository,
          }),
        ].map((detail) => (
          <BranchDetail key={detail.name} branch={branch} detail={detail} repository={repository} />
        ))}
      </DetailsContainer>
    </CardList.Card>
  );
};

export default BranchListItem;
