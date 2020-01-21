package sonia.scm.repository;

import com.google.common.io.ByteSource;
import sonia.scm.plugin.ExtensionPoint;

import java.io.IOException;
import java.io.InputStream;

/**
 * Use this {@link RepositoryContentInitializer} to create new files with custom content
 * which will be included in the initial commit of the new repository
 */
@ExtensionPoint
public interface RepositoryContentInitializer {

  /**
   *
   * @param context add content to this context in order to commit files in the initial repository commit
   * @throws IOException
   */
  void initialize(InitializerContext context) throws IOException;

  /**
   * Use this {@link InitializerContext} to create new files on repository initialization
   * which will be included in the first commit
   */
  interface InitializerContext {

    /**
     * @return repository to which this initializerContext belongs to
     */
    Repository getRepository();

    /**
     * create new file which will be included in initial repository commit
     * @param path path of new file
     * @return
     */
    CreateFile create(String path);
  }

  /**
   * Use this to apply content to new files which should be committed on repository initialization
   */
  interface CreateFile {

    /**
     * Applies content to new file
     * @param content content of file as string
     * @return {@link InitializerContext}
     * @throws IOException
     */
    InitializerContext from(String content) throws IOException;

    /**
     * Applies content to new file
     * @param input content of file as input stream
     * @return {@link InitializerContext}
     * @throws IOException
     */
    InitializerContext from(InputStream input) throws IOException;

    /**
     * Applies content to new file
     * @param byteSource content of file as byte source
     * @return {@link InitializerContext}
     * @throws IOException
     */
    InitializerContext from(ByteSource byteSource) throws IOException;

  }
}
