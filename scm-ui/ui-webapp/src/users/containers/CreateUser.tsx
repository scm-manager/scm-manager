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
import { Redirect } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useRequiredIndexLink } from "@scm-manager/ui-api";
import { Page } from "@scm-manager/ui-components";
import { Form, useCreateResource } from "@scm-manager/ui-forms";
import * as userValidator from "../components/userValidation";
import { User, UserCreation } from "@scm-manager/ui-types";

type UserCreationForm = Pick<UserCreation, "password" | "name" | "displayName" | "active" | "external" | "mail"> & {
  passwordConfirmation: string;
};

const CreateUser: FC = () => {
  const [t] = useTranslation("users");
  const indexLink = useRequiredIndexLink("users");
  const { submit, submissionResult: createdUser } = useCreateResource<UserCreationForm, User>(
    indexLink,
    ["user", "users"],
    (user) => user.name,
    {
      contentType: "application/vnd.scmm-user+json;v=2",
    }
  );

  if (!!createdUser) {
    return <Redirect to={`/user/${createdUser.name}`} />;
  }

  return (
    <Page title={t("createUser.title")} subtitle={t("createUser.subtitle")} showContentOnError={true}>
      <Form
        onSubmit={submit}
        translationPath={["users", "createUser.form"]}
        defaultValues={{
          name: "",
          password: "",
          passwordConfirmation: "",
          active: true,
          external: false,
          displayName: "",
          mail: "",
        }}
      >
        {({ watch }) => (
          <>
            <Form.Row>
              <Form.Input
                name="name"
                rules={{
                  validate: userValidator.isNameValid,
                }}
                testId="input-username"
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

export default CreateUser;
