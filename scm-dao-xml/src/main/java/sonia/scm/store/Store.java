package sonia.scm.store;

import java.io.File;

public enum Store {
  CONFIG("config"),
  DATA("data"),
  BLOB("blob");

  private static final String GLOBAL_STORE_BASE_DIRECTORY = "var";

  private String directory;

  Store(String directory) {

    this.directory = directory;
  }

  /**
   * Get the relkative store directory path to be stored in the repository root
   * <p>
   * The repository store directories are:
   * repo_base_dir/config/
   * repo_base_dir/blob/
   * repo_base_dir/data/
   *
   * @return the relative store directory path to be stored in the repository root
   */
  public String getRepositoryStoreDirectory() {
    return directory;
  }

  /**
   * Get the relative store directory path to be stored in the global root
   * <p>
   * The global store directories are:
   * base_dir/config/
   * base_dir/var/blob/
   * base_dir/var/data/
   *
   * @return the relative store directory path to be stored in the global root
   */
  public String getGlobalStoreDirectory() {
    if (this.equals(CONFIG)) {
      return directory;
    }
    return GLOBAL_STORE_BASE_DIRECTORY + File.separator + directory;
  }
}
