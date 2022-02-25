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
import styled from "styled-components";
import { apiClient, Button, Modal } from "@scm-manager/ui-components";
import { HalRepresentation, IndexResources, Link } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { ApiResult } from "@scm-manager/ui-api";
import { useQuery } from "react-query";
import { useThemeState } from "./Theme";

type Props = {
  index: IndexResources;
};

const useFeedbackUrl = (url: string): ApiResult<HalRepresentation> =>
  useQuery(["feedback"], () => apiClient.get(url).then(r => r.json()), { refetchOnWindowFocus: false });

const useFeedback = (index: IndexResources) => {
  const feedbackUrl = (index._links.feedback as Link).href;
  const { data, error, isLoading } = useFeedbackUrl("http://localhost:8080/api/v1/feedback/scm-manager/url");
  const { theme } = useThemeState();

  if (error || isLoading) {
    return {
      isAvailable: false,
      formUrl: ""
    };
  }

  let formUrl = (data?._links.form as Link).href;
  if ((data?._links.form as Link).templated) {
    formUrl = formUrl.replace("{page}", encodeURIComponent(""));
    formUrl = formUrl.replace("{instanceId}", encodeURIComponent(index.instanceId));
    formUrl = formUrl.replace("{theme}", encodeURIComponent(theme));
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
  z-index: 9999999;
  right: 1rem;
  bottom: -1px;
  border-radius: 0.2rem 0.2rem 0 0;
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
  height: 30rem;
  width: auto;
`;

const FeedbackForm: FC<FormProps> = ({ close, formUrl }) => {
  const [t] = useTranslation("commons");

  return (
    <Modal title={t("feedback.modalTitle")} active={true} closeFunction={close}>
      <FeedbackWrapper>
        <iframe src={formUrl} height="100%" width="100%" title="feedback-form" />
      </FeedbackWrapper>
    </Modal>
  );
};

export default Feedback;
