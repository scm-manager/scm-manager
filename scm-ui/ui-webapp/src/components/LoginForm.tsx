import React from "react";
import { translate } from "react-i18next";
import styled from "styled-components";
import { Image, ErrorNotification, InputField, SubmitButton, UnauthorizedError } from "@scm-manager/ui-components";

type Props = {
  error?: Error;
  loading: boolean;
  loginHandler: (username: string, password: string) => void;

  // context props
  t: (p: string) => string;
};

type State = {
  username: string;
  password: string;
};

const TopMarginBox = styled.div`
  margin-top: 5rem;
`;

const AvatarWrapper = styled.figure`
  margin-top: -70px;
  padding-bottom: 20px;
`;

const AvatarImage = styled(Image)`
  width: 128px;
  height: 128px;
  padding: 5px;
  background: #fff;
  border: 1px solid lightgray;
  border-radius: 50%;
`;

class LoginForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      username: "",
      password: ""
    };
  }

  handleSubmit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.loginHandler(this.state.username, this.state.password);
    }
  };

  handleUsernameChange = (value: string) => {
    this.setState({
      username: value
    });
  };

  handlePasswordChange = (value: string) => {
    this.setState({
      password: value
    });
  };

  isValid() {
    return this.state.username && this.state.password;
  }

  areCredentialsInvalid() {
    const { t, error } = this.props;
    if (error instanceof UnauthorizedError) {
      return new Error(t("errorNotification.wrongLoginCredentials"));
    } else {
      return error;
    }
  }

  render() {
    const { loading, t } = this.props;
    return (
      <div className="column is-4 box has-text-centered has-background-white-ter">
        <h3 className="title">{t("login.title")}</h3>
        <p className="subtitle">{t("login.subtitle")}</p>
        <TopMarginBox className="box">
          <AvatarWrapper>
            <AvatarImage src="/images/blib.jpg" alt={t("login.logo-alt")} />
          </AvatarWrapper>
          <ErrorNotification error={this.areCredentialsInvalid()} />
          <form onSubmit={this.handleSubmit}>
            <InputField
              placeholder={t("login.username-placeholder")}
              autofocus={true}
              onChange={this.handleUsernameChange}
            />
            <InputField
              placeholder={t("login.password-placeholder")}
              type="password"
              onChange={this.handlePasswordChange}
            />
            <SubmitButton label={t("login.submit")} fullWidth={true} loading={loading} />
          </form>
        </TopMarginBox>
      </div>
    );
  }
}

export default translate("commons")(LoginForm);
