import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import Image from "./Image";

type Props = WithTranslation & {
  message?: string;
};

const Wrapper = styled.div`
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 256px;
`;

const FixedSizedImage = styled(Image)`
  margin-bottom: 0.75rem;
  width: 128px;
  height: 128px;
`;

class Loading extends React.Component<Props> {
  render() {
    const { message, t } = this.props;
    return (
      <Wrapper className="is-flex">
        <FixedSizedImage src="/images/loading.svg" alt={t("loading.alt")} />
        <p className="has-text-centered">{message}</p>
      </Wrapper>
    );
  }
}

export default withTranslation("commons")(Loading);
