//@flow
import React from "react";
import ErrorNotification from "./ErrorNotification";
import { BackendError, ForbiddenError } from "./errors";

type Props = {
  error: Error,
  title: string,
  subtitle: string
};

class ErrorPage extends React.Component<Props> {
  render() {
    const { title, error } = this.props;

    return (
      <section className="section">
        <div className="box column is-4 is-offset-4 container">
          <h1 className="title">{title}</h1>
          {this.renderSubtitle()}
          <ErrorNotification error={error} />
        </div>
      </section>
    );
  }

  renderSubtitle = () => {
    const { error, subtitle } = this.props;
    if (error instanceof BackendError || error instanceof ForbiddenError) {
      return null;
    }
    return <p className="subtitle">{subtitle}</p>
  }
}

export default ErrorPage;
