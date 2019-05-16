package sonia.scm.repository;

import sonia.scm.GenericDAO;

import java.util.List;

public interface RepositoryRoleDAO extends GenericDAO<RepositoryRole> {
  @Override
  List<RepositoryRole> getAll();
}
