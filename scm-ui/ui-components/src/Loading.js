//@flow
import React from "react";
import { translate } from "react-i18next";
import styled from "styled-components";
import Image from "./Image";

type Props = {
  t: string => string,
  message?: string
};

const Wrapper = styled.div`
  align-items: center;
  justify-content: center;
  min-height: 256px;
`;

const FixedSizedImage = styled(Image)`
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

export default translate("commons")(Loading);
