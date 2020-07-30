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
import { useTranslation } from "react-i18next";
import { Signature } from "@scm-manager/ui-types";
import styled from "styled-components";
import Icon from "../../Icon";
import Tooltip from "../../Tooltip";

type Props = {
  signatures: Signature[];
  className: any;
};

const StyledIcon = styled(Icon)`
  width: 1em;
  height: 1em;
  vertical-align: middle;
  border-radius: 0.25em;
  margin-bottom: 0.2em;
`;


const SignatureIcon: FC<Props> = ({ signatures, className }) => {
  const [t] = useTranslation("repos");

  const signature = signatures?.length > 0 ? signatures[0] : undefined;

  if (!signature) {
    return null;
  }

  const createTooltipMessage = () => {
    let status;
    if (signature.status === "VERIFIED") {
      status = t("changeset.signatureVerified");
    } else if (signature.status === "INVALID") {
      status = t("changeset.signatureInvalid");
    } else {
      status = t("changeset.signatureNotVerified");
    }

    if (signature.status === "NOT_FOUND") {
      return `${t("changeset.signatureStatus")}: ${status}`;
    }

    let message = `${t("changeset.keyOwner")}: ${signature.owner ? signature.owner : t("changeset.noOwner")}\n${t("changeset.keyId")}: ${signature.keyId}\n${t(
      "changeset.signatureStatus"
    )}: ${status}`;

    if (signature.contacts?.length > 0) {
      message += `\n${t("changeset.keyContacts")}:`;
      signature.contacts.forEach((contact) => {
        message += `\n- ${contact}`;
      });
    }
    return message;
  };

  const getColor = () => {
    if (signature.status === "VERIFIED") {
      return "success";
    }
    if (signature.status === "INVALID") {
      return "danger";
    }
    return undefined;
  };


  return (
    <Tooltip location="top" message={createTooltipMessage()}>
      <StyledIcon name="key" className={className} color={getColor()} />
    </Tooltip>
  );
};

export default SignatureIcon;
