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
import { usePopover } from "../../popover";
import Popover from "../../popover/Popover";
import classNames from "classnames";

type Props = {
  signatures: Signature[];
  className?: string;
};

const StyledIcon = styled(Icon)`
  width: 1em;
  height: 1em;
  vertical-align: middle;
  border-radius: 0.25em;
  margin-bottom: 0.2em;
`;

const StyledDiv = styled.div`
  > *:not(:last-child) {
    margin-bottom: 24px;
  }
`;

const SignatureIcon: FC<Props> = ({ signatures, className }) => {
  const [t] = useTranslation("repos");
  const { popoverProps, triggerProps } = usePopover();

  if (!signatures.length) {
    return null;
  }

  const getColor = (signaturesToVerify: Signature[]) => {
    const invalid = signaturesToVerify.some((sig) => sig.status === "INVALID");
    if (invalid) {
      return "danger";
    }
    const verified = signaturesToVerify.some((sig) => sig.status === "VERIFIED");
    if (verified) {
      return "success";
    }
    return undefined;
  };

  const createSignatureBlock = (signature: Signature) => {
    let status;
    if (signature.status === "VERIFIED") {
      status = t("changeset.signatureVerified");
    } else if (signature.status === "INVALID") {
      status = t("changeset.signatureInvalid");
    } else {
      status = t("changeset.signatureNotVerified");
    }

    if (signature.status === "NOT_FOUND") {
      return (
        <p>
          <div>
            {t("changeset.keyId")}: {signature.keyId}
          </div>
          <div>
            {t("changeset.signatureStatus")}: {status}
          </div>
        </p>
      );
    }

    return (
      <p>
        <div>
          {t("changeset.keyId")}:{" "}
          {signature._links?.rawKey ? <a href={signature._links.rawKey.href}>{signature.keyId}</a> : signature.keyId}
        </div>
        <div>
          {t("changeset.signatureStatus")}:{" "}
          <span className={classNames(`has-text-${getColor([signature])}`)}>{status}</span>
        </div>
        <div>
          {t("changeset.keyOwner")}: {signature.owner || t("changeset.noOwner")}
        </div>
        {signature.contacts && signature.contacts.length > 0 && (
          <>
            <div>{t("changeset.keyContacts")}:</div>
            {signature.contacts &&
              signature.contacts.map((contact) => (
                <div>
                  - {contact.name}
                  {contact.mail && ` <${contact.mail}>`}
                </div>
              ))}
          </>
        )}
      </p>
    );
  };

  const signatureElements = signatures.map((signature) => createSignatureBlock(signature));

  return (
    <>
      <Popover
        title={<h1 className="has-text-weight-bold is-size-5">{t("changeset.signatures")}</h1>}
        width={500}
        {...popoverProps}
      >
        <StyledDiv>{signatureElements}</StyledDiv>
      </Popover>
      <div {...triggerProps}>
        <StyledIcon name="key" className={className} color={getColor(signatures)} />
      </div>
    </>
  );
};

export default SignatureIcon;
