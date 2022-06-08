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

import React, { FC, useMemo, useState } from "react";
import styled from "styled-components";
import { apiClient, Button, Modal, devices } from "@scm-manager/ui-components";
import { HalRepresentation, IndexResources, Link } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { ApiResult, createQueryString } from "@scm-manager/ui-api";
import { useQuery } from "react-query";
import { useThemeState } from "./Theme";

type Props = {
  index: IndexResources;
};

const useFeedbackUrl = (url: string): ApiResult<HalRepresentation> =>
  useQuery(["config", "feedback"], () => apiClient.get(url).then(r => r.json()), {
    refetchOnWindowFocus: false
  });

const createFeedbackFormUrl = (instanceId: string, scmVersion: string, theme: string, data?: HalRepresentation) => {
  if (data?._links?.form) {
    const formUrl = (data._links.form as Link).href;
    return `${formUrl}?${createQueryString({ instanceId, scmVersion, theme })}`;
  }
  return "";
};

const useFeedback = (index: IndexResources) => {
  const feedbackUrl = (index._links.feedback as Link)?.href || "";
  const { theme } = useThemeState();
  const { data, error, isLoading } = useFeedbackUrl(feedbackUrl);
  const formUrl = useMemo(() => createFeedbackFormUrl(index.instanceId, index.version, theme, data), [
    theme,
    data,
    index.instanceId,
    index.version
  ]);

  if (!index._links.feedback || error || isLoading || !formUrl) {
    return {
      isAvailable: false,
      formUrl: ""
    };
  }

  return {
    isAvailable: true,
    formUrl
  };
};

const Feedback: FC<Props> = ({ index }) => {
  const { isAvailable, formUrl } = useFeedback(index);
  const [showModal, setShowModal] = useState(false);

  if (isAvailable && !showModal) {
    return <FeedbackTriggerButton openModal={() => setShowModal(true)} />;
  }
  if (showModal) {
    return <FeedbackForm close={() => setShowModal(false)} formUrl={formUrl} />;
  }

  return null;
};

const TriggerButton = styled(Button)`
  position: fixed;
  z-index: 5;
  left: 1rem;
  right: unset;
  bottom: -1px;
  border-radius: 0.2rem 0.2rem 0 0;
  @media screen and (min-width: ${devices.desktop.width}px) {
    right: 1rem;
    left: unset;
  }
`;

const ModalWrapper = styled(Modal)`
  .modal-card-body {
    padding: 0;
  }
`;

const FeedbackTriggerButton: FC<{ openModal: () => void }> = ({ openModal }) => {
  const [t] = useTranslation("commons");
  return <TriggerButton action={openModal} color="info" label={t("feedback.button")} icon="comment" />;
};

type FormProps = {
  close: () => void;
  formUrl: string;
};

const FeedbackWrapper = styled.div`
  height: 45rem;
  width: auto;
`;

const FeedbackForm: FC<FormProps> = ({ close, formUrl }) => {
  const [t] = useTranslation("commons");

  return (
    <ModalWrapper title={t("feedback.modalTitle")} active={true} closeFunction={close}>
      <FeedbackWrapper>
        <iframe src={formUrl} height="100%" width="100%" title="feedback-form" />
      </FeedbackWrapper>
    </ModalWrapper>
  );
};

export default Feedback;
