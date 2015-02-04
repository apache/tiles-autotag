/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tiles.autotag.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Decouples the autotag generator from the actual location of the files.
 */
public interface OutputLocator {
	/**
	 * Returns a writer for the file at this path.
	 * @param resourcePath the path of the file to write
	 * @return a Writer for the file.
	 */
	OutputStream getOutputStream(String resourcePath) throws IOException;
	
	/**
	 * Checks if the output is up to date.
	 * @param resourcePath the path of the file to write.
	 * @return true if the output doesn't need to be generated again.
	 */
	boolean isUptodate(String resourcePath);
}
