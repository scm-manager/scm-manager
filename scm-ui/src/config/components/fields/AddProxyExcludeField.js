//@flow
import React from "react";

import { translate } from "react-i18next";
import { AddButton } from "../../../components/buttons";
import InputField from "../../../components/forms/InputField";

type Props = {
  t: string => string,
  addProxyExclude: string => void
};

type State = {
  proxyExcludeToAdd: string,
  //validationError: boolean
};

class AddProxyExcludeField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      proxyExcludeToAdd: "",
      //validationError: false
    };
  }

  render() {
    const { t } = this.props;
    return (
      <div className="field">
        <InputField

          label={t("proxy-settings.add-proxy-exclude-textfield")}
          errorMessage={t("proxy-settings.add-proxy-exclude-error")}
          onChange={this.handleAddProxyExcludeChange}
          validationError={false}
          value={this.state.proxyExcludeToAdd}
          onReturnPressed={this.appendProxyExclude}
        />
        <AddButton
          label={t("proxy-settings.add-proxy-exclude-button")}
          action={this.addButtonClicked}
          //disabled={!isMemberNameValid(this.state.memberToAdd)}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendProxyExclude();
  };

  appendProxyExclude = () => {
    const { proxyExcludeToAdd } = this.state;
    //if (isMemberNameValid(memberToAdd)) {
    this.props.addProxyExclude(proxyExcludeToAdd);
    this.setState({ ...this.state, proxyExcludeToAdd: "" });
    // }
  };

  handleAddProxyExcludeChange = (username: string) => {
    this.setState({
      ...this.state,
      proxyExcludeToAdd: username,
      //validationError: membername.length > 0 && !isMemberNameValid(membername)
    });
  };
}

export default translate("config")(AddProxyExcludeField);
