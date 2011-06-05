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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class RL700SCommandsTest {

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
    public void 正常_packBits_仕様書記載の例() {
        final byte[] in = {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x22, 0x22, 0x23, (byte) 0xba,
                (byte) 0xbf, (byte) 0xa2, 0x22, 0x2b
        };
        final byte[] expected = {
                (byte) 0xed, 0x00, (byte) 0xff, 0x22, 0x05, 0x23, (byte) 0xba,
                (byte) 0xbf, (byte) 0xa2, 0x22, 0x2b, (byte) 0xed, 0x00
        };
        final byte[] actual = RL700SCommands.packBits(in);
        print(expected, actual);

        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void 正常_packBits_空配列() {
        final byte[] in = {
        };
        final byte[] expected = {
                -47, 0
        };
        final byte[] actual = RL700SCommands.packBits(in);
        print(expected, actual);

        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void 正常_packBits_同じ値の連続なしでオーバーフロー() {
        final byte[] in = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
                23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
                44, 45, 46, 47
        };
        final byte[] expected = {
                47, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
                43, 44, 45, 46, 47
        };
        final byte[] actual = RL700SCommands.packBits(in);
        print(expected, actual);

        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void 正常_packBits_同じ値の連続ありでオーバーフロー() {
        final byte[] in = {
                0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                22,
                23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
                44, 45, 46
        };
        final byte[] expected = {
                47, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21,
                22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
                43, 44, 45, 46
        };
        final byte[] actual = RL700SCommands.packBits(in);
        print(expected, actual);

        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void 正常_packBits_同じ値の連続ありでオーバーフロー2() {
        final byte[] in = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
                23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
                44, 45, 46, 46
        };
        final byte[] expected = {
                47, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41,
                42, 43, 44, 45, 46, 46
        };
        final byte[] actual = RL700SCommands.packBits(in);
        print(expected, actual);

        assertTrue(Arrays.equals(expected, actual));
    }

    private static void print(byte[] expected, byte[] actual) {
        if (Boolean.getBoolean("dummy_key")) {
            System.out.println("expected: " + Arrays.toString(expected));
            System.out.println("actual: " + Arrays.toString(expected));
        }
    }
}
