/*
 * Copyright (c) 2016-17 Open Bell Project, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * WLD-Sebastian R
 * WLD-Paul J
 * WLD-Muj H
 *
 * Updated: 2/15/17 4:07 AM
 */

package org.openbell.sdk;

public class ItemBell {
	private int rowid;
	private String uuid;
	private String name;
	private int enabled;

	public ItemBell (int rowid, String uuid, String name, int enabled) {
		this.rowid = rowid;
		this.uuid = uuid;
		this.name = name;
		this.enabled = enabled;
	}

	public int getID () {
		return rowid;
	}

	public String getUUID () {
		return uuid;
	}

	public String getName () {
		return name;
	}

	public boolean getEnabled () {
		return (enabled == 1);
	}
}
