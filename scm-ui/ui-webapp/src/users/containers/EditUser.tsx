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
import DeleteUser from "./DeleteUser";
import { User } from "@scm-manager/ui-types";
import UserConverter from "../components/UserConverter";
import { Form, useUpdateResource } from "@scm-manager/ui-forms";
import * as userValidator from "../components/userValidation";
import { Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  user: User;
};

const EditUser: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const { submit } = useUpdateResource<User>(user, (user) => user.name, {
    contentType: "application/vnd.scmm-user+json;v=2",
    collectionName: ["user", "users"],
  });

  return (
    <>
      <Subtitle subtitle={t("userForm.subtitle")} />
      <Form translationPath={["users", "createUser.form"]} defaultValues={user} onSubmit={submit}>
        <Form.Row>
          <Form.Input
            name="displayName"
            rules={{
              validate: userValidator.isDisplayNameValid,
            }}
          />
          <Form.Input
            name="mail"
            className="is-half"
            rules={{
              validate: (email) => !email || userValidator.isMailValid(email),
            }}
          />
        </Form.Row>
        <Form.Row hidden={user.external}>
          <Form.Checkbox name="active" />
        </Form.Row>
      </Form>
      <hr />
      <UserConverter user={user} />
      <DeleteUser user={user} />
    </>
  );
};

export default EditUser;
