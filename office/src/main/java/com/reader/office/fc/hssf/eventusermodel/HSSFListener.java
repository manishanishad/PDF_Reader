
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package com.reader.office.fc.hssf.eventusermodel;

import com.reader.office.fc.hssf.record.Records;

/**
 * Interface for use with the HSSFRequest and HSSFEventFactory.  Users should create
 * a listener supporting this interface and register it with the HSSFRequest (associating
 * it with Records SID's).
 *
 * @see com.reader.office.fc.hssf.eventusermodel.HSSFEventFactory
 * @see com.reader.office.fc.hssf.eventusermodel.HSSFRequest
 * @author  acoliver@apache.org
 */

public interface HSSFListener
{

    /**
     * process an HSSF Records. Called when a record occurs in an HSSF file.
     */

    public void processRecord(Records record);
}
