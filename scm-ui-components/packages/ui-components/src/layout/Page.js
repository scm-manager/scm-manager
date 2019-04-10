//@flow
import * as React from "react";
import { compose } from "redux";
import injectSheet from "react-jss";
import classNames from "classnames";
import { translate } from "react-i18next";
import Loading from "./../Loading";
import ErrorNotification from "./../ErrorNotification";
import Title from "./Title";
import Subtitle from "./Subtitle";
import PageActions from "./PageActions";
import ErrorBoundary from "../ErrorBoundary";

type State = {
  value: string
};

type Props = {
  title?: string,
  subtitle?: string,
  loading?: boolean,
  error?: Error,
  showContentOnError?: boolean,
  children: React.Node,
  filter: string => void,

  // context props
  classes: Object,
  t: string => string
};

const styles = {
  actions: {
    display: "flex",
    justifyContent: "flex-end"
  },
  inputField: {
    float: "right",
    marginTop: "1.25rem",
    marginRight: "1.25rem"
  },
  inputHeight: {
    height: "2.5rem"
  },
  button: {
    float: "right",
    marginTop: "1.25rem"
  }
};

class Page extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = { value: "" };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({ value: event.target.value });
  }

  handleSubmit(event) {
    this.props.filter(this.state.value);
    event.preventDefault();
  }

  render() {
    const { error } = this.props;
    return (
      <section className="section">
        <div className="container">
          {this.renderPageHeader()}
          <ErrorBoundary>
            <ErrorNotification error={error} />
            {this.renderContent()}
          </ErrorBoundary>
        </div>
      </section>
    );
  }

  renderPageHeader() {
    const { title, subtitle, children, classes, t } = this.props;

    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && child.type.name === PageActions.name) {
        pageActions = (
          <div
            className={classNames(
              classes.actions,
              "column is-three-fifths is-mobile-action-spacing"
            )}
          >
            <form className={classNames(classes.inputField, "input-field")}>
              <div
                className="control has-icons-left"
                onSubmit={this.handleSubmit}
              >
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
            <div className={classNames(classes.button, "input-button control")}>
              {child}
            </div>
          </div>
        );
        pageActionsExists = true;
      }
    });
    let underline = pageActionsExists ? (
      <hr className="header-with-actions" />
    ) : null;

    return (
      <>
        <div className="columns">
          <div className="column">
            <Title title={title} />
            <Subtitle subtitle={subtitle} />
          </div>
          {pageActions}
        </div>
        {underline}
      </>
    );
  }

  renderContent() {
    const { loading, children, showContentOnError, error } = this.props;

    if (error && !showContentOnError) {
      return null;
    }
    if (loading) {
      return <Loading />;
    }

    let content = [];
    React.Children.forEach(children, child => {
      if (child && child.type.name !== PageActions.name) {
        content.push(child);
      }
    });
    return content;
  }
}

export default compose(
  injectSheet(styles),
  translate("commons")
)(Page);
