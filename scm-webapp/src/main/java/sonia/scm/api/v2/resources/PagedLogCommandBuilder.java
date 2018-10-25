package sonia.scm.api.v2.resources;

import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

class PagedLogCommandBuilder {
  private final RepositoryService repositoryService;
  private int page;
  private int pageSize ;

  PagedLogCommandBuilder(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  PagedLogCommandBuilder page(int page) {
    this.page = page;
    return this;
  }

  PagedLogCommandBuilder pageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  LogCommandBuilder create() {
    return repositoryService.getLogCommand()
      .setPagingStart(page * pageSize)
      .setPagingLimit(pageSize);
  }
}
