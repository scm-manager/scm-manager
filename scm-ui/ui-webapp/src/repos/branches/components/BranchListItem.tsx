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

import { Card, CardList } from "@scm-manager/ui-layout";
import { Link } from "react-router-dom";
import { encodePart } from "../../sources/components/content/FileLink";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";
import { Trans, useTranslation } from "react-i18next";
import { DateFromNow } from "@scm-manager/ui-components";
import { ExtensionPoint, extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import React, { FC } from "react";
import { Branch, BranchDetails, Repository } from "@scm-manager/ui-types";
import AheadBehindTag from "./AheadBehindTag";
import { Dialog, Menu } from "@scm-manager/ui-overlays";
import { Icon } from "@scm-manager/ui-core";

type Props = {
  branch: Branch;
  remove: (branch: Branch) => void;
  isLoading: boolean;
  baseUrl: string;
  repository: Repository;
  branchesDetails?: BranchDetails[];
};

const BranchListItem: FC<Props> = ({ branch, remove, isLoading, branchesDetails, baseUrl, repository }) => {
  const [t] = useTranslation("repos");
  const binder = useBinder();
  const branchDetails = branchesDetails?.find(({ branchName }) => branchName === branch.name);
  const canDelete = "delete" in branch._links;
  const hasAction =
    canDelete ||
    binder.hasExtension<extensionPoints.BranchListMenu>("branches.list.menu", {
      repository,
      branch,
    });
  const hasDetails =
    !branch.defaultBranch ||
    binder.hasExtension<extensionPoints.BranchListDetail>("branches.list.detail", {
      branch,
      branchDetails,
      repository,
    });
  return (
    <CardList.Card
      key={branch.name}
      action={
        hasAction ? (
          <Menu>
            {canDelete ? (
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
            ) : null}
            <ExtensionPoint<extensionPoints.BranchListMenu>
              name="branches.list.menu"
              props={{
                repository,
                branch,
              }}
              renderAll
            />
          </Menu>
        ) : undefined
      }
    >
      <Card.Row className="is-flex">
        <Card.Title>
          <Link to={`${baseUrl}/${encodePart(branch.name)}/info`} ref={useKeyboardIteratorTarget()}>
            {branch.name}
          </Link>
        </Card.Title>
      </Card.Row>
      <Card.Row className="is-flex is-flex-wrap-wrap is-size-7 has-text-secondary is-white-space-pre">
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
      </Card.Row>
      {hasDetails ? (
        <Card.Row>
          <Card.Details>
            {!branch.defaultBranch ? (
              <Card.Details.Detail>
                <Card.Details.Detail.Label>{t("branch.aheadBehind.label")}</Card.Details.Detail.Label>
                <AheadBehindTag branch={branch} details={branchDetails} />
              </Card.Details.Detail>
            ) : null}
            <ExtensionPoint<extensionPoints.BranchListDetail>
              name="branches.list.detail"
              props={{
                repository,
                branch,
                branchDetails,
              }}
              renderAll
            />
          </Card.Details>
        </Card.Row>
      ) : null}
    </CardList.Card>
  );
};

export default BranchListItem;
