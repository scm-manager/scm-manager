//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";
import { translate } from "react-i18next";

type Props = {
  filter: string => void,

  // context props
  classes: Object,
  t: string => string
};

type State = {
  value: string
};

const styles = {
  inputField: {
    float: "right",
    marginTop: "1.25rem",
    marginRight: "1.25rem"
  },
  inputHeight: {
    height: "2.5rem"
  }
};

class FilterInput extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = { value: "" };
  }

  handleChange = event => {
    this.setState({ value: event.target.value });
  };

  handleSubmit = event => {
    this.props.filter(this.state.value);
    event.preventDefault();
  };

  render() {
    const { classes, t } = this.props;
    return (
      <form
        className={classNames(classes.inputField, "input-field")}
        onSubmit={this.handleSubmit}
      >
        <div className="control has-icons-left">
          <input
            className={classNames(classes.inputHeight, "input")}
            type="search"
            placeholder={t("filterEntries")}
            value={this.state.value}
            onChange={this.handleChange}
          />
          <span className="icon is-small is-left">
            <i className="fas fa-search" />
          </span>
        </div>
      </form>
    );
  }
}

export default injectSheet(styles)(translate("commons")(FilterInput));
