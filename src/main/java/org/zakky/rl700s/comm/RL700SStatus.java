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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.EnumSet;

public final class RL700SStatus {

    /**
     * RL-700S から Android 端末へ一度に送信できるデータの最大長
     */
    private static final int MAX_OUT_SIZE = 16;

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

    public static final int STATUS_SIZE = 32;

    public static RL700SStatus parse(ByteBuffer buffer) throws ParseException {
        if (buffer.remaining() < STATUS_SIZE) {
            throw new BufferUnderflowException();
        }
        final byte[] array = buffer.array();
        int current = buffer.arrayOffset() + buffer.position();
        if (array[current++] != (byte) 0x80) {
            // ヘッダーマーカーエラー
            throw new ParseException("missing header marker.", current - 1);
        }
        if (array[current++] != (byte) 0x20) {
            // サイズエラー
            throw new ParseException("invalid size.", current - 1);
        }
        if (array[current++] != (byte) 'B') {
            // エラー
            throw new ParseException("invalid value.", current - 1);
        }
        if (array[current++] != (byte) '3') {
            // エラー
            throw new ParseException("invalid value.", current - 1);
        }
        if (array[current++] != (byte) '1') {
            // エラー
            throw new ParseException("invalid value.", current - 1);
        }
        if (array[current++] != (byte) '0') {
            // エラー
            throw new ParseException("invalid value.", current - 1);
        }
        if (array[current++] != (byte) 0x00) {
            // エラー
            throw new ParseException("invalid value.", current - 1);
        }
        final int enhancedErrorCode = array[current++] & 0xff;
        final int enhancedInfo1 = array[current++] & 0xff;
        final int enhancedInfo2 = array[current++] & 0xff;
        final int mediaWidth = array[current++] & 0xff;
        final int mediaType = array[current++] & 0xff;
        current += 5; // 不定領域
        final int mediaLength = array[current++] & 0xff;
        final int statusType = array[current++] & 0xff;
        final int phaseType = array[current++] & 0xff;
        final int phaseNumber = ((array[current++] & 0xff) << 8) | (array[current++] & 0xff);
        current += 10; // 不定領域

        assert current == buffer.arrayOffset() + buffer.position() + STATUS_SIZE;
        buffer.position(buffer.position() + STATUS_SIZE);

        return new RL700SStatus(enhancedErrorCode, enhancedInfo1, enhancedInfo2, mediaWidth,
                mediaType, mediaLength, statusType, phaseType, phaseNumber);
    }

    enum ErrorInfo {
        /** メディア無し */
        NO_MEDIA(bytesToInt((1 << 0), 0)),
        /** メディア終了(再開した場合、同じページから印刷) */
        MEDIA_END(bytesToInt((1 << 1), 0)),
        /** カッタージャム */
        CUTTER_JAM(bytesToInt((1 << 2), 0)),
        /** バッテリー弱 */
        LOW_BATTERY(bytesToInt((1 << 3), 0)),
        /** 本体ビジー(本体印刷中またはクーリング中) */
        BUSY(bytesToInt((1 << 4), 0)),
        /** 未使用 */
        UNUSED1(bytesToInt((1 << 5), 0)),
        /** 高圧アダプタ */
        HIGH_VOLTAGE_ADAPTER(bytesToInt((1 << 6), 0)),
        /** 未使用 */
        UNUSED2(bytesToInt((1 << 7), 0)),
        /** メディア交換 */
        MEDIA_CHANGE(bytesToInt(0, (1 << 0))),
        /** 展開バッファフル */
        EXPANSION_BUFFER_FULL(bytesToInt(0, (1 << 1))),
        /** 通信エラー */
        COMMUNICATION_ERROR(bytesToInt(0, (1 << 2))),
        /** 通信バッファフル */
        COMMUNICATION_BUFFER_FULL(bytesToInt(0, (1 << 3))),
        /** カバーオープン */
        COVER_OPEN(bytesToInt(0, (1 << 4))),
        /** 未使用 */
        UNUSED3(bytesToInt(0, (1 << 5))),
        /** 先端検出エラー */
        HEAD_DETECTION_ERROR(bytesToInt(0, (1 << 6))),
        /** RFID エラー */
        RFID_ERROR(bytesToInt(0, (1 << 7)));

        private final int mRawValue;

        private ErrorInfo(int value) {
            assert 0 <= value && value <= (1 << 16) - 1;
            assert Integer.bitCount(value) == 1;
            mRawValue = value;
        }

        private static int bytesToInt(int errorInfo1, int errorInfo2) {
            if (errorInfo1 < 0 || 255 < errorInfo1) {
                throw new RuntimeException("'errorInfo1' out of range: " + errorInfo1);
            }
            if (errorInfo2 < 0 || 255 < errorInfo2) {
                throw new RuntimeException("'errorInfo2' out of range: " + errorInfo2);
            }
            final int value = errorInfo1 | (errorInfo2 << 8);
            return value;
        }

        public static EnumSet<ErrorInfo> fromRawValue(int errorInfo1, int errorInfo2) {
            final int value = bytesToInt(errorInfo1, errorInfo2);
            final EnumSet<ErrorInfo> result = EnumSet.noneOf(RL700SStatus.ErrorInfo.class);
            for (ErrorInfo e : values()) {
                if ((e.mRawValue & value) != 0) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    private final int mEnhancedErrorCode;
    private final EnumSet<ErrorInfo> mErrorInfoSet;
    private final int mMediaWidth;
    private final int mMediaType;
    private final int mMediaLength;
    private final int mStatusType;
    private final int mPhaseType;
    private final int mPhaseNumber;

    public RL700SStatus(int enhancedErrorCode, int errorInfo1, int errorInfo2,
            int mediaWidth, int mediaType, int mediaLength, int statusType, int phaseType,
            int phaseNumber) {
        super();
        mEnhancedErrorCode = enhancedErrorCode;
        mErrorInfoSet = ErrorInfo.fromRawValue(errorInfo1, errorInfo2);
        mMediaWidth = mediaWidth;
        mMediaType = mediaType;
        mMediaLength = mediaLength;
        mStatusType = statusType;
        mPhaseType = phaseType;
        mPhaseNumber = phaseNumber;
    }

    public static final int EERR_MEDIA_FINISHED = 0x10;

    /**
     * 拡張エラーコードを返します。
     * 
     * @return 拡張エラーコード。 {@link RL700SStatus} に定義されている {@code EERR_} で始まる定数を参照。
     * @see #EERR_MEDIA_FINISHED
     */
    public int getEnhancedErrorCode() {
        return mEnhancedErrorCode;
    }

    /**
     * エラー情報のセットを返します。
     * 
     * @return エラー情報セット。
     */
    public EnumSet<ErrorInfo> getErrorInfoSet() {
        return EnumSet.copyOf(mErrorInfoSet);
    }

    public int getMediaWidth() {
        return mMediaWidth;
    }

    public int getMediaType() {
        return mMediaType;
    }

    public int getMediaLength() {
        return mMediaLength;
    }

    public int getStatusType() {
        return mStatusType;
    }

    public int getPhaseType() {
        return mPhaseType;
    }

    public int getPhaseNumber() {
        return mPhaseNumber;
    }

}
