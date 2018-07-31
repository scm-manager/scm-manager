//@flow
import React from "react";
import ErrorNotification from "./ErrorNotification";

type Props = {
  error: Error,
  title: string,
  subtitle: string
};

class ErrorPage extends React.Component<Props> {
  render() {
    const { title, subtitle, error } = this.props;

    return (
      <section className="section">
        <div className="box column is-4 is-offset-4 container">
          <h1 className="title">{title}</h1>
          <p className="subtitle">{subtitle}</p>
          <ErrorNotification error={error} />
        </div>
      </section>
    );
  }
}

export default ErrorPage;
