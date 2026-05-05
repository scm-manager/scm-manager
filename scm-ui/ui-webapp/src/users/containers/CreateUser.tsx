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

import React, { FC, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Navigate } from "react-router-dom";
import { useUsers } from "@scm-manager/ui-api";
import { Page } from "@scm-manager/ui-components";
import {
  ErrorNotification,
  Form,
  Loading,
  Notification,
  useCreateResource,
  useDocumentTitle,
} from "@scm-manager/ui-core";
import { Link, User, UserCollection, UserCreation } from "@scm-manager/ui-types";
import * as userValidator from "../components/userValidation";

type UserCreationForm = Pick<UserCreation, "password" | "name" | "displayName" | "active" | "external" | "mail"> & {
  passwordConfirmation: string;
};

const CreateUserForm: FC<{ users: UserCollection }> = ({ users }) => {
  const [t] = useTranslation("users");
  useDocumentTitle(t("createUser.title"));
  const { submit, submissionResult: createdUser } = useCreateResource<UserCreationForm, User>(
    (users._links.create as Link).href,
    ["user", "users"],
    (user) => user.name,
    {
      contentType: "application/vnd.scmm-user+json;v=2",
    },
  );
  const defaultValuesRef = useRef({
    name: "",
    password: "",
    passwordConfirmation: "",
    active: true,
    external: false,
    displayName: "",
    mail: "",
  });

  if (createdUser) {
    return <Navigate to={`/user/${createdUser.name}`} />;
  }

  return (
    <Page title={t("createUser.title")} subtitle={t("createUser.subtitle")} showContentOnError={true}>
      {users.externalAuthenticationAvailable && (
        <Notification type="warning">{t("createUser.notification")}</Notification>
      )}
      <Form onSubmit={submit} translationPath={["users", "createUser.form"]} defaultValues={defaultValuesRef.current}>
        {({ watch }) => (
          <>
            <Form.Row>
              <Form.Input
                name="name"
                rules={{
                  validate: userValidator.isNameValid,
                }}
                testId="input-username"
                autoFocus
              />
              <Form.Input
                name="displayName"
                rules={{
                  validate: userValidator.isDisplayNameValid,
                }}
              />
            </Form.Row>
            <Form.Row>
              <Form.Input
                name="mail"
                className="is-half"
                rules={{
                  validate: (email) => !email || userValidator.isMailValid(email),
                }}
              />
            </Form.Row>
            <Form.Row>
              <Form.Checkbox
                name="external"
                rules={{
                  deps: ["password"],
                }}
              />
            </Form.Row>
            <Form.Row hidden={watch("external")}>
              <Form.SecretConfirmation
                name="password"
                rules={{
                  validate: userValidator.isPasswordValid,
                }}
              />
            </Form.Row>
            <Form.Row hidden={watch("external")}>
              <Form.Checkbox name="active" />
            </Form.Row>
          </>
        )}
      </Form>
    </Page>
  );
};

const CreateUser: FC = () => {
  const { data: users, isLoading, error } = useUsers({ page: 0 });

  if (isLoading) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }

  return <CreateUserForm users={users!} />;
};

export default CreateUser;
