
package org.zakky.rl700s.comm;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zakky.rl700s.comm.RL700SStatus.ErrorInfo;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.EnumSet;

public class RL700SStatusTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private static ByteBuffer createDefaultStatusBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocate(RL700SStatus.STATUS_SIZE);
        buffer.put((byte) 0x80);
        buffer.put((byte) 0x20);
        buffer.put((byte) 0x42);
        buffer.put((byte) 0x33);
        buffer.put((byte) 0x31);
        buffer.put((byte) 0x30);
        buffer.put((byte) 0x00);
        // 拡張エラー番号
        buffer.put((byte) 0x00);

        // エラー情報1
        buffer.put((byte) 0x00);
        // エラー情報2
        buffer.put((byte) 0x00);
        // メディア幅
        buffer.put((byte) 0x00);
        // メディア種類
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);

        // 予約(不定)
        buffer.put((byte) 0x00);
        // メディア長さ
        buffer.put((byte) 0x00);
        // ステータス種類
        buffer.put((byte) 0x00);
        // フェーズ種類
        buffer.put((byte) 0x00);
        // フェーズ番号上位バイト
        buffer.put((byte) 0x00);
        // フェーズ番号下位バイト
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);

        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);
        // 予約(不定)
        buffer.put((byte) 0x00);

        buffer.flip();

        return buffer;
    }

    @Test
    public void 正常_Parse_デフォルト() throws Exception {
        final ByteBuffer buffer = createDefaultStatusBuffer();

        final RL700SStatus status = RL700SStatus.parse(buffer);

        assertEquals("parse must consume " + RL700SStatus.STATUS_SIZE + "bytes in buffer", 0,
                buffer.remaining());

        assertEquals(0, status.getEnhancedErrorCode());
        assertEquals(EnumSet.noneOf(ErrorInfo.class),
                status.getErrorInfoSet());
        assertEquals(0, status.getMediaWidth());
        assertEquals(0, status.getMediaType());
        assertEquals(0, status.getMediaLength());
        assertEquals(0, status.getStatusType());
        assertEquals(0, status.getPhaseType());
        assertEquals(0, status.getPhaseNumber());
    }

    @Test
    public void 正常_Parse_代表例1() throws Exception {
        final ByteBuffer buffer = createDefaultStatusBuffer();
        // 拡張エラー番号
        buffer.put(7, (byte) RL700SStatus.EERR_MEDIA_FINISHED);

        // エラー情報1
        buffer.put(8, (byte) 0x02);
        // エラー情報2
        buffer.put(9, (byte) 0x10);
        // メディア幅
        buffer.put(10, (byte) 210);
        // メディア種類
        buffer.put(11, (byte) 150);
        // メディア長さ
        buffer.put(17, (byte) 200);
        // ステータス種類
        buffer.put(18, (byte) 0x06);
        // フェーズ種類
        buffer.put(19, (byte) 0x01);
        // フェーズ番号上位バイト
        buffer.put(20, (byte) 0x82);
        // フェーズ番号下位バイト
        buffer.put(21, (byte) 0x7f);

        final RL700SStatus status = RL700SStatus.parse(buffer);

        assertEquals("parse must consume " + RL700SStatus.STATUS_SIZE + "bytes in buffer", 0,
                buffer.remaining());

        assertEquals(RL700SStatus.EERR_MEDIA_FINISHED, status.getEnhancedErrorCode());
        assertEquals(EnumSet.of(ErrorInfo.MEDIA_END, ErrorInfo.COVER_OPEN),
                status.getErrorInfoSet());
        assertEquals(210, status.getMediaWidth());
        assertEquals(150, status.getMediaType());
        assertEquals(200, status.getMediaLength());
        assertEquals(6, status.getStatusType());
        assertEquals(1, status.getPhaseType());
        assertEquals(0x827f, status.getPhaseNumber());
    }

    @Test(expected = ParseException.class)
    public void 異常_Parse_不正なマーカー() throws Exception {
        final ByteBuffer buffer = createDefaultStatusBuffer();
        buffer.put(0, (byte) 0x81);

        RL700SStatus.parse(buffer);
    }

    @Test
    public void 正常_ErrorInfo_fromRawValue() throws Exception {
        assertEquals(EnumSet.noneOf(ErrorInfo.class), ErrorInfo.fromRawValue(0, 0));
        assertEquals(EnumSet.of(ErrorInfo.NO_MEDIA, ErrorInfo.MEDIA_END, ErrorInfo.CUTTER_JAM,
                ErrorInfo.LOW_BATTERY), ErrorInfo.fromRawValue(0x0f, 0));
        assertEquals(EnumSet.of(ErrorInfo.COVER_OPEN, ErrorInfo.UNUSED3,
                ErrorInfo.HEAD_DETECTION_ERROR, ErrorInfo.RFID_ERROR), ErrorInfo.fromRawValue(0,
                0xf0));
        assertEquals(EnumSet.allOf(ErrorInfo.class), ErrorInfo.fromRawValue(255, 255));
    }

    @Test(expected = RuntimeException.class)
    public void 異常_ErrorInfo_fromRawValueのerrorInfo1が範囲外1() throws Exception {
        ErrorInfo.fromRawValue(-1, 100);
    }

    @Test(expected = RuntimeException.class)
    public void 異常_ErrorInfo_fromRawValueのerrorInfo1が範囲外2() throws Exception {
        ErrorInfo.fromRawValue(256, 100);
    }

    @Test(expected = RuntimeException.class)
    public void 異常_ErrorInfo_fromRawValueのerrorInfo2が範囲外1() throws Exception {
        ErrorInfo.fromRawValue(100, -1);
    }

    @Test(expected = RuntimeException.class)
    public void 異常_ErrorInfo_fromRawValueのerrorInfo2が範囲外2() throws Exception {
        ErrorInfo.fromRawValue(100, 256);
    }
}
