/*
 * Copyright (C) 2017 Stephan Fuhrmann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.sfuhrm.capsula.targetbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads an input stream and puts each line to a given consumer.
 * @author Stephan Fuhrmann
 */
@Slf4j
class InputStreamConsumer implements Runnable {
    /** The charset to read data with. */
    private final Charset charset;

    /** Where the data is read from. */
    private final InputStream in;

    /** Where to put the input streams lines to. */
    private final Consumer<String> consumer;

    /**
     * Creates a new instance.
     * @param myInputStream the input stream to read. Must be in the
     *                      charset specified in {@code myCharset}.
     * @param myConsumer the consumer to put each line read from the
     *                   input stream to.
     * @param myCharset the charset the input stream is encoded in.
     */
    InputStreamConsumer(final InputStream myInputStream,
                        final Consumer<String> myConsumer,
                        final Charset myCharset) {
        this.in = Objects.requireNonNull(myInputStream);
        this.consumer = Objects.requireNonNull(myConsumer);
        this.charset = Objects.requireNonNull(myCharset);
    }

    @Override
    public void run() {
        try {
            Reader reader = new InputStreamReader(in, charset);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while (null != (line = bufferedReader.readLine())) {
                consumer.accept(line);
            }
        } catch (IOException ex) {
            log.error("Error while reading input stream", ex);
        }
    }

}
