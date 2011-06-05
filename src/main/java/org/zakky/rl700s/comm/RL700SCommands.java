/*
 * Copyright 2011 YAMAZAKI Makoto<makoto1975@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zakky.rl700s.comm;

import java.nio.ByteBuffer;
import java.util.EnumSet;

/**
 * RL-700S に対して送るコマンドを生成するクラスです。
 */
public final class RL700SCommands {
    /**
     * Android 端末から RL-700S へ転送するコマンドの最大長
     */
    private static final int MAX_OUT_SIZE = 64;

    /**
     * Android端末からプリンタへ送るコマンドのためのバッファを割り当てます。
     * 
     * @return バッファ。 {@link ByteBuffer#capacity() capacity()} が
     *         {@value #MAX_OUT_SIZE} な {@link ByteBuffer} を返します。
     */
    public static ByteBuffer allocateOutBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocate(MAX_OUT_SIZE);
        return buffer;
    }

    /**
     * 動作モードのための enum
     */
    public enum Mode {
        AUTO_TAPE_CUT(1 << 6), MIRROR(1 << 7);
        private final int mRawValue;

        private Mode(int rawValue) {
            mRawValue = rawValue;
        }

        public int rawValue() {
            return mRawValue;
        }

        public static byte modesValue(EnumSet<Mode> modes) {
            int value = 0;
            for (Mode m : modes) {
                value |= m.rawValue();
            }
            assert 0 <= value && value <= 255;
            return (byte) value;
        }
    };

    /**
     * 拡張モードのための enum
     */
    public enum EnhancedMode {
        HALF_CUT(1 << 2), // ハーフカット(マルチハーフカット)、ラミネートテープのみ
        NON_CHAIN_PRINT(1 << 3), // チェインプリントしない(最後の1枚をフィードカットする)
        CUT_ON_CHAIN_PRINT(1 << 5), // 連続印刷の時に最後の1枚の後部をカットする
        FINE_PRINT(1 << 6), // 高精細印刷(720dpi x 360 dpi)
        COPY_PRINT(1 << 7); // コピー印刷機能(印刷時バッファクリアなし)
        private final int mRawValue;

        private EnhancedMode(int rawValue) {
            mRawValue = rawValue;
        }

        public int rawValue() {
            return mRawValue;
        }

        public static byte modesValue(EnumSet<EnhancedMode> modes) {
            int value = 0;
            for (EnhancedMode m : modes) {
                value |= m.rawValue();
            }
            assert 0 <= value && value <= 255;
            return (byte) value;
        }
    };

    enum CommandMode {
        ESC_P(0), RASTER(1);
        private final int mRawValue;

        private CommandMode(int rawValue) {
            mRawValue = rawValue;
        }

        public int rawValue() {
            return mRawValue;
        }

    }

    enum CompressionMode {
        NONE(0), TIFF(2);
        private final int mRawValue;

        private CompressionMode(int rawVaue) {
            mRawValue = rawVaue;
        }

        public int rawValue() {
            return mRawValue;
        }
    }

    private static final byte ESC = (byte) 0x1b;

    /*
     * RL-700S に対して発行するコマンドのバイト列を生成するメソッド群
     */

    /**
     * 無効司令コマンド
     * 
     * @param buffer コマンド書き込み先バッファ。 {@link ByteBuffer#reset() reset()} し、
     *            コマンド書き込み後で {@link ByteBuffer#flip() flip()} したものを返します。
     * @param count コマンドの繰り返し回数。
     */
    public static void getVoid(ByteBuffer buffer, int count) {
        buffer.reset();
        final int c = Math.min(MAX_OUT_SIZE, count);
        for (int i = 0; i < c; i++) {
            buffer.put((byte) 0);
        }
        buffer.flip();
    }

    /**
     * ステータス情報リクエストコマンド
     * 
     * @param buffer コマンド書き込み先バッファ。 {@link ByteBuffer#reset() reset()} し、
     *            コマンド書き込み後で {@link ByteBuffer#flip() flip()} したものを返します。
     */
    public static void getStatus(ByteBuffer buffer) {
        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) 'i');
        buffer.put((byte) 'S');
        buffer.flip();
    }

    /**
     * 初期化コマンド
     * 
     * @param buffer コマンド書き込み先バッファ。 {@link ByteBuffer#reset() reset()} し、
     *            コマンド書き込み後で {@link ByteBuffer#flip() flip()} したものを返します。
     */
    public static void getInit(ByteBuffer buffer) {
        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) '@');
        buffer.flip();
    }

    /**
     * 各種モード設定コマンド
     * 
     * @param buffer コマンド書き込み先バッファ。 {@link ByteBuffer#reset() reset()} し、
     *            コマンド書き込み後で {@link ByteBuffer#flip() flip()} したものを返します。
     * @param modes 有効にするモードの集合。
     */
    public static void getSetMode(ByteBuffer buffer, EnumSet<Mode> modes) {
        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) 'i');
        buffer.put((byte) 'M');
        buffer.put((byte) Mode.modesValue(modes));
        buffer.flip();
    }

    public static void getSetMergin(ByteBuffer buffer, int mergin) {
        final byte low = (byte) ((mergin >>> 0) & 0xFF);
        final byte high = (byte) ((mergin >>> 8) & 0xFF);

        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) 'i');
        buffer.put((byte) 'd');
        buffer.put((byte) low);
        buffer.put((byte) high);
        buffer.flip();
    }

    /**
     * 各種モード設定コマンド
     * 
     * @param buffer コマンド書き込み先バッファ。 {@link ByteBuffer#reset() reset()} し、
     *            コマンド書き込み後で {@link ByteBuffer#flip() flip()} したものを返します。
     * @param modes 有効にする拡張モードの集合。
     */
    public static void getSetEnhancedMode(ByteBuffer buffer, EnumSet<EnhancedMode> modes) {
        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) 'i');
        buffer.put((byte) 'K');
        buffer.put((byte) EnhancedMode.modesValue(modes));
        buffer.flip();
    }

    public static void getSwitchCommandMode(ByteBuffer buffer, CommandMode mode) {
        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) 'i');
        buffer.put((byte) 'a');
        buffer.put((byte) mode.rawValue());
        buffer.flip();
    }

    public static void getSendRasterLine(ByteBuffer buffer, byte[] line, CompressionMode mode) {
        if (mode != CompressionMode.NONE) {
            throw new UnsupportedOperationException("unsupported compression mode: " + mode.name());
        }
        final int length = line.length;
        final byte low = (byte) ((length >>> 0) & 0xFF);
        final byte high = (byte) ((length >>> 8) & 0xFF);
        buffer.reset();
        buffer.put((byte) 'G');
        buffer.put(low);
        buffer.put(high);
        buffer.put(line);
        buffer.flip();
    }

    private static final int MAX_LINE_BYTES = 48;

    static byte[] packBits(byte[] line) {
        if (line.length != MAX_LINE_BYTES) {
            final byte[] orig = line;
            line = new byte[MAX_LINE_BYTES];
            System.arraycopy(orig, 0, line, 0, Math.min(orig.length, line.length));
        }

        final byte[] packed = new byte[line.length + 1];
        int inConsumed = 0; // line の消費済みバイト数
        int outConsumed = 0; // packed の消費済みバイト数
        boolean same = true;
        for (int current = 0; current < line.length; current++) {
            if (inConsumed == current) {
                // １つ目は仮に same として扱う(ここが末尾の場合に常にsameとして扱われるようにするため)
                same = true;
                continue;
            }
            assert inConsumed < current;
            if (current == inConsumed + 1) {
                // ２つ目なのでモードを確定
                same = (line[current - 1] == line[current]);
                continue;
            }
            // ３つ目以降
            if (same == (line[current - 1] == line[current])) {
                // モード継続中なのでなにもしない
                continue;
            }
            if (same) {
                final int length = current - inConsumed;
                byte[] unpacked = makeUnpackedIfOverflow(line, outConsumed + 2, packed);
                if (unpacked != null) {
                    return unpacked;
                }
                packed[outConsumed++] = (byte) (-1 * (length - 1)); // １つ減らしたものを負にするのが仕様
                packed[outConsumed++] = line[current - 1];
                inConsumed = current; // 数にインデックスを代入しているが、１つ前までの数はインデックスと一致する
            } else {
                // 違うで着ていたがここに来て１つ前と同じだった
                // ２つ前までを違うものとして消費し、１つ前から「同じ」が始まったものとして扱う
                final int length = current - inConsumed - 1;
                byte[] unpacked = makeUnpackedIfOverflow(line, outConsumed + 1 + length, packed);
                if (unpacked != null) {
                    return unpacked;
                }
                packed[outConsumed++] = (byte) (length - 1); // １つ減らして書くのが仕様
                System.arraycopy(line, inConsumed, packed, outConsumed, length);
                outConsumed += length;
                inConsumed = current - 1;
            }
            same = !same;
        }

        final int length = line.length - inConsumed;
        if (length != 0) {
            // 末尾に残された分を現在のモードに従いすべて消費する
            if (same) {
                byte[] unpacked = makeUnpackedIfOverflow(line, outConsumed + 2, packed);
                if (unpacked != null) {
                    return unpacked;
                }
                packed[outConsumed++] = (byte) (-1 * (length - 1)); // １つ減らしたものを負にするのが仕様
                packed[outConsumed++] = line[line.length - 1];
            } else {
                byte[] unpacked = makeUnpackedIfOverflow(line, outConsumed + 1 + length, packed);
                if (unpacked != null) {
                    return unpacked;
                }
                packed[outConsumed++] = (byte) (length - 1); // １つ減らして書くのが仕様
                System.arraycopy(line, inConsumed, packed, outConsumed, length);
                outConsumed += length;
            }
            inConsumed = line.length; // 更新する必要はないが念のため
        }

        // packed は後ろに余分なスペースが残っているので切り詰める
        final byte[] result = new byte[outConsumed];
        System.arraycopy(packed, 0, result, 0, outConsumed);

        return result;
    }

    private static byte[] makeUnpackedIfOverflow(byte[] in, int outLength, byte[] out) {
        if (outLength <= in.length) {
            // まだ元より長くはなってないのでセーフ
            return null;
        }
        // 収まらない場合は無圧縮
        assert out.length == in.length + 1;
        out[0] = (byte) (in.length - 1);
        System.arraycopy(in, 0, out, 1, in.length);
        return out;
    }

    public static void getSendZeroRasterLine(ByteBuffer buffer) {
        buffer.reset();
        buffer.put((byte) 'Z');
        buffer.flip();
    }

    public static void getStartPrintWithHalfCut(ByteBuffer buffer) {
        buffer.reset();
        buffer.put((byte) 0x0b);
        buffer.flip();
    }

    public static void getStartPrint(ByteBuffer buffer) {
        buffer.reset();
        buffer.put((byte) 0x0c);
        buffer.flip();
    }

    public static void getStartPrintWithEvacuation(ByteBuffer buffer) {
        buffer.reset();
        buffer.put((byte) 0x1a);
        buffer.flip();
    }

    enum Paper {
        UNKNOWN(0), LAMINATE(1), LETERING(2), NON_LAMINATE(3), HG(9), SZ(16);
        private final int mRawValue;

        private Paper(int rawVaue) {
            mRawValue = rawVaue;
        }

        public int rawValue() {
            return mRawValue;
        }

    }

    public static void getSetPrintInformation(ByteBuffer buffer, Paper paperKind,
            Integer paperWidth, Integer paperLength, boolean enableRecover, boolean lowPowerPrint) {
        final int mask = ((paperKind == null) ? 0 : (1 << 1))
                | ((paperWidth == null) ? 0 : (1 << 2)) | ((paperLength == null) ? 0 : (1 << 3))
                | (enableRecover ? 0xF0 : 0);
        buffer.reset();
        buffer.put(ESC);
        buffer.put((byte) 'i');
        buffer.put((byte) 'c');
        buffer.put((byte) mask);
        buffer.put((byte) (paperKind == null ? 0 : paperKind.rawValue()));
        buffer.put((byte) (paperWidth == null ? 0 : paperWidth.intValue()));
        buffer.put((byte) (paperLength == null ? 0 : paperLength.intValue()));
        buffer.put((byte) (lowPowerPrint ? 1 : 0));
        buffer.flip();
    }

    public static void getSelectCompressionMode(ByteBuffer buffer, CompressionMode mode) {
        buffer.reset();
        buffer.put((byte) 'M');
        buffer.put((byte) mode.rawValue());
        buffer.flip();
    }

}