package sonia.scm.store;

import java.io.File;

public enum Store {
  CONFIG("config"),
  DATA("data"),
  BLOB("blob");

  private static final String GLOBAL_STORE_BASE_DIRECTORY = "var";
  private static final String STORE_DIRECTORY = "store";

  private String directory;

  Store(String directory) {

    this.directory = directory;
  }

  /**
   * Get the relative store directory path to be stored in the repository root
   * <p>
   * The repository store directories are:
   * repo_base_dir/store/config/
   * repo_base_dir/store/blob/
   * repo_base_dir/store/data/
   *
   * @return the relative store directory path to be stored in the repository root
   */
  public String getRepositoryStoreDirectory() {
    return STORE_DIRECTORY + File.separator + directory;
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
