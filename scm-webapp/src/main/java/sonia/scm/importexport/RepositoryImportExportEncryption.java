/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.importexport;


import com.google.common.base.Strings;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.repository.api.ImportFailedException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

public class RepositoryImportExportEncryption {

  public static OutputStream encrypt(OutputStream os, String secret) {
    if (!Strings.isNullOrEmpty(secret)) {
      try {
        IvParameterSpec ivspec = createIvParamSpec();
        SecretKeySpec secretKey = createSecretKey(secret);
        Cipher cipher = createCipher();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        return new CipherOutputStream(os, cipher);
      } catch (GeneralSecurityException e) {
        throw new ExportFailedException(noContext(), "Could not encrypt repository on export.", e);
      }
    }
    return os;
  }

  public static InputStream decrypt(InputStream is, String secret) {
    if (!Strings.isNullOrEmpty(secret)) {
      try {
        IvParameterSpec ivspec = createIvParamSpec();
        SecretKeySpec secretKey = createSecretKey(secret);
        Cipher cipher = createCipher();
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        return new CipherInputStream(is, cipher);
      } catch (GeneralSecurityException e) {
        throw new ImportFailedException(noContext(), "Could not decrypt repository on import.", e);
      }
    }
    return is;
  }

  private static SecretKeySpec createSecretKey(String secret) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(secret.toCharArray(), "salt".getBytes(), 65536, 256);
    SecretKey tmp = factory.generateSecret(spec);
    return new SecretKeySpec(tmp.getEncoded(), "AES");
  }

  private static Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
    return Cipher.getInstance("AES/CBC/PKCS5Padding");
  }

  private static IvParameterSpec createIvParamSpec() {
    byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    return new IvParameterSpec(iv);
  }
}
