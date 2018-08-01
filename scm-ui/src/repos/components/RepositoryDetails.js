//@flow
import React from "react";
import type {Repository} from '../types/Repositories';

type Props = {
  repository: Repository
};

class RepositoryDetails extends React.Component<Props> {
  render() {
    const { repository } = this.props;
    return <div>{repository.description}</div>;
  }
}

export default RepositoryDetails;
