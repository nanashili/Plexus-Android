package com.plexus.argon;

import java.util.Locale;

public final class Argon {

    private final int     tCostIterations;
    private final int     mCostKiB;
    private final int     parallelism;
    private final int     hashLength;
    private final Type    type;
    private final Version version;

    private Argon(Builder builder) {
        this.tCostIterations = builder.tCostIterations;
        this.mCostKiB        = builder.mCostKiB;
        this.parallelism     = builder.parallelism;
        this.hashLength      = builder.hashLength;
        this.type            = builder.type;
        this.version         = builder.version;
    }

    /**
     * Finds the type from the encoded hash.
     * @param encoded
     * @param password
     * @return
     * @throws UnknownTypeException If it cannot determine the type from the encoded hash.
     */
    public static boolean verify(String encoded, byte[] password) throws UnknownTypeException {
        return verify(encoded, password, Type.fromEncoded(encoded));
    }

    public static boolean verify(String encoded, byte[] password, Type type) {
        if (encoded  == null) throw new IllegalArgumentException();
        if (password == null) throw new IllegalArgumentException();

        byte[] defensivePasswordCopy = password.clone();

        return ArgonNative.verify(encoded, defensivePasswordCopy, type.nativeValue) == ArgonNative.OK;
    }

    public static class Builder {
        private final Version version;

        private int  tCostIterations = 3;
        private int  mCostKiB        = 1 << 12;
        private int  parallelism     = 1;
        private int  hashLength      = 32;
        private Type type            = Type.Argon2i;

        public Builder(Version version) {
            this.version = version;
        }

        /**
         * Type of Argon to use {@link Type#Argon2i} is the default.
         */
        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        /**
         * Sets parallelism to {@param n} threads (default 1)
         */
        public Builder parallelism(int n) {
            this.parallelism = n;
            return this;
        }

        /**
         * Sets the memory usage of 2^{@param n} KiB (default 12)
         *
         * @param n This function accepts [0..30]. 0 is 1 KiB and 30 is 1 TiB.
         */
        public Builder memoryCostOrder(int n) {
            if (n <  0) throw new IllegalArgumentException("n too small, minimum 0");
            if (n > 30) throw new IllegalArgumentException("n too high, maximum 30");
            return memoryCostKiB(1 << n);
        }

        /**
         * Sets the memory usage of {@param kib} KiB.
         */
        public Builder memoryCostKiB(int kib) {
            if (kib      < 4) throw new IllegalArgumentException("kib too small, minimum 4");
            if (kib % 4 != 0) throw new IllegalArgumentException("kib must be multiple of 4");
            this.mCostKiB = kib;
            return this;
        }

        /**
         * Sets the memory usage using the {@link MemoryCost} enum.
         */
        public Builder memoryCost(MemoryCost memoryCost) {
            return memoryCostKiB(memoryCost.getKiB());
        }

        /**
         * Sets the number of iterations to {@param n} (default = 3)
         */
        public Builder iterations(int n) {
            this.tCostIterations = n;
            return this;
        }

        /**
         * Output hash length, default 32.
         */
        public Builder hashLength(int hashLength) {
            this.hashLength = hashLength;
            return this;
        }

        public Argon build() {
            return new Argon(this);
        }
    }

    public Result hash(byte[] password, byte[] salt) throws ArgonException {
        if (salt     == null) throw new IllegalArgumentException();
        if (password == null) throw new IllegalArgumentException();

        StringBuffer encoded               = new StringBuffer();
        byte[]       hash                  = new byte[hashLength];
        byte[]       defensivePasswordCopy = password.clone();
        byte[]       defensiveSaltCopy     = salt.clone();

        int result = ArgonNative.hash(tCostIterations, mCostKiB, parallelism,
                defensivePasswordCopy,
                defensiveSaltCopy,
                hash,
                encoded,
                type.nativeValue,
                version.nativeValue);

        if (result != ArgonNative.OK) {
            throw new ArgonException(result, ArgonNative.resultToString(result));
        }

        return new Result(encoded.toString(), hash);
    }

    public final class Result {
        private final String encoded;
        private final byte[] hash;

        private Result(String encoded, byte[] hash) {
            this.encoded = encoded;
            this.hash    = hash;
        }

        public String getEncoded() {
            return encoded;
        }

        public byte[] getHash() {
            return hash;
        }

        public String getHashHex() {
            return toHex(hash);
        }

        @Override
        public String toString() {
            return String.format(Locale.US,
                    "Type:           %s%n" +
                            "Iterations:     %d%n" +
                            "Memory:         %d KiB%n" +
                            "Parallelism:    %d%n" +
                            "Hash:           %s%n" +
                            "Encoded:        %s%n",
                    type,
                    tCostIterations,
                    mCostKiB,
                    parallelism,
                    getHashHex(),
                    encoded);
        }
    }

    private static String toHex(byte[] hash) {
        StringBuilder stringBuilder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            stringBuilder.append(String.format(Locale.US, "%02x", b));
        }
        return stringBuilder.toString();
    }

}
