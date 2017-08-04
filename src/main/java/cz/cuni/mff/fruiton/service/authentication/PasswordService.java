package cz.cuni.mff.fruiton.service.authentication;

import java.security.spec.InvalidKeySpecException;

public interface PasswordService {

    class Hash {

        private String salt;
        private String hash;

        public Hash(final String salt, final String hash) {
            this.salt = salt;
            this.hash = hash;
        }

        public String getSalt() {
            return salt;
        }


        public String getHash() {
            return hash;
        }
    }

    Hash getPasswordHash(String password) throws InvalidKeySpecException;

    boolean isPasswordEqual(String password, String salt, String passwordHash) throws InvalidKeySpecException;

}
