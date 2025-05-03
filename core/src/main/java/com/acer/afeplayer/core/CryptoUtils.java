package com.acer.afeplayer.core;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 加密工具类<br>
 * ot为original text<br>
 * ct为crypto text
 */
@SuppressWarnings("unused")
public final class CryptoUtils {
    private static final int AES_GCM_TAG_SIZE = 128; // AES-GCM模式 认证标签长度
    private static final HexFormat HEX_FORMATER; // 默认十六进制编解码器
    private static final SecureRandom secureRandom; // 安全随机类
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static {
        HEX_FORMATER = HexFormat.of();
        secureRandom = new SecureRandom();
    }

    /* *编码解码* */
    /* HEX */
    /**
     * 格式化为十六进制字符串
     * @param bytes 字节数组
     * @see HexFormat#formatHex(byte[])
     * @return 十六进制字符串
     */
    public static String formatHex(byte[] bytes) {
        return HEX_FORMATER.formatHex(bytes);
    }
    /**
     * 解析十六进制字符串
     * @param hex 十六进制字符串
     * @see HexFormat#parseHex(CharSequence)
     * @return 字节数组
     */
    public static byte[] parseHex(CharSequence hex) {
        return HEX_FORMATER.parseHex(hex);
    }

    /* Base64 */
    /**
     * 格式化为Base64（返回字节数组）
     * @param bytes 字节数组
     * @see Base64.Encoder#encode(byte[])
     * @return Base64字符串
     */
    public static byte[] formatBase64Only(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }
    /**
     * 格式化为Base64
     * @param bytes 字节数组
     * @param charset 编码格式
     * @see #formatBase64Only(byte[])
     * @return Base64字符串
     */
    public static String formatBase64(byte[] bytes, Charset charset) {
        return new String(formatBase64Only(bytes), charset);
    }
    /**
     * 格式化为Base64（编码为UTF-8）
     * @param bytes 字节数组
     * @see #formatBase64(byte[], Charset)
     * @return Base64字符串
     */
    public static String formatBase64(byte[] bytes) {
        return formatBase64(bytes, StandardCharsets.UTF_8);
    }
    /**
     * 解析Base64
     * @param bytes Base64字节数组
     * @see Base64.Decoder#decode(byte[])
     * @return 字节数组
     */
    public static byte[] parseBase64(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }
    /**
     * 解析Base64
     * @param str Base64字符串
     * @param charset 编码格式
     * @see #parseBase64(byte[])
     * @return 字节数组
     */
    public static byte[] parseBase64(String str, Charset charset) {
        return parseBase64(str.getBytes(charset));
    }
    /**
     * 解析Base64（编码为UTF-8）
     * @param str Base64字符串
     * @see #parseBase64(String, Charset)
     * @return 字节数组
     */
    public static byte[] parseBase64(String str) {
        return parseBase64(str, StandardCharsets.UTF_8);
    }

    /* SHA */
    /**
     * 哈希函数枚举类型
     */
    public enum SHAAlgorithm {
        MD5,
        SHA_1,
        SHA_256,
        SHA_384,
        SHA_512,
        SHA3_256("SHA3-256"),
        ;

        private final String id;
        SHAAlgorithm() {
            this.id = super.toString().replace('_', '-');
        }
        SHAAlgorithm(String id) {
            this.id = id;
        }
        @Override
        public String toString() {
            return id;
        }
    }
    /**
     * 格式化为哈希值
     * @param bytes 字节数组
     * @param algorithm 哈希函数
     * @return 哈希值
     */
    public static byte[] formatSHA(byte[] bytes, SHAAlgorithm algorithm) {
        try {
            return MessageDigest.getInstance(algorithm.toString()).digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 格式化为哈希值
     * @param str 字符串
     * @param charset 编码格式
     * @param algorithm 哈希函数
     * @see #formatSHA(byte[], SHAAlgorithm)
     * @return 哈希值
     */
    public static byte[] formatSHA(String str, Charset charset, SHAAlgorithm algorithm) {
        return formatSHA(str.getBytes(charset), algorithm);
    }
    // 快捷方法 //
    /**
     * 格式化为哈希值（SHA-256）（十六进制）
     * @see #formatSHA(byte[], SHAAlgorithm)
     */
    public static String formatSHA256HEX(byte[] bytes) {
        return formatHex(formatSHA(bytes, SHAAlgorithm.SHA_256));
    }
    /**
     * 格式化为哈希值（SHA3-256）（十六进制）
     * @see #formatSHA(byte[], SHAAlgorithm)
     */
    public static String formatSHA3256HEX(byte[] bytes) {
        return formatHex(formatSHA(bytes, SHAAlgorithm.SHA3_256));
    }


    /* *加密解密* */
    /**
     * 生成安全随机字节数组
     * @see SecureRandom#nextBytes(byte[])
     */
    public static byte[] buildRandomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    /**
     * 生成安全随机的字母字符串
     * @param size 字符串大小
     * @return 安全随机字符串
     */
    public static String buildRandomString(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(CHAR_UPPER.charAt(secureRandom.nextInt(CHAR_UPPER.length())));
        }
        return sb.toString();
    }

    /**
     * 密码操作模式
     */
    public enum CryptoMode {
        /// AES GCM<br>安全通用的加密模式
        AES_GCM("AES", "/GCM/NoPadding", 12),
        /// AES CTR<br>通用高速的加密模式
        AES_CTR("AES", "/CTR/NoPadding", 16),
        ;

        public final String type;
        public final String id;
        public final int ivLength; // iv长度
        CryptoMode(String type, String id, int ivLength) {
            this.type = type;
            this.id = id;
            this.ivLength = ivLength;
        }
        @Override
        public String toString() {
            return type + id;
        }

        /**
         * 生成指定加密类型的参数规范
         * @param iv iv
         * @return 参数规范
         */
        public AlgorithmParameterSpec buildParameterSpec(byte[] iv) {
            switch (this) {
                case AES_GCM -> {
                    return new GCMParameterSpec(AES_GCM_TAG_SIZE, iv);
                }
                case AES_CTR -> {
                    return new IvParameterSpec(iv);
                }
                default -> throw new RuntimeException("无效CryptoMode类型");
            }
        }
        /**
         * 获取合适当前模式的Cipher
         * @return 新Cipher实例
         */
        public Cipher buildCipher() {
            try {
                return Cipher.getInstance(toString());
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException(e); // 内部异常
            }
        }
        /**
         * 初始化Cipher
         */
        public Cipher initCipher(Cipher cipher, boolean encryptMode, byte[] key, byte[] iv) {
            try {
                cipher.init(encryptMode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                        new SecretKeySpec(key, type), buildParameterSpec(iv));
            } catch (InvalidKeyException e) {
                throw new CryptoRuntimeException(e); // 外部异常
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e); // 内部异常
            }
            return cipher;
        }
        /**
         * 获取Cipher并初始化
         * @return 初始化的Cipher
         */
        public Cipher buildCipherInit(boolean encryptMode, byte[] key, byte[] iv) {
            return initCipher(buildCipher(), encryptMode, key, iv);
        }
    }
    /**
     * AES密码操作
     * @param oriData 原文 / 密文
     * @param key 密钥字节数组（需要对齐）
     * @param mode 加密模式
     * @param encryptMode 操作类型（true:加密，false:解密）
     * @throws CryptoRuntimeException 加密发生异常 如密钥无法对齐等
     * @return 密文 / 原文
     */
    public static byte[] cryptoAES(byte[] oriData, byte[] key, CryptoMode mode, boolean encryptMode) {
        try {
            byte[] iv; // iv
            byte[] data; // 数据
            /* 初始化iv和数据 */
            if (encryptMode) {
                /* 加密模式 */
                // 生成iv和调用原文 //
                iv = buildRandomBytes(mode.ivLength);
                data = oriData;
            } else {
                /* 解密模式 */
                // 获取iv和密文 //
                iv = new byte[mode.ivLength];
                data = new byte[oriData.length - iv.length];
                System.arraycopy(oriData, 0, iv, 0, iv.length);
                System.arraycopy(oriData, iv.length, data, 0, data.length);
            }
            /* 进行密码操作 */
            byte[] proData = mode.buildCipherInit(encryptMode, key, iv).doFinal(data);
            /* 处理输出数据 */
            if (encryptMode) {
                /* 加密模式 */
                // 结合iv和密文 //
                byte[] outData = new byte[iv.length + proData.length];
                System.arraycopy(iv, 0, outData, 0, iv.length);
                System.arraycopy(proData, 0, outData, iv.length, proData.length);
                return outData;
            } else {
                /* 解密模式 */
                return proData; // 返回原文
            }
        } catch (IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e); // 内部异常
        }
    }
    /// 操作监听接口
    public interface CryptoEventHandle {
        void onCrypto(int handleLength);
    }
    /**
     * AES-CTR单线程堵塞流 密码操作<br>
     * @param in 输入流（加密时：原数据流）
     * @param out 输出流（加密时：原数据流）
     * @param key 密钥字节数组（需要对齐）
     * @param mode 加密模式（建议使用AES_CTR）
     * @param bufferSize 缓冲区大小 按需设置 建议不超过1024*1024 (8GB)
     * @param encryptMode  操作类型（true:加密，false:解密）
     * @param eventHandle 流式操作事件接口
     */
    public static void cryptoAESStream(InputStream in, OutputStream out, byte[] key, CryptoMode mode, int bufferSize,
                                       boolean encryptMode, CryptoEventHandle eventHandle) {
        try {
            // 获取iv //
            byte[] iv;
            if (encryptMode) {
                iv = buildRandomBytes(mode.ivLength); // 构建iv
                out.write(iv); // 写入到头
            } else iv = in.readNBytes(mode.ivLength); // 获取iv
            if (iv.length == 0) throw new RuntimeException("Iv.length == 0！");
            // 构建初始化cipher //
            Cipher cipher = mode.buildCipherInit(encryptMode, key, iv);
            // 开始操作 //
            if (encryptMode) {
                try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
                    byte[] buffer = new byte[bufferSize];
                    int readBytes;
                    while ((readBytes = in.read(buffer)) != -1) {
                        if (eventHandle != null) eventHandle.onCrypto(readBytes);
                        cos.write(buffer, 0, readBytes);
                    }
                }
            } else {
                try (CipherInputStream cis = new CipherInputStream(in, cipher)) {
                    byte[] buffer = new byte[bufferSize];
                    int readBytes;
                    while ((readBytes = cis.read(buffer)) != -1) {
                        if (eventHandle != null) eventHandle.onCrypto(readBytes);
                        out.write(buffer, 0, readBytes);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 密码操作块
     * @param index
     * @param data
     */
    public record CryptoChunk(long index, ByteBuffer data, boolean isLast) implements Comparable<CryptoChunk> {
        public CryptoChunk {
            data.flip();
        }

        // 比较大小 //
        @Override
        public int compareTo(CryptoChunk o) {
            return Long.compare(index, o.index); // 比较计数索引
        }
    }
    /**
     * 构建块iv
     * @param rootIv 总iv
     * @param index 索引
     * @param chunkSize 块大小
     * @return 块iv
     */
    public static byte[] buildChunkIv(byte[] rootIv, long index, int chunkSize) {
        return ByteBuffer.allocate(16)
                .put(rootIv, 0, 8)
                .putLong(index * (chunkSize / 16))
                .array();
    }
    /**
     * AES-CTR多线程堵塞流 密码操作<br>
     * （由于技术原因目前无法和单线程密码操作兼容）<br>
     * （实验性功能 有不少问题 目前不建议使用）
     * @param in 输入流
     * @param out 输出流
     * @param key 密钥字节数组（需要对齐）
     * @param mode 加密模式（建议使用AES_CTR）
     * @param chunkSize 线程块大小 按需设置
     * @param encryptMode  操作类型（true:加密，false:解密）
     * @param eventHandle 流式操作事件接口
     */
    public static void cryptoAESStreamMultiThread(InputStream in, OutputStream out, byte[] key, CryptoMode mode, int chunkSize,
                                                  boolean encryptMode, CryptoEventHandle eventHandle) {
        // 创建线程池 //
        try (ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            /* 初始化 */
            PriorityBlockingQueue<CryptoChunk> orderedQueue =
                    new PriorityBlockingQueue<>(32, CryptoChunk::compareTo); // 创建线程队列
            AtomicLong readCounter = new AtomicLong(0); // 读取原子计数器
            AtomicLong writerCounter = new AtomicLong(0); // 写入原子计数器
            // 获取iv
            byte[] iv;
            if (encryptMode) {
                iv = buildRandomBytes(mode.ivLength); // 生成8位iv
                out.write(iv); // 写入头部
            } else {
                iv = in.readNBytes(mode.ivLength);
            }

            // 启动写入进程 //
            Thread writeThread = new Thread(() -> {
                try {
                    // 循环 //
                    while (true) {
                        CryptoChunk chunk = orderedQueue.poll(100, TimeUnit.MILLISECONDS);
                        // 验证是否结束 //
                        if (chunk == null) {
                            if (writerCounter.get() == readCounter.get()) break; // 读取数量和写入数量相等即完成
                            continue;
                        }
                        // 严格顺序检查 //
                        if (chunk.index != writerCounter.get()) {
                            orderedQueue.put(chunk);
                            continue;
                        }
                        if (eventHandle != null) eventHandle.onCrypto(chunk.data.array().length); // 完成调用接口
                        out.write(chunk.data.array()); // 写入数据
                        writerCounter.incrementAndGet(); // 写入计数器自增
                        // 验证是否结束 //
                        if (chunk.isLast) {
                            break; // 完成
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writeThread.start();

            // 分块处理 //
            {
                byte[] buffer = new byte[chunkSize];
                int readBytes;
                while ((readBytes = in.readNBytes(buffer, 0, buffer.length)) > 0) {
                    long index = readCounter.getAndIncrement(); // 获取计数索引并自增
                    byte[] chunkData = Arrays.copyOf(buffer, readBytes); // 块数据
                    boolean isLast = readBytes < buffer.length; // 数据不完整则为最终一块
                    // 发放线程操作 //
                    executorService.submit(() -> {
                        try {
                            Cipher cipher = mode.buildCipherInit(encryptMode, key, buildChunkIv(iv, index, chunkSize));
                            byte[] processed = cipher.doFinal(chunkData); // 密码操作
                            orderedQueue.put(new CryptoChunk(index, ByteBuffer.wrap(processed), isLast)); // 放入队列
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            writeThread.join(); // 堵塞等待写入线程执行完毕
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
