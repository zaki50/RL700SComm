
package org.zakky.rl700s.comm;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zakky.rl700s.comm.RL700SStatus.ErrorInfo;

import java.nio.ByteBuffer;
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

    @Test
    public void 正常_Parse_代表例1() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocate(RL700SStatus.STATUS_SIZE);
        buffer.put((byte) 0x80);
        buffer.put((byte) 0x20);
        buffer.put((byte) 0x42);
        buffer.put((byte) 0x33);
        buffer.put((byte) 0x31);
        buffer.put((byte) 0x30);
        buffer.put((byte) 0x00);
        // 拡張エラー番号
        buffer.put((byte) RL700SStatus.EERR_MEDIA_FINISHED);

        // エラー情報1
        buffer.put((byte) 0x02);
        // エラー情報2
        buffer.put((byte) 0x10);
        // メディア幅
        buffer.put((byte) 210);
        // メディア種類
        buffer.put((byte) 150);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);

        // 予約(不定)
        buffer.put((byte) 0);
        // メディア長さ
        buffer.put((byte) 200);
        // ステータス種類
        buffer.put((byte) 0x06);
        // フェーズ種類
        buffer.put((byte) 0x01);
        // フェーズ番号上位バイト
        buffer.put((byte) 0x82);
        // フェーズ番号下位バイト
        buffer.put((byte) 0x7f);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);

        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);
        // 予約(不定)
        buffer.put((byte) 0);

        buffer.flip();

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

}
