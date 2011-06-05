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

public final class RL700SStatus {

    /**
     * RL-700S から Android 端末へ転送するステータスの最大長
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

    public static RL700SStatus parse(ByteBuffer buffer) {
        throw new RuntimeException("not implemented.");
    }
}
